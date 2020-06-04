package com.tenext.auth.service

import com.tenext.auth.callback.MyCallback
import com.tenext.auth.consts.RequestCode
import com.tenext.auth.impl.VerifyImpl

/**
 * 验证码相关Service
 */
internal class VerifyService : BaseService(), VerifyImpl {

    companion object {
        const val register_type = "register"
        const val reset_pwd_type = "resetpass"
        const val bind_phone_type = "register"
    }

    /**
     * 获取手机验证码
     */
    override fun sendPhoneCode(
        type: String, countryCode: String, phone: String, callback: MyCallback
    ) {
        val param = commonParams("AppSendVerificationCode")
        param["Type"] = type
        param["CountryCode"] = countryCode
        param["PhoneNumber"] = phone
        postJson(param, callback, RequestCode.send_phone_code)
    }

    /**
     * 获取手机验证码
     */
    override fun sendEmailCode(type: String, email: String, callback: MyCallback) {
        val param = commonParams("AppSendEmailVerificationCode")
        param["Type"] = type
        param["Email"] = email
        postJson(param, callback, RequestCode.send_email_code)
    }

    /**
     * 验证手机验证码
     */
    override fun verifyPhoneCode(
        type: String, countryCode: String, phone: String, code: String, callback: MyCallback
    ) {
        val param = commonParams("AppCheckVerificationCode")
        param["Type"] = type
        param["CountryCode"] = countryCode
        param["PhoneNumber"] = phone
        param["VerificationCode"] = code
        postJson(param, callback, RequestCode.check_phone_code)
    }

    /**
     * 验证邮箱验证码
     */
    override fun verifyEmailCode(type: String, email: String, code: String, callback: MyCallback) {
        val param = commonParams("AppCheckEmailVerificationCode")
        param["Type"] = type
        param["Email"] = email
        param["VerificationCode"] = code
        postJson(param, callback, RequestCode.check_email_code)
    }

}