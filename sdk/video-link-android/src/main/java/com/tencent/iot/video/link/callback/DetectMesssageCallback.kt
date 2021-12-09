package com.tencent.iot.video.link.callback

interface DetectMesssageCallback {
    fun onMessage(message: String): Boolean
}