package com.tencent.iot.explorer.link.core.link.entity

class BleDeviceInfo {
    companion object {

        fun byteArr2BleDeviceInfo(byteArray: ByteArray): BleDeviceInfo {
            var ret = BleDeviceInfo()
            if (byteArray.isEmpty()) return ret
            if (byteArray.get(0) != 0x08.toByte()) return ret

            ret.llsyncVersion = byteArray[3].toInt().toString()
            ret.mtuFlag = if ((byteArray[4].toInt() and 0x80) == 8) 1 else 0
            ret.mtuSize = (byteArray[4].toInt() and 0x02 shl 8) or (byteArray[5].toInt() and 0xFF)
            ret.devNameLen = byteArray[6].toInt()
            val nameByteArr = ByteArray(ret.devNameLen)
            System.arraycopy(byteArray, 7, nameByteArr, 0, ret.devNameLen)
            ret.devName = String(nameByteArr)

            return ret
        }
    }

    var llsyncVersion = "" // 协议版本号
    var mtuFlag = 0  // 是否设置 mtu 当 mtu flag为 1 时，进行 MTU 设置；当 mtu flag 为 0 时，不设置 MTU
    var mtuSize = 0  // mtu 大小
    var devNameLen = 0
    var devName = ""
}