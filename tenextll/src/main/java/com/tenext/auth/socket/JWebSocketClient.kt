package com.tenext.auth.socket

import com.tenext.auth.socket.callback.ConnectionCallback
import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import java.net.URI

class JWebSocketClient(serverUri: URI, handler: DispatchMsgHandler, connectionCallback: ConnectionCallback) : WebSocketClient(serverUri) {

    private val dispatchMsgHandler = handler
    var isConnected = false
    private val connectListener = connectionCallback

    override fun onOpen(handshakedata: ServerHandshake) {
    }

    override fun onClose(code: Int, reason: String, remote: Boolean) {
        disconnect()
    }

    override fun onMessage(message: String) {
        isConnected = true
        connectListener.connected()
        dispatchMsgHandler.dispatch(message)
    }

    override fun onError(ex: Exception?) {
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