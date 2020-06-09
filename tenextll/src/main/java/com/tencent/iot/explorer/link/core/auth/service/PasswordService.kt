package com.tencent.iot.explorer.link.core.auth.service

import com.tencent.iot.explorer.link.core.auth.IoTAuth
import com.tencent.iot.explorer.link.core.auth.callback.MyCallback
import com.tencent.iot.explorer.link.core.auth.consts.RequestCode
import com.tencent.iot.explorer.link.core.auth.impl.PasswordImpl

internal class PasswordService : BaseService(), PasswordImpl {

    override fun sendPhoneCode(countryCode: String, phone: String, callback: MyCallback) {
        IoTAuth.verifyImpl.sendPhoneCode(
            VerifyService.reset_pwd_type, countryCode, phone, callback
        )
    }

    override fun sendEmailCode(email: String, callback: MyCallback) {
        IoTAuth.verifyImpl.sendEmailCode(VerifyService.reset_pwd_type, email, callback)
    }

    override fun checkPhoneCode(
        countryCode: String, phone: String, code: String, callback: MyCallback
    ) {
        IoTAuth.verifyImpl.verifyPhoneCode(
            VerifyService.reset_pwd_type, countryCode, phone, code, callback
        )
    }

    override fun checkEmailCode(email: String, code: String, callback: MyCallback) {
        IoTAuth.verifyImpl.verifyEmailCode(VerifyService.reset_pwd_type, email, code, callback)
    }

    override fun resetEmailPassword(
        email: String, code: String, pwd: String, callback: MyCallback
    ) {
        val param = tokenParams("AppResetPasswordByEmail")
        param["Email"] = email
        param["VerificationCode"] = code
        param["Password"] = pwd
        postJson(param, callback, RequestCode.email_reset_pwd)
    }

    override fun resetPhonePassword(
        countryCode: String, phone: String, code: String, pwd: String, callback: MyCallback
    ) {
        val param = tokenParams("AppResetPasswordByCellphone")
        param["CountryCode"] = countryCode
        param["PhoneNumber"] = phone
        param["VerificationCode"] = code
        param["Password"] = pwd
        postJson(param, callback, RequestCode.phone_reset_pwd)
    }

    override fun resetPassword(oldPwd: String, pwd: String, callback: MyCallback) {
        val param = tokenParams("AppUserResetPassword")
        param["Password"] = oldPwd
        param["NewPassword"] = pwd
        tokenPost(param, callback, RequestCode.reset_password)
    }

}