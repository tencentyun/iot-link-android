package com.tenext.auth.socket.callback

import com.tenext.auth.message.payload.Payload

/**
 * 设备接收到下发数据回调
 */
interface ActivePushCallback {

    /**
     * 网络断开后重连
     */
    fun reconnected()

    /**
     * 成功解析
     */
    fun success(payload: Payload)

    /**
     *  未知的数据解析失败，需要开发者解析
     */
    fun unknown(json: String, errorMessage: String)

}