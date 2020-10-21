package com.tencent.iot.explorer.link.kitlink.entity

import com.tencent.iot.explorer.link.core.auth.entity.DeviceEntity

class ShareDeviceEntity : DeviceEntity() {
    init {
        this.shareDevice = true
    }
}