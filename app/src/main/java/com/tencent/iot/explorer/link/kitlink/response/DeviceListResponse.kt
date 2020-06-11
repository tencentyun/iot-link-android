package com.tencent.iot.explorer.link.kitlink.response

import com.tencent.iot.explorer.link.kitlink.entity.DeviceEntity

class DeviceListResponse {
    var Total = 0
    var RequestId = ""
    var DeviceList = ArrayList<DeviceEntity>()
}