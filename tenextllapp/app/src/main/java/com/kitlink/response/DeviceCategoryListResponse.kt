package com.kitlink.response

import com.kitlink.entity.DeviceCategoryEntity

class DeviceCategoryListResponse {
    var RequestId = ""
    var List = arrayListOf<DeviceCategoryEntity>()
}