package com.tencent.iot.explorer.link.core.auth.socket.callback

interface ConnectionCallback {
    fun connected()

    fun disconnected()
}