package com.tencent.iot.explorer.link.core.link.service

import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.JSONArray
import com.alibaba.fastjson.JSONObject
import java.nio.ByteBuffer

class LLSyncTLVEntity {

    companion object {
        /************** LLSync TLV Type bit 7、6、5 ****************/
        const val LLSYNC_TLV_TYPE_BOOLEAN = 0x00 shl 5  //0x00    0000 0000    boolean -> 0
        const val LLSYNC_TLV_TYPE_INT = 0x01 shl 5  //0x20    0010 0000    int -> 1
        const val LLSYNC_TLV_TYPE_STRING = 0x02 shl 5  //0x40    0100 0000    string -> 2
        const val LLSYNC_TLV_TYPE_FLOAT = 0x03 shl 5  //0x60    0110 0000    float -> 3
        const val LLSYNC_TLV_TYPE_ENUM = 0x04 shl 5  //0x80    1000 0000    enum -> 4
        const val LLSYNC_TLV_TYPE_TIMESTAMP = 0x05 shl 5  //0xa0    1010 0000    timestamp -> 5
        const val LLSYNC_TLV_TYPE_STRUCT = 0x06 shl 5  //0xc0    1100 0000    struct -> 6
        const val LLSYNC_TLV_TYPE_ARRAY = 0x07 shl 5  //0xe0    1110 0000    struct -> 7

        const val LLSYNC_TLV_LENGTH_BOOLEAN = 1  //可省略
        const val LLSYNC_TLV_LENGTH_INT = 4 //可省略
        const val LLSYNC_TLV_LENGTH_STRING = 2
        const val LLSYNC_TLV_LENGTH_FLOAT = 4 //可省略
        const val LLSYNC_TLV_LENGTH_ENUM = 2 //可省略
        const val LLSYNC_TLV_LENGTH_TIMESTAMP = 4 //可省略
        const val LLSYNC_TLV_LENGTH_STRUCT = 2
        const val LLSYNC_TLV_LENGTH_ARRAY = 2
    }

