package com.tencent.iot.explorer.link.kitlink.response

import com.tencent.iot.explorer.link.kitlink.entity.DeviceProductEntity

/**
 *  设备产品信息响应实体
 */
class DeviceProductResponse {

    var Products = arrayListOf<DeviceProductEntity>()
    var RequestId = ""

}