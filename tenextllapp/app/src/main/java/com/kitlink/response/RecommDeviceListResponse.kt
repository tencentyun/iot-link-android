package com.kitlink.response

import com.kitlink.entity.RecommDeviceEntity

class RecommDeviceListResponse {
    var RequestId = ""
    var CategoryList = arrayListOf<RecommDeviceEntity>()
}