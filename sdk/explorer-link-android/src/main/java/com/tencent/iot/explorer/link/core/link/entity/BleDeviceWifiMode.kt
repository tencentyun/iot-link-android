package com.tencent.iot.explorer.link.core.link.entity

enum class BleDeviceWifiMode(var modeValue : Byte) {
    NULL(0x00.toByte()),
    STA(0x01.toByte());

    fun getValue(): Byte {
        return modeValue
    }

    companion object {
        fun valueOf(modeValue : Byte) : BleDeviceWifiMode{
            if (modeValue == 0x01.toByte()) {
                return STA
            }
            return NULL
        }
    }
}