package com.tencent.iot.explorer.link.core.auth.message.resp

import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.JSONObject

class ResponseMessage {

    var error = ""
    var error_message = ""
    var data: Data? = null
    var reqId = -1

    inner class Data {
        var Response: JSONObject? = null

        override fun toString(): String {
            return JSON.toJSONString(this)
        }
    }

}