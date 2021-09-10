package com.tencent.iot.explorer.link.core.auth.entity

class VirtualBindDeviceList {
    var requestId = ""
    var totalCount = 0
    var virtualBindDeviceList: MutableList<VirtualBindDevice> = ArrayList()
}