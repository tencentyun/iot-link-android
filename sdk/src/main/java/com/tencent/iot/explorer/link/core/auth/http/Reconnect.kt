package com.tencent.iot.explorer.link.core.auth.http

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

/**
 * 重新连接工具
 */
class Reconnect private constructor() {

    private object Holder {
        val instance = Reconnect()
    }

    companion object {
        val instance = Holder.instance
    }

    private var job: Job? = null
    private var hasListener = false
    private var hasConnected = false

    private var connectionListener: ConnectionListener? = null


    /**
     * 开始监听
     */
    fun start(listener: ConnectionListener) {
        start("www.baidu.com", listener)
    }

    /**
     * 开始监听
     */
    fun start(host: String, listener: ConnectionListener) {
        connectionListener = listener
        hasListener = true
        job = GlobalScope.launch {
            repeat(100) {
                if (!hasConnected) {
                    hasConnected = ping(host)
                    Thread.sleep(1000)
                }
                if (hasConnected && hasListener) {
                    Thread.sleep(2000)
                    connectionListener?.onConnected()
                }
            }
        }
    }

    /**
     * 停止监听
     */
    fun stop() {
        connectionListener = null
        hasListener = false
        job?.cancel()
    }

    /**
     * ping外网
     */
    private fun ping(): Boolean {
        return ping("www.baidu.com")
    }

    /**
     * ping外网
     */
    private fun ping(host: String): Boolean {
        var isSuccess: Boolean
        var process: Process? = null
        try {
            process = Runtime.getRuntime()
                .exec("/system/bin/ping -c 1 $host")
            isSuccess = (process.waitFor() == 0)
        } catch (e: Exception) {
            isSuccess = false
            e.printStackTrace()
            process?.destroy()
        } finally {
            process?.destroy()
        }
        return isSuccess
    }
}

interface ConnectionListener {
    fun onConnected()
}