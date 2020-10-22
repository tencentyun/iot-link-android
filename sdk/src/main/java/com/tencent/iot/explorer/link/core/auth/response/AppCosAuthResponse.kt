package com.tencent.iot.explorer.link.core.auth.response

import com.alibaba.fastjson.JSONObject

class AppCosAuthResponse {

    var expiredTime = 0L
    var expiration = ""
    var credentials: JSONObject? = null
    var requestId = ""
    var startTime = 0L
    var cosConfig: CosConfig? = null

    class CosConfig {
        var bucket = ""
        var region = ""
        var path = ""
    }
}