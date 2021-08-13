package com.tencent.iot.explorer.link.core.link.entity

import android.bluetooth.BluetoothDevice
import android.util.Log
import com.alibaba.fastjson.JSONObject
import java.lang.Exception

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