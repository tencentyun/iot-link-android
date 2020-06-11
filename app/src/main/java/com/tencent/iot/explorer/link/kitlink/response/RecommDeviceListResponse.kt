package com.tencent.iot.explorer.link.kitlink.response

import com.tencent.iot.explorer.link.kitlink.entity.CategoryDeviceEntity
import com.tencent.iot.explorer.link.kitlink.entity.RecommDeviceEntity

class RecommDeviceListResponse {
    var RequestId = ""
    var CategoryList = arrayListOf<CategoryDeviceEntity>()
    var ProductList = arrayListOf<RecommDeviceEntity>()
}