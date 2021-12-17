package com.tencent.iot.explorer.link.core.link.entity

import com.tencent.iot.explorer.link.core.link.service.BleConfigService
import java.io.ByteArrayInputStream
import java.io.DataInputStream
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class BleDeviceProperty {
    var len = 0
    var data: ByteArray? = null
        set(value) {
            field = value
            field?.let {
                if (it[0] != 0x00.toByte()) return@let

                var size = ((it[1].toInt() and 0xFF) shl 8) or (it[2].toInt() and 0xFF)

                var valueDataByteArr = ByteArray(size)
                System.arraycopy(it, 3, valueDataByteArr, 0, size)
                valueData = valueDataByteArr
            }
        }
    var valueData: ByteArray? = null
        set(value) {
            field = value
            field?.let {
                var index = 0
                while (index < it.size) {
                    val tag = it[index]
                    var id = tag.toInt() and 0x1F
                    index ++
                    var type = tag.toInt() and 0xE0 shr 5
                    when(type) {
                        0 -> { // 布尔
                            boolMap.put(id, it[index].toInt() == 1)
                            index ++
                        }
                        1 -> { // 整数
                            var numByteArray = ByteArray(4)
                            System.arraycopy(it, index, numByteArray, 0, 4)
                            intMap.put(id, bytesToInt(numByteArray))
                            index += 4
                        }
                        2 -> { // 字符串
                            var lenByteArray = ByteArray(2)
                            System.arraycopy(it, index, lenByteArray, 0, 2)
                            index += 2
                            var len = bytesToInt(lenByteArray)
                            var strByteArray = ByteArray(len)
                            System.arraycopy(it, index, strByteArray, 0, len)
                            stringMap.put(id, String(strByteArray))
                            index += len
                        }
                        3 -> { // 浮点数
                            var floatnumByteArray = ByteArray(4)
                            System.arraycopy(it, index, floatnumByteArray, 0, 4)
                            floatMap.put(id, byte2float(floatnumByteArray))
                            index += 4
                        }
                        4 -> { // 枚举
                            var numByteArray = ByteArray(2)
                            System.arraycopy(it, index, numByteArray, 0, 2)
                            enumMap.put(id, bytesToInt(numByteArray))
                            index += 2
                        }
                        5 -> { // 时间
                            var numByteArray = ByteArray(4)
                            System.arraycopy(it, index, numByteArray, 0, 4)
                            timestampMap.put(id, bytesToTimestamp(numByteArray).toLong())
                            index += 4
                        }
                        6 -> { // 结构体
                            var lenByteArray = ByteArray(2)
                            System.arraycopy(it, index, lenByteArray, 0, 2)
                            index += 2
                            var len = bytesToInt(lenByteArray)
                            var structByteArray = ByteArray(len)
                            System.arraycopy(it, index, structByteArray, 0, len)
                            var property = BleDeviceProperty()
                            property.valueData = structByteArray
                            structMap.put(id, property)
                            index += len
                        }
                    }
                }
            }
        }
    var boolMap: HashMap<Int, Boolean> = HashMap()
    var intMap: HashMap<Int, Int> = HashMap()
    var stringMap: HashMap<Int, String> = HashMap()
    var floatMap: HashMap<Int, Float> = HashMap()
    var enumMap: HashMap<Int, Int> = HashMap()
    var timestampMap: HashMap<Int, Long> = HashMap()
    var structMap: HashMap<Int, BleDeviceProperty> = HashMap()

    fun toValueData(): ByteArray {
        var tmpData = toData()
        var retData = ByteArray(tmpData.size - 3)
        System.arraycopy(tmpData, 3, retData, 0, retData.size)
        return retData
    }

    fun toData(): ByteArray {

        var byteList = ArrayList<Byte>()
        byteList.add(0x00.toByte())
        byteList.add(0x00.toByte())
        byteList.add(0x00.toByte())
        for (key in boolMap.keys) {
            var header = 0 and 0x07 shl 5 or (key and 0x1F)
            var value = if (boolMap.get(key) == true) 1 else 0
            byteList.add(header.toByte())
            byteList.add(value.toByte())
        }
        for (key in intMap.keys) {
            intMap.get(key)?.let {
                var header = 1 and 0x07 shl 5 or (key and 0x1F)
                var valueByteArr = BleConfigService.get().number2Bytes(it.toLong(), 4)
                byteList.add(header.toByte())
                for (i in 0 until valueByteArr.size) {
                    byteList.add(valueByteArr[i])
                }
            }
        }

        for (key in stringMap.keys) {
            stringMap.get(key)?.let {
                var header = 2 and 0x07 shl 5 or (key and 0x1F)
                var value = it.toByteArray()
                var lenBytes = BleConfigService.get().number2Bytes(value.size.toLong(), 2)
                var valueByteArray = ByteArray(2 + value.size)
                System.arraycopy(lenBytes, 0, valueByteArray, 0, 2)
                System.arraycopy(value, 0, valueByteArray, 2, value.size)
                byteList.add(header.toByte())
                for (i in 0 until valueByteArray.size) {
                    byteList.add(valueByteArray[i])
                }
            }
        }

        for (key in floatMap.keys) {
            floatMap.get(key)?.let {
                var header = 3 and 0x07 shl 5 or (key and 0x1F)
                var valueByteArr = getBytes(it)
                byteList.add(header.toByte())
                for (i in 0 until valueByteArr.size) {
                    byteList.add(valueByteArr[i])
                }
            }
        }

        for (key in enumMap.keys) {
            enumMap.get(key)?.let {
                var header = 4 and 0x07 shl 5 or (key and 0x1F)
                var valueByteArr = BleConfigService.get().number2Bytes(it.toLong(), 2)
                byteList.add(header.toByte())
                for (i in 0 until valueByteArr.size) {
                    byteList.add(valueByteArr[i])
                }
            }
        }

        for (key in timestampMap.keys) {
            timestampMap.get(key)?.let {
                var header = 5 and 0x07 shl 5 or (key and 0x1F)
                var valueByteArr = BleConfigService.get().number2Bytes(it, 4)
                byteList.add(header.toByte())
                for (i in 0 until valueByteArr.size) {
                    byteList.add(valueByteArr[i])
                }
            }
        }

        for (key in structMap.keys) {
            structMap.get(key)?.let {
                var header = 6 and 0x07 shl 5 or (key and 0x1F)
                var valueByteArr = it.toData()
                var lenBytes = BleConfigService.get().number2Bytes(valueByteArr.size.toLong(), 2)
                var valueByteArray = ByteArray(2 + valueByteArr.size)
                System.arraycopy(lenBytes, 0, valueByteArray, 0, 2)
                System.arraycopy(valueByteArr, 0, valueByteArray, 2, valueByteArr.size)
                byteList.add(header.toByte())
                for (i in 0 until valueByteArray.size) {
                    byteList.add(valueByteArray[i])
                }
            }
        }
        var retByteArr = ByteArray(byteList.size)
        for (i in 0 until byteList.size) {
            retByteArr[i] = byteList[i]
        }

        var sizeBytes = BleConfigService.get().number2Bytes((retByteArr.size - 3).toLong(), 2)
        System.arraycopy(sizeBytes, 0, retByteArr, 1, 2)
        return retByteArr
    }

    companion object {
        fun data2BleDeviceProperty(data: ByteArray) : BleDeviceProperty {
            var bleDeviceProperty = BleDeviceProperty()
            bleDeviceProperty.data = data
            return bleDeviceProperty
        }

        fun bytesToInt(bs: ByteArray): Int {
            var num = 0
            for (i in bs.indices.reversed()) {
                num += (bs[i] * Math.pow(255.0, (bs.size - i - 1).toDouble())).toInt()
            }
            return num
        }

        fun bytesToTimestamp(bs: ByteArray): Int {
            return ((bs[0]+0 and 0xFF shl 24) or (bs[1]+0 and 0xFF shl 16) or (bs[2]+0 and 0xFF shl 8) or (bs[3]+0 and 0xFF))
        }

        fun byte2float(b: ByteArray): Float {
            var tmp: Int
            tmp = b[0].toInt()
            tmp = tmp and 0xff
            tmp = tmp or (b[1].toLong() shl 8).toInt()
            tmp = tmp and 0xffff
            tmp = tmp or (b[2].toLong() shl 16).toInt()
            tmp = tmp and 0xffffff
            tmp = tmp or (b[3].toLong() shl 24).toInt()
            return java.lang.Float.intBitsToFloat(tmp)
        }


        fun getBytes(data: Float): ByteArray {
            val intBits = java.lang.Float.floatToIntBits(data)
            return BleConfigService.get().number2Bytes(intBits.toLong(), 4)
        }
    }
}