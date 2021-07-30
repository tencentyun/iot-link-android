package com.tencent.iot.explorer.link.core.link.entity

class BleWifiConnectInfo {
    var wifiMode: BleDeviceWifiMode = BleDeviceWifiMode.NULL
    var connected: Boolean = false
    var softAp = false
    var ssid = ""
    var ssidLen = 0

    companion object {
        fun byteArr2BleWifiConnectInfo(byteArray: ByteArray): BleWifiConnectInfo {
            var ret = BleWifiConnectInfo()
            if (byteArray.isEmpty()) return ret
            if (byteArray.get(0) != 0xE2.toByte()) return ret

            ret.wifiMode = BleDeviceWifiMode.valueOf(byteArray[3].toInt().toByte())
            ret.connected = byteArray[4].toInt() == 0
            ret.ssidLen = byteArray[6].toInt()
            val ssidByteArr = ByteArray(ret.ssidLen)
            System.arraycopy(byteArray, 7, ssidByteArr, 0, ret.ssidLen)
            ret.ssid = String(ssidByteArr)

            return ret
        }
    }
}