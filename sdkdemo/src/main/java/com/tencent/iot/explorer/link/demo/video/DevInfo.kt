package com.tencent.iot.explorer.link.demo.video

class DevInfo {
    var DeviceName = ""
    var Status = 0
    var DevicePsk = ""
    var LoginTime = 0L
    var ExpireTime = 0L
    var LogLevel = 0
    var Online = 0
    var EnableState =0
    var Channel = 0
    var mjpeg = 0

    set(value) {
        field = value
        Status = field
    }
}