package com.tenext.auth.socket.callback

import com.tenext.auth.message.payload.Payload
import com.tenext.auth.message.resp.RespFailMessage
import com.tenext.auth.message.resp.RespSuccessMessage

interface DispatchCallback {

    fun yunMessage(reqId: Int, json: String, response: RespSuccessMessage)

    fun yunMessageFail(reqId: Int, json: String, response: RespFailMessage)

    fun payloadMessage(payload: Payload)

    fun payloadUnknownMessage(json: String, errorMessage: String)

    fun unknownMessage(reqId: Int,json: String)

}