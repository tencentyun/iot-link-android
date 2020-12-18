package com.tencent.iot.explorer.link.core.link.listener


interface SoftAPConfigNetListener {

    fun onSuccess()

    fun onFail(code: String, msg: String)

}