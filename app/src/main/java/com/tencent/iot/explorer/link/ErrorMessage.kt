package com.tencent.iot.explorer.link

import android.util.Log
import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.JSONObject
import com.tencent.iot.explorer.link.kitlink.consts.CommonField
import com.tencent.iot.explorer.link.kitlink.util.JsonManager

class ErrorMessage {

    var Code = ""

    var Message = ""

    companion object {
        //app数据
        fun parseErrorMessage(data: String): ErrorMessage {

            var jsonObject = JSON.parse(data) as JSONObject
            var errorMessage = ErrorMessage()
            if (jsonObject.containsKey(CommonField.ERROR)) {
                errorMessage = JsonManager.parseJson(jsonObject.getString(CommonField.ERROR), ErrorMessage::class.java)
            } else {
                errorMessage = JsonManager.parseJson(data, ErrorMessage::class.java)
            }
            return errorMessage
        }
    }

}