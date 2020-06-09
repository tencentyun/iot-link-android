package com.kitlink.response

import com.kitlink.entity.DeviceEntity

class DeviceListResponse {
    var Total = 0
    var RequestId = ""
    var DeviceList = ArrayList<DeviceEntity>()
}