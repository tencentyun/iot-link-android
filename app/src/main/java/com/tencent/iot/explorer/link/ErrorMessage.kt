package com.tencent.iot.explorer.link

import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.JSONObject
import com.tencent.iot.explorer.link.core.auth.util.JsonManager
import com.tencent.iot.explorer.link.kitlink.consts.CommonField

class ErrorMessage {

    var Code = ""

    var Message = ""

    companion object {
        //app数据
        fun parseErrorMessage(data: String): ErrorMessage {

            val jsonObject = JSON.parse(data) as JSONObject
            val errorMessage: ErrorMessage
            errorMessage = if (jsonObject.containsKey(CommonField.ERROR)) {
                JsonManager.parseJson(jsonObject.getString(CommonField.ERROR), ErrorMessage::class.java)
            } else {
                JsonManager.parseJson(data, ErrorMessage::class.java)
            }
            return errorMessage
        }
    }

}