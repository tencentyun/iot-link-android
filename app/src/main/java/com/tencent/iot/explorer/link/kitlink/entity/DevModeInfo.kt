package com.tencent.iot.explorer.link.kitlink.entity

import com.alibaba.fastjson.JSONObject

class DevModeInfo {
    var id = ""
    var name = ""
    var value = ""
    var key = ""
    var desc = ""
    var required = false
    var mode = ""
    var define: JSONObject? = null
}