package com.tencent.iot.explorer.link.kitlink.entity

import android.text.TextUtils
import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.JSONArray
import com.alibaba.fastjson.JSONObject
import com.tencent.iot.explorer.link.core.log.L
import com.tencent.iot.explorer.link.kitlink.consts.CommonField
import java.lang.Exception

/**
 * WebSocket通用响应实体
 */
class ParentRespEntity {
    var error = ""
    var error_message = ""
    var data: JSONObject? = null
    var reqId = -1

    fun toShow(): String {
        return "error:${error}error_message:${error_message}data:$data"
    }

    fun getResponse(): JSONObject? {
        try {
            if (data == null) return null
            val response = data!!.getString(CommonField.RESPONSE)
            if (TextUtils.isEmpty(response)) return null
            return JSON.parseObject(response)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    fun getResponseKeyObj(key: String): JSONObject? {
        val obj = getResponse()
        if (obj != null) {
            return obj.getJSONObject(key)
        }
        return null
    }

    fun getResponseKeyArray(key: String): JSONArray? {
        val obj = getResponse()
        if (obj != null) {
            return obj.getJSONArray(key)
        }
        return null
    }

    /**
     * 获得响应结果中的template字段的数据
     */
    fun getResponseTemplate(): JSONObject? {
        val obj = getResponse()
        if (obj != null) {
            val products = obj.getJSONArray(CommonField.PRODUCTS)
            if (products.isNullOrEmpty()) return null
            val json = JSON.parseObject(products[0].toString()).getString(CommonField.DATA_TEMPLATE)
            L.d("template=$json")
            return JSON.parseObject(json)
        }
        return null
    }

    fun getResponse(key: String): String {
        val obj = getResponse()
        return if (obj == null) {
            ""
        } else {
            if (TextUtils.isEmpty(obj.getString(key))) {
                ""
            } else {
                obj.getString(key)
            }
        }
    }

    fun getResponseError(): JSONObject? {
        try {
            if (data == null) return null
            val response = data!!.getString(CommonField.RESPONSE)
            if (TextUtils.isEmpty(response)) return null
            val respObj = JSON.parseObject(response)
            return respObj.getJSONObject(CommonField.ERROR)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    fun getResponseErrorMessage(): String {
        return getResponseError(CommonField.MESSAGE)
    }

    fun getResponseError(key: String): String {
        val entity = getResponseError()
        return if (entity == null) {
            ""
        } else {
            if (TextUtils.isEmpty(entity.getString(key))) {
                ""
            } else {
                entity.getString(key)
            }
        }
    }
}