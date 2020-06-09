package com.tencent.iot.explorer.link.core.auth.http

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

/**
 * 多次重复任务工具
 */
class RetryJob {

    constructor()

    constructor(time: Int, mills: Long) {
        this.time = time
        this.mills = mills
    }

    private var job: Job? = null

    private var time = 80
    private var mills = 2000L
    private var hasListener = false
    private var hasConnected = false

    private var retryListener: RetryListener? = null


    /**
     * 开始监听
     */
    fun start(listener: RetryListener) {
        start("www.baidu.com", listener)
    }

    /**
     * 开始监听
     */
    fun start(host: String, listener: RetryListener) {
        retryListener = listener
        hasListener = true
        job?.cancel()
        job = CoroutineScope(Dispatchers.IO).launch {
            repeat(time) {
                if (!hasConnected) {
                    hasConnected = ping(host)
                    Thread.sleep(1000)
                }
                if (hasConnected && hasListener) {
                    Thread.sleep(mills)
                    retryListener?.onRetry()
                }
            }
        }
    }

    /**
     * 停止监听
     */
    fun stop() {
        retryListener = null
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

interface RetryListener {
    fun onRetry()
}