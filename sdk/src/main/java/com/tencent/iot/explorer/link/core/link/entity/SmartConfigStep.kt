package com.tencent.iot.explorer.link.core.link.entity

enum class SmartConfigStep {
    /**
     * 开始配网
     */
    STEP_LINK_START,
    /**
     * 正在配网
     */
    STEP_DEVICE_CONNECTING,
    /**
     * 发送wifi信息给设备
     */
    STEP_SEND_WIFI_INFO,
    /**
     * 设备成功连接到wifi
     */
    STEP_DEVICE_CONNECTED_TO_WIFI,
    /**
     * 从设备端获取到设备信息
     */
    STEP_GOT_DEVICE_INFO,
    /**
     * 开始绑定
     */
    STEP_DEVICE_BOUND,
    /**
     * 配网成功(包括配网、绑定)
     */
    STEP_LINK_SUCCESS
}