package com.auth.socket.callback

import com.auth.message.resp.RespFailMessage
import com.auth.message.resp.RespSuccessMessage

/**
 * JWebSocketClient消息发送回调
 */
interface MessageCallback {

    fun success(reqId: Int, message: String, response: RespSuccessMessage)

    fun fail(reqId: Int, message: String, response: RespFailMessage)

    fun unknownMessage(reqId: Int, message: String)
}