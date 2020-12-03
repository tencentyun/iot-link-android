package com.tencent.iot.explorer.link.core.auth.socket.callback

import com.tencent.iot.explorer.link.core.auth.message.payload.Payload

interface PayloadMessageCallback {

    fun payloadMessage(payload: Payload)
}