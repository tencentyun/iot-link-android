package com.auth.socket

import android.text.TextUtils
import com.tencent.iot.explorer.link.core.auth.message.payload.Payload
import com.tencent.iot.explorer.link.core.auth.message.resp.RespFailMessage
import com.tencent.iot.explorer.link.core.auth.message.resp.RespSuccessMessage
import com.tencent.iot.explorer.link.core.auth.message.upload.IotMsg
import com.tencent.iot.explorer.link.core.auth.socket.DispatchMsgHandler
import com.tencent.iot.explorer.link.core.auth.socket.JWebSocketClient1
import com.tencent.iot.explorer.link.core.auth.socket.callback.ActivePushCallback
import com.tencent.iot.explorer.link.core.auth.socket.callback.DispatchCallback
import com.tencent.iot.explorer.link.core.auth.socket.callback.MessageCallback
import com.tencent.iot.explorer.link.core.auth.socket.entity.RequestEntity
import com.tencent.iot.explorer.link.core.auth.util.WifiUtil
import com.tencent.iot.explorer.link.core.log.L
import java.net.URI
import java.util.*

/**
 * JWebsocketClient管理
 */
internal class WSClientManager1 private constructor() {

    private var hasListener = false
    //标记是否可以重新创建
    private var client: JWebSocketClient1? = null
    private var networkStateThread: Thread? = null
    @Volatile
    private var requestSuq = 0 //请求序号

    //消息响应分发
    private val handler = DispatchMsgHandler()

    private val host = "wss://iot.cloud.tencent.com/ws/explorer"

    private val messageList = LinkedList<String>()
    private val requestQueue = LinkedList<RequestEntity>()
    private val confirmQueue = LinkedList<RequestEntity>()

    private val activePushCallbacks = LinkedList<ActivePushCallback?>()

    /**
     * 单例
     */
    companion object {

        private var debugTag = ""

        val INSTANCE: WSClientManager1 by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
            WSClientManager1()
        }

        /**
         * 设置日志tag
         */
        fun setDebugTag(tag: String) {
            debugTag = if (TextUtils.isEmpty(tag)) {
                tag
            } else {
                "?uin=$tag"
            }
        }
    }

    /**
     * 响应数据解析回调
     */
    private val callback = object : DispatchCallback {
        override fun yunMessage(reqId: Int, message: String, response: RespSuccessMessage) {
            getRequestEntity(reqId)?.run {
                confirmQueue.remove(this)
                messageCallback?.success(reqId, message, response)
            }
        }

        override fun yunMessageFail(reqId: Int, message: String, response: RespFailMessage) {
            getRequestEntity(reqId)?.run {
                confirmQueue.remove(this)
                messageCallback?.fail(reqId, message, response)
            }
        }

        override fun payloadMessage(payload: Payload) {
            activePushCallbacks.forEach {
                it?.success(payload)
            }
        }

        override fun payloadUnknownMessage(json: String, errorMessage: String) {
            activePushCallbacks.forEach {
                it?.unknown(json, errorMessage)
            }
        }

        override fun unknownMessage(reqId: Int, json: String) {
            getRequestEntity(reqId)?.run {
                confirmQueue.remove(this)
                messageCallback?.unknownMessage(reqId, json)
            }
        }
    }

    init {
        handler.dispatchCallback = callback
        createSocketClient()
        L.d("The SDK initialized successfully")
    }

    /**
     * 创建JWebSocketClient
     */
    @Synchronized
    private fun createSocketClient() {
        val myHost = if (debugTag.isNotEmpty())
            host + debugTag
        else
            host
        //创建WebSocket
        client = JWebSocketClient1(URI(myHost), handler)
        client!!.connect()
    }

    /**
     * 注册网络重连监听
     */
    private fun registerNetworkListener() {
        if (networkStateThread == null) {
            hasListener = true
            networkStateThread = Thread(NetworkStateRunnable())
            networkStateThread?.start()
        }
    }

    /**
     * 反注册网络重连监听
     */
    private fun unregisterNetworkListener() {
        hasListener = false
        networkStateThread?.interrupt()
        networkStateThread = null
    }

    /**
     * 连接监听
     */
    private val connectListener = object : JWebSocketClient1.ConnectListener {
        @Synchronized
        override fun connected() {
            resend()
            unregisterNetworkListener()
        }

        @Synchronized
        override fun disconnected() {
            client?.destroy()
            client = null
            registerNetworkListener()
        }
    }

    /**
     * 添加监听器
     */
    fun addActivePushCallback(callback: ActivePushCallback) {
        if (activePushCallbacks.size > 30) {//最多30个
            activePushCallbacks.removeFirst()
        }
        activePushCallbacks.addLast(callback)
    }

    /**
     * 移除监听器
     */
    fun removeActivePushCallback(callback: ActivePushCallback) {
        activePushCallbacks.remove(callback)
    }

    /**
     * 移除所有监听器
     */
    fun removeAllActivePushCallback() {
        activePushCallbacks.clear()
    }

    /**
     * 发送消息，不做消息回复确认
     */
    fun sendMessage(message: String) {
        client?.run {
            if (isConnected) {
                send(message)
                return
            }
        }
        messageList.addLast(message)
    }

    /**
     * 发送请求消息
     */
    @Synchronized
    fun sendRequestMessage(iotMsg: IotMsg, messageCallback: MessageCallback?) {
        if (requestSuq >= Int.MAX_VALUE - 1000)
            requestSuq = 0
        val entity = RequestEntity(requestSuq++, iotMsg)
        entity.messageCallback = messageCallback
        client?.run {
            if (isConnected) {
                confirmQueue.addLast(entity)
                send(iotMsg.toString())
                return
            }
        }
        requestQueue.addLast(entity)
    }

    /**
     * 只重发一次
     */
    @Synchronized
    private fun resend() {
        client?.run {
            confirmQueue.forEach {
                send(it.iotMsg.toString())
            }
            while (requestQueue.isNotEmpty()) {
                send(requestQueue.poll()?.iotMsg.toString())
            }
            while (messageList.isNotEmpty()) {
                send(messageList.poll())
            }
        }
    }

    /**
     * 获得请求对象
     */
    @Synchronized
    private fun getRequestEntity(reqId: Int): RequestEntity? {
        val iterator = confirmQueue.iterator()
        while (iterator.hasNext()) {
            if (iterator.next().reqId == reqId) {
                return iterator.next()
            }
        }
        return null
    }

    /**
     * 重连
     */
    fun reconnect() {
        connectListener.disconnected()
    }

    /**
     * 销毁
     */
    fun destroy() {
        removeAllActivePushCallback()
        client?.destroy()
        client = null
        unregisterNetworkListener()
    }

    /**
     * 通过ping外网的方式判断网络连接
     */
    inner class NetworkStateRunnable : Runnable {
        override fun run() {
            try {
                while (hasListener) {
                    if (WifiUtil.ping("www.baidu.com")) {
                        createSocketClient()
                    }
                    Thread.sleep(1000)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}