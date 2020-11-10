package com.tencent.iot.explorer.link.kitlink.entity

import com.alibaba.fastjson.JSONArray
import com.tencent.iot.explorer.link.customview.dialog.WorkTimeMode

class AutomicTaskEntity {
    var name = ""
    var icon = ""
    var familyId = ""
    var actionsJson: JSONArray? = null
    var conditionsJson: JSONArray? = null
    var status = 0
    var matchType = 0
    var conditions: MutableList<ManualTask> = ArrayList()
    var tasks: MutableList<ManualTask> = ArrayList()
    var workTimeMode = WorkTimeMode()

}