package com.tencent.iot.video.link.callback

interface XP2PCallback {
    fun fail(msg: String?, errorCode: Int)
}