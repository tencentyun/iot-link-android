package com.tencent.iot.explorer.link.kitlink.entity

import android.text.TextUtils

class ManualTask {
    var type = 1   // 1 延时任务  0 设备控制任务  2 通知类型  3 选择手动  4 定时任务  5 场景变化阈值
//    var devName = "" // 用于界面显示的设备别名
    var deviceName = "" // 真正的不可设置的设备名
    var aliasName = ""
    var propertyId = ""
    var taskTip = ""
    var task = ""
    var taskKey = ""
    var hour = 0 // 当 type 为 0 时，该字段生效
    var min = 0 // 当 type 为 0 时，该字段生效
    var pos = -1
    var iconUrl = ""
    var productId = ""
    var actionId = ""
    var workDays = ""
    var workDayType = 0 // 0 执行一次  1 每天  2 工作日  3 周末  4 自定义
    var sceneId = ""
    var condId = ""
    var notificationType = 0 // 消息中心

    fun getAlias(): String {
        return if (TextUtils.isEmpty(aliasName)) {
            deviceName
        } else {
            aliasName
        }
    }
}