package com.tencent.iot.explorer.link.auth.message.resp

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

}