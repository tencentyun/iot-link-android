package com.tencent.iot.explorer.link.core.auth.response

import com.tencent.iot.explorer.link.core.auth.entity.Product

/**
 *  设备产品信息响应实体
 */
class DeviceProductResponse {

    var Products = arrayListOf<Product>()
    var RequestId = ""

}