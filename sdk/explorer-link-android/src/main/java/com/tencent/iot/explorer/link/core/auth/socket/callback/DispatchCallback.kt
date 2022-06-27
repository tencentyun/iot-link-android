package com.tencent.iot.explorer.link.core.auth.socket.callback

import com.tencent.iot.explorer.link.core.auth.message.payload.Payload
import com.tencent.iot.explorer.link.core.auth.message.resp.RespFailMessage
import com.tencent.iot.explorer.link.core.auth.message.resp.RespSuccessMessage

interface DispatchCallback {

    fun yunMessage(reqId: String, json: String, response: RespSuccessMessage)

    fun yunMessageFail(reqId: String, json: String, response: RespFailMessage)

    fun payloadMessage(payload: Payload)

    fun payloadUnknownMessage(json: String, errorMessage: String)

    fun unknownMessage(reqId: String, json: String)

}