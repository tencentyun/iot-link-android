package com.tencent.iot.explorer.link.core.auth.message.upload


/**
 * 解绑设备
 */
class HeartMessage(deviceIds: ArrayString) : YunMessage() {

    var deviceIds = deviceIds

    init {
        Action = "AppDeviceTraceHeartBeat"
    }

    override fun toString(): String {
        reqId = 2
        addValue("DeviceIds", deviceIds)
        return super.toString()
    }

}