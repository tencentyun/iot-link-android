package com.tencent.iot.explorer.link.kitlink.entity

import com.tencent.iot.explorer.link.kitlink.util.DateUtils
import java.text.SimpleDateFormat
import java.util.*

class LogMessage {
    var automationId = ""
    var automationName = ""
    var familyId = ""
    var userId = ""
    var result = ""
    var resultCode = 0
    var actionResults: MutableList<ActionResult>? = null
    var createAt = ""
    set(value) {
        field = value

        var timeStr = DateUtils.utc2Local(field)
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        val date: Date = sdf.parse(timeStr)
        this.day = date.date.toString()
        this.mouth = (date.month + 1).toString()
        this.year = (date.year + 1970).toString()
        this.time = String.format("%02d:%02d:%02d", date.hours, date.minutes, date.seconds)
    }
    var msgId = ""
    var sceneId = ""
    var sceneName = ""
    var day = ""
    var mouth = ""
    var year = ""
    var time = ""
    var opened = false
}