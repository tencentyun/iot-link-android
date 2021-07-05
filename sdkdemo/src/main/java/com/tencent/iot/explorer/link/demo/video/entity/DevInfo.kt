package com.tencent.iot.explorer.link.demo.video.entity

class DevInfo {
    var deviceName = ""
    var Status = 0
    var DevicePsk = ""
    var CreateTime = 0L
    var FirstOnlineTime = 0L
    var LoginTime = 0L
    var LogLevel = 0
    var Version = ""
    var channel = 0
    var online = 0
    set(value) {
        field = value
        Status = field
    }
}