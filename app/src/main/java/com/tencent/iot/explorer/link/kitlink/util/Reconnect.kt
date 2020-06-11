package com.tencent.iot.explorer.link.kitlink.util

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

    @Volatile
    private var connectionListener: ConnectionListener? = null


    /**
     * 开始监听，ping 包测试成功，就会关闭该协程，ping 包测试失败，会一直尝试 ping
     */
    fun start(listener: ConnectionListener) {
        connectionListener = listener
        hasListener = true
        job = GlobalScope.launch {
            repeat(100) {

                // 当前网络不通的情况下，发送 ping 包测试
                if (!hasConnected) {
                    hasConnected = ping()
                    Thread.sleep(1000)
                }

                // 后续每两秒回调一次连接成功
                if (hasConnected && hasListener) {
                    Thread.sleep(2000)
                    connectionListener?.onConnected()
                }
            }
        }
    }

    /**
     * 幂等方式停止监听网络是否可达
     */
    fun stop(listener: ConnectionListener) {
        if (connectionListener == listener) {
            connectionListener = null
            hasListener = false
            job?.cancel()
        }
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
        } finally {
            process?.destroy()
        }
        return isSuccess
    }
}

interface ConnectionListener {
    fun onConnected()
}