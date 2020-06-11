package com.tencent.iot.explorer.link.auth.socket.callback

interface ConnectionCallback {
    fun connected()

    fun disconnected()
}