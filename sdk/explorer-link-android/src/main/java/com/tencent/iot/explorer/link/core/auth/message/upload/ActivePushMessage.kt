package com.tencent.iot.explorer.link.core.auth.message.upload

import com.tencent.iot.explorer.link.core.auth.message.MessageConst
import com.tencent.iot.explorer.link.core.log.L


class ActivePushMessage(deviceIds: ArrayString) : UploadMessage() {

    init {
        action = "ActivePush"
        commonParams["DeviceIds"] = deviceIds
    }

    override fun toString(): String {
        reqId = MessageConst.ACTIVE_PUSH_REQ_ID
        val res = super.toString()
        L.d("ActivePushMessage to string res:$res")
        return res
    }
}
