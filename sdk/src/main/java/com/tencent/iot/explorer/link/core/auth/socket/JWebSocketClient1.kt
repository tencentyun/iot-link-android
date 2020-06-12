package com.tencent.iot.explorer.link.core.auth.socket

import com.tencent.iot.explorer.link.core.log.L
import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import java.net.URI

class JWebSocketClient1(serverUri: URI?, handler: DispatchMsgHandler) : WebSocketClient(serverUri) {

    var isConnected = false
    var connectListener: ConnectListener? = null
    private var dispatchMsgHandler = handler

    //保活相关参数
    private var heartBack = true
    private var isKeep = false
    private var keepLiveThread: Thread? = null
    private var delayMillis = 60 * 1000L
    private var param = "{\"action\":\"Hello\",\"reqId\":-1}"

    override fun send(text: String?) {
        try {
            super.send(text)
        } catch (e: Exception) {
            disconnect()
            e.printStackTrace()
        }
    }

    private fun sendHeart() {
        this.send(param)
    }

    override fun onOpen(handshakedata: ServerHandshake?) {
    }

    override fun onClose(code: Int, reason: String?, remote: Boolean) {
        disconnect()
    }

    override fun onMessage(message: String?) {
        L.d("JWebSocketClient", message ?: "")
        dispatchMsgHandler.dispatch(message ?: "")
        keepLive()
        reconnected()
    }

    @Synchronized
    private fun reconnected() {
        if (!isConnected) {//重连
            isConnected = true
            connectListener?.connected()
        }
    }

    override fun onError(ex: Exception?) {
        disconnect()
    }

    /**
     * 保活
     */
    private fun keepLive() {
        if (keepLiveThread == null) {
            isKeep = true
            keepLiveThread = Thread(KeepLiveRunnable())
            keepLiveThread?.start()
        }
    }

    /**
     * 取消保活
     */
    private fun removeKeepLive() {
        isKeep = false
        keepLiveThread?.let {
            it.interrupt()
            keepLiveThread = null
        }
    }

    fun destroy() {
        isConnected = false
        removeKeepLive()
        this.close()
    }

    private fun disconnect() {
        isConnected = false
        connectListener?.disconnected()
    }

    inner class KeepLiveRunnable : Runnable {
        override fun run() {
            try {
                while (isKeep) {
                    if (!heartBack) {
                        disconnect()
                    }
                    sendHeart()
                    heartBack = false
                    Thread.sleep(delayMillis)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    interface ConnectListener {

        fun connected()

        fun disconnected()
    }

}