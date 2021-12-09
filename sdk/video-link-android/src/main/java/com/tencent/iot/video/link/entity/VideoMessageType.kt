package com.tencent.iot.video.link.entity

enum class VideoMessageType(value: Int) {
    DETECT_BODY(1), // 探测消息
    DETECT_RESP_BODY(2); // 探测响应消息

    private var value = 0

    init {
        this.value = value
    }

    fun getValue(): Int {
        return this.value
    }
}