    fun getControlBleDevicebyteArr(valueType: String, index: Int, value: String, define: String): ByteArray {
        when (valueType) {
            "bool" -> {
                var type = LLSYNC_TLV_TYPE_BOOLEAN xor index
                var boolByte = value.toByte()
                var byteArr = ByteArray(2)
                byteArr[0] = type.toByte()
                byteArr[1] = boolByte
                return byteArr
            }
            "int" -> {
                var type = LLSYNC_TLV_TYPE_INT xor index
                var intByte = BleConfigService.get().number2Bytes(value.toLong(), LLSYNC_TLV_LENGTH_INT)
                var byteArr = ByteArray(1+LLSYNC_TLV_LENGTH_INT)
                byteArr[0] = type.toByte()
                System.arraycopy(intByte, 0, byteArr, 1, LLSYNC_TLV_LENGTH_INT)
                return byteArr
            }
            "string" -> { //只有字符串、结构体和数组类型拥有Length字段。
                var type = LLSYNC_TLV_TYPE_STRING xor index
                var stringByte = value.toByteArray()
                var stringLengthByte = BleConfigService.get().number2Bytes(stringByte.size.toLong(), LLSYNC_TLV_LENGTH_STRING)
                var byteArr = ByteArray(1+LLSYNC_TLV_LENGTH_STRING+stringByte.size)
                byteArr[0] = type.toByte()
                System.arraycopy(stringLengthByte, 0, byteArr, 1, LLSYNC_TLV_LENGTH_STRING)
                System.arraycopy(stringByte, 0, byteArr, LLSYNC_TLV_LENGTH_STRING+1, stringByte.size)
                return byteArr
            }
            "float" -> {
                var type = LLSYNC_TLV_TYPE_FLOAT xor index
                var floatByte = ByteBuffer.allocate(LLSYNC_TLV_LENGTH_FLOAT).putFloat(value.toFloat()).array()
                var byteArr = ByteArray(1+LLSYNC_TLV_LENGTH_FLOAT)
                byteArr[0] = type.toByte()
                System.arraycopy(floatByte, 0, byteArr, 1, LLSYNC_TLV_LENGTH_FLOAT)
                return byteArr
            }
            "enum" -> {
                var type = LLSYNC_TLV_TYPE_ENUM xor index
                var enumByte = BleConfigService.get().number2Bytes(value.toLong(), LLSYNC_TLV_LENGTH_ENUM)
                var byteArr = ByteArray(1+LLSYNC_TLV_LENGTH_ENUM)
                byteArr[0] = type.toByte()
                System.arraycopy(enumByte, 0, byteArr, 1, LLSYNC_TLV_LENGTH_ENUM)
                return byteArr
            }
            "stringenum" ->{//蓝牙设备暂时不支持

            }
            "timestamp" -> {
                var type = LLSYNC_TLV_TYPE_TIMESTAMP xor index
                var timestampByte = BleConfigService.get().number2Bytes(value.toLong(), LLSYNC_TLV_LENGTH_TIMESTAMP)
                var byteArr = ByteArray(1+LLSYNC_TLV_LENGTH_TIMESTAMP)
                byteArr[0] = type.toByte()
                System.arraycopy(timestampByte, 0, byteArr, 1, LLSYNC_TLV_LENGTH_TIMESTAMP)
                return byteArr
            }
            "struct" -> { //只有字符串、结构体和数组类型拥有Length字段。暂时不支持结构体嵌套结构体
                var valueJson = JSON.parseObject(value)
                var type = LLSYNC_TLV_TYPE_STRUCT xor index
                var specsArr = JSON.parseObject(define).getJSONArray("specs")
                var totalSpecsByteArr = ByteArray(0)
                specsArr.forEachIndexed { index, it ->
                    var tempJSON = JSON.parseObject(it.toString())
                    if (valueJson.getString(tempJSON.getString("id")) != null) {
                        var tempSpecs = getControlBleDevicebyteArr(tempJSON.getJSONObject("dataType").getString("type"), index, valueJson.getString(tempJSON.getString("id")), "")
                        var lastTotalSpecsByteArr = totalSpecsByteArr.copyOf()
                        totalSpecsByteArr = ByteArray(lastTotalSpecsByteArr.size +tempSpecs.size)
                        System.arraycopy(lastTotalSpecsByteArr, 0, totalSpecsByteArr, 0, lastTotalSpecsByteArr.size)
                        System.arraycopy(tempSpecs, 0, totalSpecsByteArr, lastTotalSpecsByteArr.size, tempSpecs.size)
                    }
                }
                var structLengthByte = BleConfigService.get().number2Bytes(totalSpecsByteArr.size.toLong(), LLSYNC_TLV_LENGTH_STRUCT)
                var byteArr = ByteArray(1+LLSYNC_TLV_LENGTH_STRUCT+totalSpecsByteArr.size)
                byteArr[0] = type.toByte()
                System.arraycopy(structLengthByte, 0, byteArr, 1, LLSYNC_TLV_LENGTH_STRUCT)
                System.arraycopy(totalSpecsByteArr, 0, byteArr, LLSYNC_TLV_LENGTH_STRUCT+1, totalSpecsByteArr.size)
                return byteArr
            }
            "array" -> { //只有字符串、结构体和数组类型拥有Length字段。数组的成员可以是整形、字符串、结构体、浮点型
                var valueArr = JSON.parseArray(value)
                var type = LLSYNC_TLV_TYPE_ARRAY xor index
                var arrayInfo = JSON.parseObject(define).getJSONObject("arrayInfo")
                var totalValueByteArr = ByteArray(0)
                if (arrayInfo.getString("type").equals("int")) { //数组的成员是整形
                    valueArr.forEachIndexed { _, it ->
                        var tempValue = BleConfigService.get().number2Bytes(it.toString().toLong(), LLSYNC_TLV_LENGTH_INT)
                        var lastTotalValueByteArr = totalValueByteArr.copyOf()
                        totalValueByteArr = ByteArray(lastTotalValueByteArr.size +tempValue.size)
                        System.arraycopy(lastTotalValueByteArr, 0, totalValueByteArr, 0, lastTotalValueByteArr.size)
                        System.arraycopy(tempValue, 0, totalValueByteArr, lastTotalValueByteArr.size, tempValue.size)
                    }
                } else if (arrayInfo.getString("type").equals("string")) {
                    valueArr.forEachIndexed { _, it ->

                        var stringByte = it.toString().toByteArray()
                        var stringLengthByte = BleConfigService.get().number2Bytes(stringByte.size.toLong(), LLSYNC_TLV_LENGTH_STRING)
                        var tempValue = ByteArray(LLSYNC_TLV_LENGTH_STRING+stringByte.size)
                        System.arraycopy(stringLengthByte, 0, tempValue, 0, LLSYNC_TLV_LENGTH_STRING)
                        System.arraycopy(stringByte, 0, tempValue, LLSYNC_TLV_LENGTH_STRING, stringByte.size)

                        var lastTotalValueByteArr = totalValueByteArr.copyOf()
                        totalValueByteArr = ByteArray(lastTotalValueByteArr.size +tempValue.size)
                        System.arraycopy(lastTotalValueByteArr, 0, totalValueByteArr, 0, lastTotalValueByteArr.size)
                        System.arraycopy(tempValue, 0, totalValueByteArr, lastTotalValueByteArr.size, tempValue.size)
                    }
                } else if (arrayInfo.getString("type").equals("float")) {
                    valueArr.forEachIndexed { _, it ->
                        var floatByte = ByteBuffer.allocate(LLSYNC_TLV_LENGTH_FLOAT).putFloat(it.toString().toFloat()).array()
                        var tempValue = ByteArray(LLSYNC_TLV_LENGTH_FLOAT)
                        System.arraycopy(floatByte, 0, tempValue, 0, LLSYNC_TLV_LENGTH_FLOAT)
                        var lastTotalValueByteArr = totalValueByteArr.copyOf()
                        totalValueByteArr = ByteArray(lastTotalValueByteArr.size +tempValue.size)
                        System.arraycopy(lastTotalValueByteArr, 0, totalValueByteArr, 0, lastTotalValueByteArr.size)
                        System.arraycopy(tempValue, 0, totalValueByteArr, lastTotalValueByteArr.size, tempValue.size)
                    }
                } else if (arrayInfo.getString("type").equals("struct")) {
                    valueArr.forEachIndexed { _, it ->
                        var floatByte = ByteBuffer.allocate(LLSYNC_TLV_LENGTH_FLOAT).putFloat(it.toString().toFloat()).array()
                        var tempValue = ByteArray(LLSYNC_TLV_LENGTH_FLOAT)
                        System.arraycopy(floatByte, 0, tempValue, 0, LLSYNC_TLV_LENGTH_FLOAT)
                        var lastTotalValueByteArr = totalValueByteArr.copyOf()
                        totalValueByteArr = ByteArray(lastTotalValueByteArr.size +tempValue.size)
                        System.arraycopy(lastTotalValueByteArr, 0, totalValueByteArr, 0, lastTotalValueByteArr.size)
                        System.arraycopy(tempValue, 0, totalValueByteArr, lastTotalValueByteArr.size, tempValue.size)
                    }
                }
                var structLengthByte = BleConfigService.get().number2Bytes(totalValueByteArr.size.toLong(), LLSYNC_TLV_LENGTH_ARRAY)
                var byteArr = ByteArray(1+LLSYNC_TLV_LENGTH_ARRAY+totalValueByteArr.size)
                byteArr[0] = type.toByte()
                System.arraycopy(structLengthByte, 0, byteArr, 1, LLSYNC_TLV_LENGTH_ARRAY)
                System.arraycopy(totalValueByteArr, 0, byteArr, LLSYNC_TLV_LENGTH_ARRAY+1, totalValueByteArr.size)
                return byteArr
            }
        }
        return ByteArray(0)
    }
}