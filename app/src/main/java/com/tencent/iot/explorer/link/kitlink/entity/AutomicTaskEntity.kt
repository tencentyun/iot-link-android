package com.tencent.iot.explorer.link.kitlink.entity

import com.alibaba.fastjson.JSONArray
import com.tencent.iot.explorer.link.customview.dialog.entity.WorkTimeMode

class AutomicTaskEntity {
    var automationId = ""
    var name = ""
    var icon = ""
    var familyId = ""
    var conditions: JSONArray? = null
    var actions: JSONArray? = null
    var status = 0
    var matchType = 0
    var conditionsItem: MutableList<ManualTask> = ArrayList()
    var tasksItem: MutableList<ManualTask> = ArrayList()
    var workTimeMode = WorkTimeMode()
    var effectiveBeginTime = ""
    var effectiveEndTime = ""
    var effectiveDays = ""
}