package com.tenext.auth.impl

import com.tenext.auth.callback.MyCallback

 interface VerifyImpl {

    /**
     * 发送手机验证码
     */
    fun sendPhoneCode(type: String, countryCode: String, phone: String, callback: MyCallback)

    /**
     * 发送手机验证码
     */
    fun sendEmailCode(type: String, email: String, callback: MyCallback)

    /**
     * 验证手机验证码
     */
    fun verifyPhoneCode(
        type: String, countryCode: String, phone: String, code: String, callback: MyCallback
    )

    /**
     * 验证邮箱验证码
     */
    fun verifyEmailCode(type: String, email: String, code: String, callback: MyCallback)

}