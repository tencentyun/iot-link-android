package com.tencent.iot.explorer.link.core.link.entity

enum class BleConfigStep {
    /**
     * 开始配网
     */
    STEP_LINK_START,
    /**
     * 连接蓝牙设备
     */
    STEP_CONNECT_BLE_DEV,
    /**
     * 设备 wifi 方式
     */
    STEP_SET_WIFI_MODE,
    /**
     * 发送 WIFI 信息
     */
    STEP_SEND_WIFI_INFO,
    /**
     * 设备成功连接到wifi
     */
    STEP_DEVICE_CONNECTED_TO_WIFI,
    /**
     * 发送 token 信息
     */
    STEP_SEND_TOKEN,
    /**
     * 配网成功(包括配网、绑定)
     */
    STEP_LINK_SUCCESS
}