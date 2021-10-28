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
    var indexWithDevname = true  // 默认需要同时匹配设备名
    var boundState = 0 // 0 未绑定  1 绑定中  2 已绑定
    var mac = ""
    var type = 0 // 1 纯蓝牙协议   0 蓝牙辅助配网
    var bindTag = ""

    override fun equals(other: Any?): Boolean {
        if (other is BleDevice) {
            return if (this.indexWithDevname == other.indexWithDevname && other.indexWithDevname) {
                this.devName == other.devName && this.productId == other.productId
            } else {
                this.productId == other.productId
            }
        }
        return false
    }
}