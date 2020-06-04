package com.tenext.link.listener

import com.tenext.link.entity.DeviceInfo

interface SoftAPListener : IotLinkListener {

    fun connectedToWifi(deviceInfo: DeviceInfo)

    fun connectFailed(deviceInfo: DeviceInfo, ssid: String)

}