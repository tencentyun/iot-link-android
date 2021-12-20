package com.tencent.iot.explorer.link.core.link.service

object LLSyncErrorCode {
    const val WIFI_CONFIG_BROADCAST_PRASE_ERROR_CODE = "-5000" // 广播没有解析出内容，直接返回
    const val WIFI_CONFIG_TIMEOUT_ERROR_CODE = "-5001" // ble设备wifi辅助配网超时。
    const val WIFI_CONFIG_BLE_DISCONNECT_ERROR_CODE = "-5002" // ble设备wifi辅助配网时和ble设备断开连接。
    const val WIFI_CONFIG_BLE_GET_DEVICE_INFO_RESPONSE_ERROR_CODE = "-5003" // ble设备wifi辅助配网时，设备反馈蓝牙设备信息获取失败。
    const val WIFI_CONFIG_BLE_SET_MTU_ERROR_CODE = "-5004" // ble设备wifi辅助配网时，设置MTU失败。
    const val WIFI_CONFIG_BLE_SET_WIFI_MODE_ERROR_CODE = "-5005" // ble设备wifi辅助配网时，设置设备WiFi模式失败。
    const val WIFI_CONFIG_BLE_SET_WIFI_MODE_RESPONSE_ERROR_CODE = "-5006" // ble设备wifi辅助配网时，设备响应设置WiFi模式失败。
    const val WIFI_CONFIG_BLE_SET_WIFI_INFO_ERROR_CODE = "-5007" // ble设备wifi辅助配网时，向设备发送wifi info失败。
    const val WIFI_CONFIG_BLE_REQUEST_WIFI_CONNECT_ERROR_CODE = "-5008" // ble设备wifi辅助配网时，请求设备连接wifi失败。
    const val WIFI_CONFIG_BLE_WIFI_CONNECT_RESPONSE_ERROR_CODE = "-5009" // ble设备wifi辅助配网时，设备连接wifi失败。
    const val WIFI_CONFIG_BLE_REQUEST_BIND_TOKEN_ERROR_CODE = "-5010" // ble设备wifi辅助配网时，请求设备绑定token失败。
    const val WIFI_CONFIG_BLE_BIND_TOKEN_RESPONSE_ERROR_CODE = "-5011" // ble设备wifi辅助配网时，设备绑定token失败。
    const val WIFI_CONFIG_BLE_SET_MTU_RESPONSE_ERROR_CODE = "-5012" // ble设备wifi辅助配网时，设备响应设置MTU失败。
    const val WIFI_CONFIG_BLE_REQUEST_GET_DEVICE_INFO_ERROR_CODE = "-5013" // ble设备wifi辅助配网时，获取蓝牙设备信息失败。
    const val WIFI_CONFIG_BLE_BIND_BLE_DEVICE_NET_ERROR_CODE = "-5014" // ble设备wifi辅助配网时，绑定蓝牙设备接口服务器失败。
    const val WIFI_CONFIG_BLE_BIND_BLE_DEVICE_NET_OTHER_ERROR_CODE = "-5015" // ble设备wifi辅助配网时，绑定蓝牙设备接口其他失败。
    const val WIFI_CONFIG_BLE_BIND_BLE_DEVICE_RESPONSE_ERROR_CODE = "-5016" // ble设备wifi辅助配网时，向蓝牙设备发送绑定设备结果失败。
    const val WIFI_CONFIG_BLE_PARAMS_ERROR_CODE = "-5017" // ble设备wifi辅助配网时，解析数据失败。

    const val PURE_BLE_SET_UNIX_TIMESTAMP_NONCE_ERROR_CODE = "-5100" // ble纯蓝牙绑定时，设置unix失败。

}