package com.tenext.auth.message.resp

import com.tenext.auth.message.upload.IotMsg

class HeartMessage : IotMsg() {

    var error = ""
    var error_message = ""
    var data: Data? = null

    inner class Data {
        var result = ""
    }

}