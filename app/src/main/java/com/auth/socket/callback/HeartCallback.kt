package com.auth.socket.callback

import com.auth.message.resp.HeartMessage

interface HeartCallback {

    fun response(reqId: Int, message: HeartMessage)

}