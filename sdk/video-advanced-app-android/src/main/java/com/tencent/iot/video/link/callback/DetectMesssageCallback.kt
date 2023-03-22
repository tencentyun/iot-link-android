package com.tencent.iot.video.link.callback

interface DetectMesssageCallback {
    fun onMessage(version: String, message: String): Boolean
}