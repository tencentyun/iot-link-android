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
                Log.e("XXX", "manufacturerSpecificData --- ${it.size}")
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

    companion object {
        fun covertJson2BleDevice(jsonStr: String): BleDevice? {
            var json: JSONObject? = null
            try {
                json = JSONObject.parseObject(jsonStr)
            } catch (e : Exception) {
                return null
            }

            var ret = BleDevice()
            json?.let {
                if (it.containsKey("productId"))
                    ret.productId = it.getString("productId")
                if (it.containsKey("devName"))
                    ret.devName = it.getString("devName")
            }
            return ret
        }
    }
}