package com.tencent.iot.explorer.link.kitlink.entity

class ManualTask {
    var type = 1   // 1 延时任务  0 设备控制任务
    var devName = "" // 用于界面显示的设备别名
    var deviceName = "" // 真正的不可设置的设备名s
    var taskTip = ""
    var task = ""
    var taskKey = ""
    var hour = 0 // 当 type 为 0 时，该字段生效
    var min = 0 // 当 type 为 0 时，该字段生效
    var pos = -1
    var iconUrl = ""
    var productId = ""
    var actionId = ""
}