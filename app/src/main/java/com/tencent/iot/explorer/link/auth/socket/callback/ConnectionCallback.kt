package com.auth.socket.callback

interface ConnectionCallback {
    fun connected()

    fun disconnected()
}