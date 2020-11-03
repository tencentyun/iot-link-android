package com.tencent.iot.explorer.link.kitlink.entity

class ManualTask {
    var type = 0   // 0 延时任务  1 设备控制任务
    var devName = ""
    var taskTip = ""
    var task = ""
    var hour = 0 // 当 type 为 0 时，该字段生效
    var min = 0 // 当 type 为 0 时，该字段生效
    var pos = -1
}