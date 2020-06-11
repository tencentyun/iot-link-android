package com.tencent.iot.explorer.link.kitlink.response

import com.tencent.iot.explorer.link.kitlink.entity.ProductConfigEntity

class ProductsConfigResponse {
    var RequestId = ""
    var Data = arrayListOf<ProductConfigEntity>()
}