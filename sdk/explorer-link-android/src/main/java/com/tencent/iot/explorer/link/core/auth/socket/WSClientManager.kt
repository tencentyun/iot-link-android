package com.tencent.iot.explorer.link.core.auth.socket

import android.text.TextUtils
import com.tencent.iot.explorer.link.core.auth.message.MessageConst
import com.tencent.iot.explorer.link.core.auth.message.payload.Payload
import com.tencent.iot.explorer.link.core.auth.message.resp.RespFailMessage
import com.tencent.iot.explorer.link.core.auth.message.resp.RespSuccessMessage
import com.tencent.iot.explorer.link.core.auth.message.upload.ArrayString
import com.tencent.iot.explorer.link.core.auth.message.upload.HeartMessage
import com.tencent.iot.explorer.link.core.auth.message.upload.IotMsg
import com.tencent.iot.explorer.link.core.auth.socket.callback.*
import com.tencent.iot.explorer.link.core.auth.socket.entity.RequestEntity
import com.tencent.iot.explorer.link.core.auth.util.WifiUtil
import com.tencent.iot.explorer.link.core.log.L
import kotlinx.coroutines.*
import org.json.JSONObject
import java.net.URI
import java.util.*

/**
 * JWebsocketClient管理
 */
internal class WSClientManager private constructor() {

    private var hasListener = false
    private var job: Job? = null
    private lateinit var heartJob: Job

    private var heartMessageList = ArrayString()
    private var heartCount = 0

    //标记是否可以重新创建
    private var client: JWebSocketClient? = null

    @Volatile
    private var requestSuq = 0 //请求序号

    //消息响应分发
    private val handler = DispatchMsgHandler()

    private var host = "wss://iot.cloud.tencent.com/ws/explorer"
    private val messageList = LinkedList<String>()
    private val requestQueue = LinkedList<RequestEntity>()
    private val confirmQueue = LinkedList<RequestEntity>()

    //设备监听器
    private val activePushCallbacks = LinkedList<ActivePushCallback>()

    //enterRoom监听
    private var payloadMessageCallback: PayloadMessageCallback? = null

    //首页设备上下线监听
    private var deviceStatusCallback: PayloadMessageCallback? = null

    //保活相关参数
    private var isKeep = false
    private val delayMills = 10 * 1000L
    private var param = "{\"action\":\"Hello\",\"reqId\":${MessageConst.HEART_ID}}"

    //enterRoom监听使能参数
    public var enablePayloadMessageCallback = true


    /**
     * 单例
     */
    companion object {

        private var debugTag = ""

        val instance: WSClientManager by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
            WSClientManager()
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
     * 初始化
     */
    fun init() {
        isKeep = true
        createSocketClient()
        startHeartJob()
    }

    fun setBrokerUrl(value: String) {
        host = value
    }

    /**
     * 添加设备id
     */
    fun addDeviceIds(ids: ArrayString) {
        for (i in 0 until ids.size()) {
            heartMessageList.addValue(ids.getValue(i))
        }
    }

    /**
     * 发送设备订阅心跳
     */
    fun sendHeartMessage() {
        sendMessage(HeartMessage(heartMessageList))
    }

    /**
     * 移除
     */
    fun removeDeviceIds(ids: ArrayString) {
        for (i in 0 until ids.size())
            heartMessageList.remove(ids.getValue(i))
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
            // websocket消息总入口
            L.e(payload.toString())
            if (payloadMessageCallback != null && enablePayloadMessageCallback) {
                payloadMessageCallback!!.payloadMessage(payload)
            }
            if (deviceStatusCallback != null) {
                deviceStatusCallback!!.payloadMessage(payload)
            }
            activePushCallbacks.forEach {
                it.success(payload)
            }
        }

        override fun payloadUnknownMessage(json: String, errorMessage: String) {
            activePushCallbacks.forEach {
                it.unknown(json, errorMessage)
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
        client = JWebSocketClient(URI(myHost), handler, connectListener)
        client!!.connect()
    }

    /**
     * 发送心跳包
     */
    private fun startHeartJob() {
        heartJob = CoroutineScope(Dispatchers.IO).launch {
            while (isKeep) {
                sendMessage(param)
                if (heartMessageList.isNotEmpty() && heartCount == 5) {
                    sendHeartMessage()
                    heartCount = 0
                }
                heartCount++
                delay(delayMills)
            }
        }
    }

    /**
     * 停止发送心跳包
     */
    private fun stopHeartJob() {
        isKeep = false
        heartJob.cancel()
    }

    /**
     * 开始重连
     */
    private fun startJob() {
        hasListener = true
        L.e("开始重连")
        if (job == null) {
            job = CoroutineScope(Dispatchers.IO).launch {
                while (hasListener) {
                    try {
                        if (WifiUtil.ping("www.baidu.com") || WifiUtil.ping("iot.cloud.tencent.com")) {
                            if (client != null) {
                                client?.destroy()
                                client = null
                            }
                            createSocketClient()
                            L.d("正在尝试重新连接wss://iot.cloud.tencent.com")
                        } else {
                            L.d("无法连接wss://iot.cloud.tencent.com")
                        }
                        delay(2000)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        } else {
            if (!job!!.isActive)
                job?.start()
        }
    }

    /**
     * 停止重连
     */
    private fun stopJob() {
        if (job != null) {
            hasListener = false
            job?.cancel()
            job = null
        }
    }

    /**
     * 连接监听
     */
    private val connectListener = object : ConnectionCallback {

        override fun connected() {
            resend()
            if (job != null)
                activePushCallbacks.forEach {
                    it.reconnected()
                }
            stopJob()
        }

        override fun disconnected() {
            L.d("连接断开")
            client?.destroy()
            client = null
            startJob()
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
     * 添加进入trtc房间的监听器
     */
    fun addPayloadMessageCallback(callback: PayloadMessageCallback) {
        payloadMessageCallback = callback
    }

    /**
     * 添加首页设备状态更新监听
     */
    fun addDeviceStatusCallback(callback: PayloadMessageCallback) {
        deviceStatusCallback = callback
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
     * 判断连接是否正常
     */
    fun isConnected(): Boolean {
        return client?.isConnected ?: false
    }

    /**
     * 发送消息，不做消息回复确认
     */
    fun sendMessage(message: String) {
        try {
            client?.run {
                if (isConnected) {
                    send(message)
                    return
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            connectListener.disconnected()
        }
        messageList.addLast(message)
    }

    /**
     * 发送消息，不做消息回复确认
     */
    fun sendMessage(message: IotMsg) {
        L.e("${message.getMyAction()}:${message}")
        sendMessage(message.toString())
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
                sendMessage(iotMsg)
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
        hasListener = false
        isKeep = false
        client?.destroy()
        client = null
        stopHeartJob()
        stopJob()
    }

}