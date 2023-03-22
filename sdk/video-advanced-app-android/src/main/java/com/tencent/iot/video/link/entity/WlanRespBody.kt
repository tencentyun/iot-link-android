package com.tencent.iot.video.link.entity

class WlanRespBody {

    var method = ""
    var clientToken = ""
    var timestamp = 0L
    var params: DeviceServerInfo = DeviceServerInfo()
    var code = 0
    var status = ""
}