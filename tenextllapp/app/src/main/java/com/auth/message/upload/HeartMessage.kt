package com.auth.message.upload

import com.auth.message.MessageConst

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
        addValue(MessageConst.DEVICE_IDS, deviceIds)
        return super.toString()
    }

}