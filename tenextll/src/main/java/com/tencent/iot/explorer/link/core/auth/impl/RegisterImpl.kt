package com.tencent.iot.explorer.link.core.auth.impl

import com.tencent.iot.explorer.link.core.auth.callback.MyCallback

/**
 * 注册
 */
interface RegisterImpl {

    /**
     * 发送手机验证码
     */
    fun sendPhoneCode(countryCode: String, phone: String, callback: MyCallback)

    /**
     * 发送手机验证码
     */
    fun sendEmailCode(email: String, callback: MyCallback)

    /**
     * 验证手机验证码
     */
    fun checkPhoneCode(
        countryCode: String, phone: String, code: String, callback: MyCallback
    )

    /**
     * 验证邮箱验证码
     */
    fun checkEmailCode(email: String, code: String, callback: MyCallback)

    /**
     *  手机号注册
     */
    fun registerPhone(
        countryCode: String, phone: String, code: String, pwd: String, callback: MyCallback
    )

    /**
     *  邮箱注册
     */
    fun registerEmail(email: String, code: String, pwd: String, callback: MyCallback)

}