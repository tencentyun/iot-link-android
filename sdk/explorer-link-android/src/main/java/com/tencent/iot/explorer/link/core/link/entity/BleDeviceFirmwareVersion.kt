package com.tencent.iot.explorer.link.core.link.entity

class BleDeviceFirmwareVersion {
    companion object {

        fun byteArr2BleDeviceFirmwareVersion(byteArray: ByteArray): BleDeviceFirmwareVersion {
            var ret = BleDeviceFirmwareVersion()
            if (byteArray.isEmpty()) return ret
            if (byteArray.get(0) != 0x08.toByte()) return ret

            ret.llsyncVersion = byteArray[3].toInt().toString()
            ret.mtuFlag = (byteArray[4].toInt() and 0x80) shr 7
            ret.mtuSize = (byteArray[4].toInt() and 0x0F shl 8) or (byteArray[5].toInt() and 0xFF)
            ret.versionLen = byteArray[6].toInt()
            val nameByteArr = ByteArray(ret.versionLen)
            System.arraycopy(byteArray, 7, nameByteArr, 0, ret.versionLen)
            ret.version = String(nameByteArr)

            return ret
        }
    }

    var llsyncVersion = "" // 协议版本号
    var mtuFlag = 0  // 是否设置 mtu 当 mtu flag为 1 时，进行 MTU 设置；当 mtu flag 为 0 时，不设置 MTU
    var mtuSize = 0  // mtu 大小
    var versionLen = 0
    var version = ""
}