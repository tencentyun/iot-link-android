package com.tencent.iot.explorer.link.core.link.listener

import com.tencent.iot.explorer.link.core.link.entity.DeviceInfo

interface SoftAPListener : IotLinkListener {

    fun connectedToWifi(deviceInfo: DeviceInfo)

    fun connectFailed(deviceInfo: DeviceInfo, ssid: String)

}