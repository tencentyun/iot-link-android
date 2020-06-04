package com.tenext.auth.socket.callback

import com.tenext.auth.message.resp.HeartMessage

interface HeartCallback {

    fun response(reqId: Int, message: HeartMessage)

}