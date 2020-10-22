package com.tencent.iot.explorer.link.kitlink.response

import com.tencent.iot.explorer.link.kitlink.entity.ShareDeviceEntity


class ShareDeviceListResponse {
    var Total = 0
    var RequestId = ""
    var ShareDevices = ArrayList<ShareDeviceEntity>()
}