package com.tencent.iot.explorer.link.core.link.entity

open class BleDevDynregInfo {
    var sign = ""
    var deviceName = ""
    var data: ByteArray? = null
        set(value) {
            field = value
            field?.let {

                val devNameLen = it[3].toInt()
                val devNameByteArr = ByteArray(devNameLen)
                System.arraycopy(it, 4, devNameByteArr, 0, devNameLen)
                deviceName = String(devNameByteArr)

                val signByteArr = ByteArray(it.size - devNameLen - 4)
                System.arraycopy(it, 4 + devNameLen, signByteArr, 0, it.size - devNameLen - 4)
                sign = String(signByteArr)
            }
        }

    companion object {
        fun data2BleDevDynregInfo(data: ByteArray) : BleDevDynregInfo{
            var bleDevDynregInfo = BleDevDynregInfo()
            bleDevDynregInfo.data = data
            return bleDevDynregInfo
        }
    }
}