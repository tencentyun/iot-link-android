package com.auth.socket.callback

import com.auth.message.payload.Payload
import com.auth.message.resp.RespFailMessage
import com.auth.message.resp.RespSuccessMessage

interface ActivePushCallback {

    /**
     * 网络断开后重连
     */
    fun reconnected()

    fun success(payload: Payload)

    fun unknown(json: String, errorMessage: String)

}