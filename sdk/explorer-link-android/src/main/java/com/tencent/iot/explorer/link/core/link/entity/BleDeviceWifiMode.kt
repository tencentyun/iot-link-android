package com.tencent.iot.explorer.link.core.link.entity

enum class BleDeviceWifiMode(var modeValue : Byte) {
    NULL(0x00.toByte()),
    STA(0x01.toByte());

    fun getValue(): Byte {
        return modeValue
    }
}