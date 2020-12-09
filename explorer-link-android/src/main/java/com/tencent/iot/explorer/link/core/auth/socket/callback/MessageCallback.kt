package com.tencent.iot.explorer.link.core.auth.socket.callback

import com.tencent.iot.explorer.link.core.auth.message.resp.RespFailMessage
import com.tencent.iot.explorer.link.core.auth.message.resp.RespSuccessMessage

interface MessageCallback {

    fun success(reqId: Int, message: String, response: RespSuccessMessage)

    fun fail(reqId: Int, message: String, response: RespFailMessage)

    fun unknownMessage(reqId: Int, message: String)

}