package com.tencent.iot.video.link.entity

import android.text.TextUtils

class DeviceServerInfo {
    var deviceName = ""
    var address = ""
    var port = 0

    fun isReady(): Boolean {
        if (TextUtils.isEmpty(address)) {
            return false
        }

        if (TextUtils.isEmpty(deviceName)) {
            return false
        }

        if (port <= 0) {
            return false
        }

        return true
    }
}