package com.tencent.iot.explorer.link.core.link.listener

interface WiredConfigListener {

    fun onStartConfigNet()

    fun onSuccess(productId: String, deviceName: String)

    fun onFail()

    fun onConfiging()

}