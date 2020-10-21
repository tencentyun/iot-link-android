package com.tencent.iot.explorer.link.core.auth.response

import com.tencent.iot.explorer.link.core.auth.entity.DeviceEntity

class DeviceListResponse {
    var Total = 0
    var RequestId = ""
    var DeviceList = ArrayList<DeviceEntity>()
}