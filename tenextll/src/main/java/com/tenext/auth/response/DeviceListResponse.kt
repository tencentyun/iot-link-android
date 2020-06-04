package com.tenext.auth.response

import com.tenext.auth.entity.Device

class DeviceListResponse {
    var Total = 0
    var RequestId = ""
    var DeviceList = ArrayList<Device>()
}