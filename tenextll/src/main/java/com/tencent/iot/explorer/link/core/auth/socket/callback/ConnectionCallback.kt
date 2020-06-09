package com.tenext.auth.socket.callback

interface ConnectionCallback {
    fun connected()

    fun disconnected()
}