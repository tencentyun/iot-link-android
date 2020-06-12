package com.tencent.iot.explorer.link.core.auth.socket.callback

import com.tencent.iot.explorer.link.core.auth.message.resp.HeartMessage

interface HeartCallback {

    fun response(reqId: Int, message: HeartMessage)

}