package com.tencent.iot.explorer.link.kitlink.entity

import com.tencent.iot.explorer.link.core.auth.entity.DeviceEntity
import com.tencent.iot.explorer.link.core.auth.entity.ProductUIDevShortCutConfig

class DeviceDetails: DeviceEntity() {
    var productUIDevShortCutConfig: ProductUIDevShortCutConfig? = null
}