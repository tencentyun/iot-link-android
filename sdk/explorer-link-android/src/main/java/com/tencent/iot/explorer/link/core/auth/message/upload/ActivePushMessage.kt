package com.tencent.iot.explorer.link.core.auth.message.upload

import com.tencent.iot.explorer.link.core.log.L


class ActivePushMessage(deviceIds: ArrayString) : UploadMessage() {

    init {
        action = "ActivePush"
        commonParams["DeviceIds"] = deviceIds
    }

    override fun toString(): String {
        reqId = 1
        val res = super.toString()
        L.d("ActivePushMessage to string res:$res")
        return res
    }
}