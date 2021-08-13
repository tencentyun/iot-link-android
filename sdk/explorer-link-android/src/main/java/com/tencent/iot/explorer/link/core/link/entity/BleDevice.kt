package com.tencent.iot.explorer.link.core.link.entity

import android.bluetooth.BluetoothDevice

class BleDevice {
    var url = ""
    var productName = ""
    var devName = ""
    var blueDev : BluetoothDevice? = null

    override fun equals(other: Any?): Boolean {
        if (other is BleDevice) {
            return this.devName == other.devName && this.productName == other.productName
        }
        return false
    }
}