package com.tencent.iot.explorer.link.core.link.entity

class BleDevOtaUpdateResponse {
    companion object {
        fun byteArr2BleDevOtaUpdateResponse(byteArray: ByteArray): BleDevOtaUpdateResponse {
            var instance = BleDevOtaUpdateResponse()
            if (byteArray.isEmpty()) return instance
            if (byteArray.get(0) != 0x09.toByte()) return instance
            instance.allowUpdate = (byteArray[3].toInt() and 0x01) == 1
            instance.allowSupportBreakpointResume = ((byteArray[3].toInt() and 0x02) shr 1) == 1
            if (instance.allowUpdate) { //允许升级和禁止升级的payload含义不同。
                if (byteArray.size > 4) instance.totalPackageNumbers = byteArray[4].toInt() and 0xFF
                if (byteArray.size > 5) instance.packageLength = byteArray[5].toInt() and 0xFF
                if (byteArray.size > 6) instance.dataRetryTime = byteArray[6].toInt() and 0xFF
                if (byteArray.size > 7) instance.deviceRebootTime = byteArray[7].toInt() and 0xFF
                if (byteArray.size > 11) {
                    val lastReceivedFileSizeByteArr = ByteArray(4)
                    System.arraycopy(byteArray, 8, lastReceivedFileSizeByteArr, 0, 4)
                    instance.lastReceivedFileSize = bytesToInt(lastReceivedFileSizeByteArr)
                }
                if (byteArray.size > 12) instance.packageSendInterval = byteArray[12].toInt() and 0xFF
            } else {
                val errorCodeByteArr = ByteArray(byteArray.size - 4)
                System.arraycopy(byteArray, 3, errorCodeByteArr, 0, errorCodeByteArr.size)
                instance.errorCode = bytesToInt(errorCodeByteArr)
            }
            return instance
        }

        fun bytesToInt(bs: ByteArray): Int {
            var num = 0
            for (i in bs.indices.reversed()) {
                num += (bs[i] * Math.pow(255.0, (bs.size - i - 1).toDouble())).toInt()
            }
            return num
        }
    }
    var allowUpdate = false // 是否允许升级
    var allowSupportBreakpointResume = false // 是否支持断点续传
    var totalPackageNumbers = 0 // 单次循环中可以连续传输的数据包个数， 取值范围 0x00 ~ 0xFF。  0 ~ 255   1byte
    var packageLength = 0 // 单个数据包大小，取值范围 0x00 ~ 0xF0。  0 ~ 240   1byte
    var dataRetryTime = 0 // 数据包的超时重传周期，单位:秒   1byte
    var deviceRebootTime = 0 // 设备重启最大时间，单位:秒   1byte
    var lastReceivedFileSize = 0 // 断点续传前已接收文件大小   4byte
    var packageSendInterval = 0 // 小程序连续两个数据包的发包间隔，单位:秒   1byte
    var errorCode = 0 // 禁止升级时 错误码 2 设备电量不足  3 版本号错误
}