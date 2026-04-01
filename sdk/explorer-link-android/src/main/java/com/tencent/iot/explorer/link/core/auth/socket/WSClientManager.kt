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
import java.net.URI
import java.util.*

/**
 * JWebsocketClient管理
 */
internal class WSClientManager private constructor() {

    private var TAG = WSClientManager.javaClass.simpleName
    private val scope = CoroutineScope(Dispatchers.IO)
    private var hasListener = false
    private var job: Job? = null
    private lateinit var heartJob: Job

    // 重连策略参数
    private var retryCount = 0
    private val maxRetryCount = 15              // 最大重试次数
    private val baseDelayMs = 2000L             // 初始延迟 2s
    private val maxDelayMs = 60_000L            // 最大延迟 60s
    private val jitterRange = 500L              // 随机抖动范围

    // 队列上限参数
    private val maxMessageQueueSize = 120
    private val maxRequestQueueSize = 60
    private val queueLogThreshold = 20

    private var heartMessageList = ArrayString()
    private var heartCount = 0

    //标记是否可以重新创建
    private var client: JWebSocketClient? = null

    @Volatile
    private var requestSuq = 0 //请求序号

    //消息响应分发
    private val handler = DispatchMsgHandler()

    private var host = "wss://iot.cloud.tencent.com/iotstudio_v2_weapp_1"
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

    private var socketCallback: ConnectionCallback? = null

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
        if (isKeep) {
            L.w("WS init ignored, already initialized")
            logQueueState("init_skip")
            return
        }

