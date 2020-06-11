package com.tencent.iot.explorer.link.kitlink.response

import com.tencent.iot.explorer.link.kitlink.entity.DeviceCategoryEntity

class DeviceCategoryListResponse {
    var RequestId = ""
    var List = arrayListOf<DeviceCategoryEntity>()
}