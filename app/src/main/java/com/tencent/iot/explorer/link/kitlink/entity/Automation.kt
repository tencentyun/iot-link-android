package com.tencent.iot.explorer.link.kitlink.entity

import com.alibaba.fastjson.JSONArray

class Automation {
    var AutomationId = ""
    var Icon = ""
    var Name = ""
    var Status = 0
    var type = 0 // 0 手动   1 自动
    var desc = "" // x 个设备
    var actions: JSONArray? = null
    var id = ""
}