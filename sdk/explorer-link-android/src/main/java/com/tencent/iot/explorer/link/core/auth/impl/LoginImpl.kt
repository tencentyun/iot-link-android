package com.tencent.iot.explorer.link.core.auth.impl

import com.tencent.iot.explorer.link.core.auth.callback.LoginCallback

/**
 * 登录
 */
interface LoginImpl {

    /**
     *  手机号登录
     */
    fun loginPhone(countryCode: String, phone: String, pwd: String, callback: LoginCallback)

    /**
     *  邮箱登录
     */
    fun loginEmail(email: String, pwd: String, callback: LoginCallback)

    /**
     * 微信登录
     */
    fun wechatLogin(code: String, callback: LoginCallback)

}