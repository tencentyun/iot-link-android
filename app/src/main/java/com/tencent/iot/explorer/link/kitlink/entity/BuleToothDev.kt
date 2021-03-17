package com.tencent.iot.explorer.link.kitlink.entity

import com.alibaba.fastjson.JSONArray
import com.alibaba.fastjson.JSONObject
import com.tencent.iot.explorer.link.core.auth.message.upload.ArrayString

class BuleToothDev {
    var name = ""
    var deviceId = ""
    var RSSI = 0
    var advertisData = JSONArray()
    var advertisServiceUUIDs = JSONArray()
    var localName = ""
    var serviceData = JSONObject()
}