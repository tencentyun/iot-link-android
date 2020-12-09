package com.tencent.iot.explorer.link.core.auth.message.resp

import com.tencent.iot.explorer.link.core.auth.message.upload.IotMsg

class HeartMessage : IotMsg() {

    var error = ""
    var error_message = ""
    var data: Data? = null

    inner class Data {
        var result = ""
    }

}