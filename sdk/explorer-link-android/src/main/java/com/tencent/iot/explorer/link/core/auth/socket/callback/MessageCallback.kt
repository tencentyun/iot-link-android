package com.tencent.iot.explorer.link.core.auth.socket.callback

import com.tencent.iot.explorer.link.core.auth.message.resp.RespFailMessage
import com.tencent.iot.explorer.link.core.auth.message.resp.RespSuccessMessage

interface MessageCallback {

    fun success(reqId: String, message: String, response: RespSuccessMessage)

    fun fail(reqId: String, message: String, response: RespFailMessage)

    fun unknownMessage(reqId: String, message: String)

}