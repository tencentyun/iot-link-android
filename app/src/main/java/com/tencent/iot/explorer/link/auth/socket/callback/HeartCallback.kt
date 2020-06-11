package com.tencent.iot.explorer.link.auth.socket.callback

import com.tencent.iot.explorer.link.auth.message.resp.HeartMessage

interface HeartCallback {

    fun response(reqId: Int, message: HeartMessage)

}