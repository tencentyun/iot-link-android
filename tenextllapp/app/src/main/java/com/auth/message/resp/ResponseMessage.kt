package com.auth.message.resp

import com.alibaba.fastjson.JSONObject

class ResponseMessage {

    var error = ""
    var error_message = ""
    var data: Data? = null
    var reqId = -1

    inner class Data {
        var Response: JSONObject? = null
    }

}