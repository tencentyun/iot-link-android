package com.tencent.iot.explorer.link.core.auth.service

import com.tencent.iot.explorer.link.core.auth.IoTAuth
import com.tencent.iot.explorer.link.core.auth.callback.MyCallback
import com.tencent.iot.explorer.link.core.auth.http.HttpCallBack
import com.tencent.iot.explorer.link.core.auth.http.HttpUtil
import com.tencent.iot.explorer.link.core.auth.response.BaseResponse
import com.tencent.iot.explorer.link.core.auth.util.JsonManager
import com.tencent.iot.explorer.link.core.auth.util.SignatureUtil
import com.tencent.iot.explorer.link.core.log.L
import java.util.*
import kotlin.collections.HashMap

/**
 * 接口请求文件
 */
open class BaseService {

    companion object {

        const val APP_SECRECY = "***REMOVED***"

        const val HOST = "https://iot.cloud.tencent.com/api/"
        const val APP_API = "studioapp"
        const val TOKEN_API = "exploreropen/tokenapi"
        const val APP_COS_AUTH = "studioapp/AppCosAuth"

    }

    /**
     * 未登录接口公共参数
     */
    fun commonParams(action: String): HashMap<String, Any> {
        val param = HashMap<String, Any>()
        param["RequestId"] = UUID.randomUUID().toString()
        param["Action"] = action
        param["Platform"] = "android"
        param["AppKey"] = IoTAuth.APP_KEY
        param["Timestamp"] = System.currentTimeMillis() / 1000
        param["Nonce"] = Random().nextInt(10)
        return param
    }

    /**
     * 登录后接口公共参数
     */
    fun tokenParams(action: String): HashMap<String, Any> {
        val param = HashMap<String, Any>()
        param["RequestId"] = UUID.randomUUID().toString()
        param["Action"] = action
        param["Platform"] = "android"
        param["AppKey"] = IoTAuth.APP_KEY
        param["Timestamp"] = System.currentTimeMillis() / 1000
        param["Nonce"] = Random().nextInt(10)
        param["AccessToken"] = IoTAuth.user.Token
        return param
    }

    private fun sign(param: HashMap<String, Any>): HashMap<String, Any> {
        val sign = SignatureUtil.format(param)
        val result = SignatureUtil.signature(sign, APP_SECRECY)
        param["Signature"] = result
        return param
    }

    /**
     * 未登录请求
     */
    fun postJson(param: HashMap<String, Any>, callback: MyCallback, reqCode: Int) {
        val action = param["Action"]
        val json = JsonManager.toJson(param)
//        val json = JsonManager.toJson(sign(param))
//        HttpUtil.postJson("$HOST$APP_API", json, object : HttpCallBack {
        HttpUtil.postJson("$HOST$APP_API/$action", json, object : HttpCallBack {
            override fun onSuccess(response: String) {
                L.d("响应$action", response)
                JsonManager.parseJson(response, BaseResponse::class.java)?.run {
                    callback.success(this, reqCode)
                }
            }

            override fun onError(error: String) {
                callback.fail(error, reqCode)
            }
        })
    }

    /**
     * 登录后请求
     */
    fun tokenPost(param: HashMap<String, Any>, callback: MyCallback, reqCode: Int) {
        val json = JsonManager.toJson(param)
        L.d("请求${param["Action"]}", json)
        if (IoTAuth.user.isExpire()) {//登录过期或未登录
            IoTAuth.loginExpiredListener?.expired(IoTAuth.user)
            return
        }
        HttpUtil.postJson("$HOST$TOKEN_API", json, object : HttpCallBack {
            override fun onSuccess(response: String) {
                L.d("响应${param["Action"]}", response)
                JsonManager.parseJson(response, BaseResponse::class.java)?.run {
                    callback.success(this, reqCode)
                }
            }

            override fun onError(error: String) {
                callback.fail(error, reqCode)
            }
        })
    }

    /**
     * 登录后请求
     */
    fun tokenAppCosAuth(param: HashMap<String, Any>, callback: MyCallback, reqCode: Int) {
        val json = JsonManager.toJson(param)
        if (IoTAuth.user.isExpire()) {//登录过期或未登录
            IoTAuth.loginExpiredListener?.expired(IoTAuth.user)
            return
        }
        HttpUtil.postJson("$HOST$APP_COS_AUTH", json, object : HttpCallBack {
            override fun onSuccess(response: String) {
                L.d("响应${param["Action"]}", response)
                JsonManager.parseJson(response, BaseResponse::class.java)?.run {
                    callback.success(this, reqCode)
                }
            }

            override fun onError(error: String) {
                callback.fail(error, reqCode)
            }
        })
    }


}