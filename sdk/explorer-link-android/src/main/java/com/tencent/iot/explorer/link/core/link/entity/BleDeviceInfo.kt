package com.tencent.iot.explorer.link.core.link.entity

class BleDeviceInfo {
    var llsyncVersion = "" // 协议版本号
    var mtuFlag = 0  // 是否设置 mtu 当 mtu flag为 1 时，进行 MTU 设置；当 mtu flag 为 0 时，不设置 MTU
    var mtuSize = 0  // mtu 大小
    var devNameLen = 0
    var devName = ""
}