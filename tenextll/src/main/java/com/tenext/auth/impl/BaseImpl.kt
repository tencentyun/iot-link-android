package com.tenext.auth.impl

import com.tenext.auth.callback.MyCallback

interface BaseImpl {

    /**
     * 未登录接口公共参数
     */
    fun commonParams(action: String): HashMap<String, Any>

    /**
     * 登录后接口公共参数
     */
    fun tokenParams(action: String): HashMap<String, Any>

    /**
     * 未登录请求
     */
    fun postJson(param: HashMap<String, Any>, callback: MyCallback, reqCode: Int)

    /**
     * 登录后请求
     */
    fun tokenAppCosAuth(param: HashMap<String, Any>, callback: MyCallback, reqCode: Int)

}