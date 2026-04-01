package com.tencent.iot.explorer.link.core.auth.socket

import com.tencent.iot.explorer.link.core.auth.socket.callback.ConnectionCallback
import com.tencent.iot.explorer.link.core.log.L
import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import java.net.URI

class JWebSocketClient(serverUri: URI, handler: DispatchMsgHandler, connectionCallback: ConnectionCallback) : WebSocketClient(serverUri) {

    private val dispatchMsgHandler = handler
    var isConnected = false
    private val connectListener = connectionCallback

    // 标记是否为主动关闭，主动关闭不触发重连
    @Volatile
    var isActiveClose = false

    override fun onOpen(handshakedata: ServerHandshake) {
        isConnected = true
        isActiveClose = false
        connectListener.connected()
        connectListener.onOpen()
    }

    override fun onClose(code: Int, reason: String, remote: Boolean) {
        L.e("onClose code:$code, reason:$reason, remote:$remote, isActiveClose:$isActiveClose")
        isConnected = false

        if (isActiveClose) {
            return
        }

        when (code) {
            1000 -> {
            }
            else -> {
                // 异常关闭，触发重连
                L.w("异常关闭($code)，触发重连")
                connectListener.disconnected()
            }
        }
    }

    override fun onMessage(message: String) {
        dispatchMsgHandler.dispatch(message)
    }

    override fun onError(ex: Exception?) {
        L.e("onError exception:${ex?.message}")
        isConnected = false
    }

    fun destroy() {
        isActiveClose = true
        isConnected = false
        close()
    }
}
