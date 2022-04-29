package com.tencent.iot.video.link.entity

class DeviceStatus {
    // 0   接收请求
    // 1   拒绝请求
    // 404 error request message
    // 405 connect number too many
    // 406 current command don't support
    // 407 device process error
    var status = 0
    var appConnectNum = 0
}