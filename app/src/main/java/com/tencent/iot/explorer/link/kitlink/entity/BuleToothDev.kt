package com.tencent.iot.explorer.link.kitlink.entity

import android.bluetooth.BluetoothDevice
import com.alibaba.fastjson.JSONArray
import com.alibaba.fastjson.JSONObject

class BuleToothDev {
    var name = ""
    var deviceId = ""
    var RSSI = 0
    var advertisData = JSONArray()
    var advertisServiceUUIDs = JSONArray()
    var localName = ""
    var serviceData = JSONObject()
    var dev: BluetoothDevice? = null
}