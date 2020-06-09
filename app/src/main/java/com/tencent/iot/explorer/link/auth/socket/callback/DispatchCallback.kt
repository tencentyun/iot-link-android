package com.auth.socket.callback

import com.auth.message.payload.Payload
import com.auth.message.resp.RespFailMessage
import com.auth.message.resp.RespSuccessMessage

interface DispatchCallback {

    fun yunMessage(reqId: Int, message: String, response: RespSuccessMessage)

    fun yunMessageFail(reqId: Int, message: String, response: RespFailMessage)

    fun payloadMessage(payload: Payload)

    fun payloadUnknownMessage(json: String, errorMessage: String)

    fun unknownMessage(reqId: Int,json: String)

}