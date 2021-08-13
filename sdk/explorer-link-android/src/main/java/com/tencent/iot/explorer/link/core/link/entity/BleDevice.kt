package com.tencent.iot.explorer.link.core.link.entity

import android.bluetooth.BluetoothDevice

class BleDevice {
    var url = ""
    var productId = ""
    var productName = ""
    var devName = ""
    var manufacturerSpecificData: ByteArray? = null
        set(value) {
            field = value
            field?.let {
                if (it.size != 17) return@let
                if (it[0] != 0x02.toByte()) return@let

                val productByteArr = ByteArray(10)
                System.arraycopy(it, 7, productByteArr, 0, 10)
                productId = String(productByteArr)
            }
        }
    var blueDev : BluetoothDevice? = null

    override fun equals(other: Any?): Boolean {
        if (other is BleDevice) {
            return this.devName == other.devName && this.productId == other.productId
        }
        return false
    }
}