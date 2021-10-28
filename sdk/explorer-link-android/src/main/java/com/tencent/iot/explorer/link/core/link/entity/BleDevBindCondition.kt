package com.tencent.iot.explorer.link.core.link.entity

import android.util.Log
import com.tencent.iot.explorer.link.core.link.service.BleConfigService

open class BleDevBindCondition {
    var signInfo = ""
    var deviceName = ""
    var data: ByteArray? = null
        set(value) {
            field = value
            field?.let {
//                if (it[0] != 0x05.toByte() && it[0] != 0x06.toByte()) return@let
                val signInfoByteArr = ByteArray(20)
                System.arraycopy(it, 3, signInfoByteArr, 0, 20)
                BleConfigService.bytesToHex(signInfoByteArr)?.let { signStr ->
                    signInfo = signStr.toLowerCase()
                }

                val devBytesLen = it.size - 23
                val devNameByteArr = ByteArray(devBytesLen)
                System.arraycopy(it, 23, devNameByteArr, 0, devBytesLen)
                deviceName = String(devNameByteArr)
            }
        }

    companion object {
        fun data2BleDevBindCondition(data: ByteArray) : BleDevBindCondition{
            var bleDevBindCondition = BleDevBindCondition()
            bleDevBindCondition.data = data
            return bleDevBindCondition
        }
    }
}