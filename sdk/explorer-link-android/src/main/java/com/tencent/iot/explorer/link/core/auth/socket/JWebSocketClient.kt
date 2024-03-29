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

    override fun onOpen(handshakedata: ServerHandshake) {
        connectListener.onOpen()
    }

    override fun onClose(code: Int, reason: String, remote: Boolean) {
        L.e("onClose code:$code, reason:$reason, remote:$remote")
        disconnect()
    }

    override fun onMessage(message: String) {
        isConnected = true
        connectListener.connected()
        dispatchMsgHandler.dispatch(message)
    }

    override fun onError(ex: Exception?) {
        L.e("onError exception:${ex?.message}")
        isConnected = true
        disconnect()
    }

    fun destroy() {
        isConnected = false
        this.close()
    }

    private fun disconnect() {
        isConnected = false
        connectListener.disconnected()
    }

}