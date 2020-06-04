package com.tenext.auth.socket.callback

import com.tenext.auth.message.resp.RespFailMessage
import com.tenext.auth.message.resp.RespSuccessMessage

interface MessageCallback {

    fun success(reqId: Int, message: String, response: RespSuccessMessage)

    fun fail(reqId: Int, message: String, response: RespFailMessage)

    fun unknownMessage(reqId: Int, message: String)

}