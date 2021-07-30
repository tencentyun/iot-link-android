package com.tencent.iot.explorer.link.core.link.entity

class BleDeviceWifiInfo {
    var ssid = ""
    var pwd = ""

    fun formatByteArr(): ByteArray {
        if (ssid == null || pwd == null) {
            return ByteArray(0)
        }

        var byteArr = ByteArray( 5 + ssid.toByteArray().size + pwd.toByteArray().size)
        byteArr[0] = 0xE2.toByte()
        byteArr[1] = ((byteArr.size - 3) / Math.pow(2.0, 8.0).toInt()).toByte()
        byteArr[2] = ((byteArr.size - 3) % Math.pow(2.0, 8.0).toInt()).toByte()
        byteArr[3] = ssid.toByteArray().size.toByte()
        System.arraycopy(ssid.toByteArray(), 0, byteArr, 4, ssid.toByteArray().size)
        byteArr[4 + ssid.toByteArray().size] = pwd.toByteArray().size.toByte()
        System.arraycopy(pwd.toByteArray(), 0, byteArr, 5 + ssid.toByteArray().size, pwd.toByteArray().size)
        return byteArr
    }
}