package com.tencent.iot.explorer.link.core.auth.message.resp

import com.alibaba.fastjson.JSON

class RespSuccessMessage {

    var response = ""
    //response中的RequestId
    var RequestId = ""

    fun <T> parse(clazz: Class<T>): T? {
        try {
            return JSON.parseObject(response, clazz)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

}