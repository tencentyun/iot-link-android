package com.auth

import com.auth.message.upload.ActivePushMessage
import com.auth.message.upload.ArrayString
import com.auth.socket.WSClientManager
import com.auth.socket.callback.ActivePushCallback
import com.auth.socket.callback.MessageCallback
import com.kitlink.util.HttpRequest
import com.util.L

/**
 * 长连接管理
 */
object IoTAuth {

    private var expireAt = 0L

    //    var accessToken = ""
//    var appKey = "aBCuYQcbDMGlzZTMU"
    var appKey = HttpRequest.APP_KEY

    /**
     * 初始化WebSocket
     */
    fun init() {
        WSClientManager.instance.init()
    }

    fun isConnected(): Boolean {
        return WSClientManager.instance.isConnected()
    }

    /**
     * 注册监听
     * @param deviceIds 多个设备的deviceId
     * @param callback MessageCallback注册监听请求回调，可为null
     */
    fun registerActivePush(deviceIds: ArrayString, callback: MessageCallback?) {
        val msg = ActivePushMessage(deviceIds)
        L.e("registerActivePush", msg.toString())
        if (callback == null)
            WSClientManager.instance.sendMessage(msg.toString())
        else
            WSClientManager.instance.sendRequestMessage(msg, callback)
        WSClientManager.instance.addDeviceIds(deviceIds)
        WSClientManager.instance.sendHeartMessage()
    }

    /**
     * 添加监听器
     * @param callback ActivePushCallback，设备数据推送到客户端的回调。在调用registerActivePush成功后，
     * 添加有监听器的页面可以收到推送数据，最多30个页面，超出时会移除最先添加的监听器，即先进先出（FIFO）
     */
    fun addActivePushCallback(callback: ActivePushCallback) {
        WSClientManager.instance.addActivePushCallback(callback)
    }

    /**
     * 移除监听器
     */
    fun removeActivePushCallback(deviceIds: ArrayString, callback: ActivePushCallback) {
        WSClientManager.instance.removeActivePushCallback(callback)
        WSClientManager.instance.removeDeviceIds(deviceIds)
    }

    /**
     * 销毁
     */
    fun destroy() {
        WSClientManager.instance.destroy()
    }
}