        isKeep = true
        createSocketClient()
        startHeartJob()
        logQueueState("init")
    }

    /**
     * 设置WebSocket连接状态回调
     */
    fun setSocketCallback(callback: ConnectionCallback) {
        socketCallback = callback
    }

    fun setBrokerUrl(value: String) {
        host = value
    }

    /**
     * 添加设备id
     */
    fun addDeviceIds(ids: ArrayString) {
        for (i in 0 until ids.size()) {
            val id = ids.getValue(i)
            if (!heartMessageList.contains(id)) {
                heartMessageList.addValue(id)
            }
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
        for (i in 0 until ids.size()) {
            heartMessageList.remove(ids.getValue(i))
        }
    }

    /**
     * 响应数据解析回调
     */
    private val callback = object : DispatchCallback {
        override fun yunMessage(reqId: String, message: String, response: RespSuccessMessage) {
            getRequestEntity(reqId)?.run {
                confirmQueue.remove(this)
                logQueueState("resp_success")
                messageCallback?.success(reqId, message, response)
            }
        }

        override fun yunMessageFail(reqId: String, message: String, response: RespFailMessage) {
            getRequestEntity(reqId)?.run {
                confirmQueue.remove(this)
                logQueueState("resp_fail")
                messageCallback?.fail(reqId, message, response)
            }
        }

        override fun payloadMessage(payload: Payload) {
            // websocket消息总入口
            L.e(payload.toString())
            if (payloadMessageCallback != null && enablePayloadMessageCallback) {
                payloadMessageCallback!!.payloadMessage(payload)
            }
            deviceStatusCallback?.payloadMessage(payload)
            activePushCallbacks.forEach {
                it.success(payload)
            }
        }

        override fun payloadUnknownMessage(json: String, errorMessage: String) {
            activePushCallbacks.forEach {
                it.unknown(json, errorMessage)
            }
        }

        override fun unknownMessage(reqId: String, json: String) {
            getRequestEntity(reqId)?.run {
                confirmQueue.remove(this)
                logQueueState("resp_unknown")
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
        scope.launch(Dispatchers.Main) {
            val myHost = if (debugTag.isNotEmpty())
                host + debugTag
            else
                host
            //创建WebSocket
            client = JWebSocketClient(URI(myHost), handler, connectListener)
            client?.connectionLostTimeout = 0
            client?.connect()
        }
    }

    /**
     * 发送心跳包
     */
    private fun startHeartJob() {
        heartJob = scope.launch(Dispatchers.IO) {
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
        if (::heartJob.isInitialized && heartJob.isActive) {
            heartJob.cancel()
        }
    }

    private fun logQueueState(scene: String) {
        val log =
            "WS queue[$scene] message=${messageList.size}, request=${requestQueue.size}, confirm=${confirmQueue.size}, retry=$retryCount, connected= ${client?.isConnected == true}"

        if (messageList.size >= queueLogThreshold || requestQueue.size >= queueLogThreshold || confirmQueue.size >= queueLogThreshold) {
            L.w(log)
        } else {
            L.d(log)
        }
    }

    private fun enqueueMessage(message: String, reason: String) {
        if (messageList.size >= maxMessageQueueSize) {
            messageList.pollFirst()
            L.w("WS message queue full, drop oldest, reason=$reason")
        }
        messageList.addLast(message)
        logQueueState("enqueue_message_$reason")
    }

    private fun enqueueRequest(entity: RequestEntity, reason: String) {
        if (requestQueue.size >= maxRequestQueueSize) {
            requestQueue.pollFirst()
            L.w("WS request queue full, drop oldest, reason=$reason")
        }
        requestQueue.addLast(entity)
        logQueueState("enqueue_request_$reason")
    }

    /**
     * 计算指数退避延迟（含随机抖动）
     * 2s, 4s, 8s, 16s, 32s, 60s, 60s, ...
     */
    private fun getRetryDelay(): Long {
        val exponentialDelay = baseDelayMs * (1L shl minOf(retryCount, 5))
        val delay = minOf(exponentialDelay, maxDelayMs)
        val jitter = (Math.random() * jitterRange).toLong()
        return delay + jitter
    }

    /**
     * 开始重连
     */
    private fun startJob() {
        hasListener = true
        retryCount = 0
        L.e("开始重连")
        logQueueState("start_retry")
        if (job == null) {
            job = scope.launch(Dispatchers.IO) {
                while (hasListener && retryCount < maxRetryCount) {
                    try {
                        val currentDelay = getRetryDelay()
                        L.d("第 ${retryCount + 1}/$maxRetryCount 次重连，延迟 ${currentDelay}ms")

                        if (WifiUtil.ping("iot.cloud.tencent.com") || WifiUtil.ping("www.baidu.com")) {
                            if (client != null) {
                                scope.launch(Dispatchers.Main) {
                                    client?.destroy()
                                    client = null
                                }
                            }
                            createSocketClient()
                            L.d("正在尝试重新连接wss://iot.cloud.tencent.com")
                        } else {
                            L.d("网络不可达，等待下次重试")
                        }

                        retryCount++
                        delay(currentDelay)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

                if (retryCount >= maxRetryCount) {
                    L.e("重连已达上限 $maxRetryCount 次，停止重连")
                    hasListener = false
                    job = null
                    socketCallback?.disconnected()
                }
            }
        } else {
            if (!job!!.isActive) {
                job?.start()
            }
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
            retryCount = 0
            logQueueState("connected_before_resend")
            resend()
            logQueueState("connected_after_resend")
            if (job != null) {
                activePushCallbacks.forEach {
                    it.reconnected()
                }
            }
            stopJob()
        }

        override fun disconnected() {
            val destroyJob = scope.launch(Dispatchers.Main) {
                L.d("连接断开")
                logQueueState("disconnected")
                client?.destroy()
                client = null
            }
            scope.launch {
                destroyJob.join()
                socketCallback?.disconnected()
                startJob()
            }
        }

        override fun onOpen() {
            socketCallback?.onOpen()
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
        if (message == null) {   // 空消息不做发送处理，且不入队列
            L.e(TAG, "empty message")
            return
        }

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
        enqueueMessage(message, "not_connected")
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
        if (requestSuq >= Int.MAX_VALUE - 1000) {
            requestSuq = 0
        }

        val reqId = UUID.randomUUID().toString()
        val entity = RequestEntity(reqId, iotMsg)
        entity.messageCallback = messageCallback

        client?.run {
            if (isConnected) {
                try {
                    confirmQueue.addLast(entity)
                    send(iotMsg.toString())
                    logQueueState("send_request_direct")
                    return
                } catch (e: Exception) {
                    confirmQueue.remove(entity)
                    e.printStackTrace()
                    connectListener.disconnected()
                }
            }
        }

        enqueueRequest(entity, "not_connected")
    }

    /**
     * 只重发一次
     */
    @Synchronized
    private fun resend() {
        client?.run {
            confirmQueue.forEach {
                it.iotMsg.toString()?.let { content ->  // 避免发送空内容
                    send(content)
                }
            }

            while (requestQueue.isNotEmpty()) {
                val entity = requestQueue.poll()
                if (entity != null) {
                    confirmQueue.addLast(entity)
                    send(entity.iotMsg.toString())
                }
            }

            while (messageList.isNotEmpty()) {
                val msg = messageList.poll()
                if (msg != null) {
                    send(msg)
                }
            }
        }
    }

    /**
     * 获得请求对象
     */
    @Synchronized
    private fun getRequestEntity(reqId: String): RequestEntity? {
        val iterator = confirmQueue.iterator()
        while (iterator.hasNext()) {
            val entity = iterator.next()
            if (entity.reqId == reqId) {
                return entity
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
        scope.launch(Dispatchers.Main) {
            hasListener = false
            isKeep = false

            // 停止重连和心跳
            stopJob()
            stopHeartJob()

            // 销毁连接
            client?.destroy()
            client = null

            messageList.clear()
            requestQueue.clear()
            confirmQueue.clear()
            heartMessageList.clear()
            logQueueState("destroy")
        }
    }
}
