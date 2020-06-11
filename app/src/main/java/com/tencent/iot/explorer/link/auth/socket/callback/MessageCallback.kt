package com.tencent.iot.explorer.link.auth.socket.callback

import com.tencent.iot.explorer.link.auth.message.resp.RespFailMessage
import com.tencent.iot.explorer.link.auth.message.resp.RespSuccessMessage

/**
 * JWebSocketClient消息发送回调
 */
interface MessageCallback {

    fun success(reqId: Int, message: String, response: RespSuccessMessage)

    fun fail(reqId: Int, message: String, response: RespFailMessage)

    fun unknownMessage(reqId: Int, message: String)
}