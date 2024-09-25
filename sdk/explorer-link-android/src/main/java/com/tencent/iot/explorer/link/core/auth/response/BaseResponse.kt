package com.tencent.iot.explorer.link.core.auth.response

import com.google.gson.Gson
import com.tencent.iot.explorer.link.core.auth.util.JsonManager

/**
 * 基础响应实体
 */
class BaseResponse {

    var code = -1
    var msg = ""
    var data = Any()

    /**
     * 请求成功
     */
    fun isSuccess(): Boolean {
        return code == 0
    }

    /**
     * 解析对应的实体
     */
    fun <T> parse(clazz: Class<T>): T? {
        return JsonManager.parseJson(data.toString(), clazz)
    }

    fun <T> jsonParse(clazz: Class<T>): T? {
        return Gson().fromJson(data.toString(), clazz)
    }
}