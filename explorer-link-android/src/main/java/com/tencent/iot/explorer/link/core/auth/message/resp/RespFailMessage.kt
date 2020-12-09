package com.tencent.iot.explorer.link.core.auth.message.resp

import com.alibaba.fastjson.JSON

class RespFailMessage {

    var Error: ErrorData? = null

    var RequestId = ""

    inner class ErrorData {
        var Code = ""
        var Message = ""
    }

    fun setErrorMessage(message: String) {
        if (Error == null)
            Error = ErrorData()
        Error?.Message = message
    }

    override fun toString(): String {
        return JSON.toJSONString(this)
    }

    fun errorString():String{
        Error?.run {
            return JSON.toJSONString(this)
        }
        return ""
    }

}