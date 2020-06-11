package com.tencent.iot.explorer.link.auth.socket.callback

import com.tencent.iot.explorer.link.auth.message.payload.Payload

interface ActivePushCallback {

    /**
     * 网络断开后重连
     */
    fun reconnected()

    fun success(payload: Payload)

    fun unknown(json: String, errorMessage: String)

}