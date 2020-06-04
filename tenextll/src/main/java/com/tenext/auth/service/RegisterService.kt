package com.tenext.auth.service

import com.tenext.auth.IoTAuth
import com.tenext.auth.callback.MyCallback
import com.tenext.auth.consts.RequestCode
import com.tenext.auth.impl.RegisterImpl
import com.tenext.auth.response.BaseResponse

internal class RegisterService : BaseService(), RegisterImpl {

    override fun sendPhoneCode(countryCode: String, phone: String, callback: MyCallback) {
        IoTAuth.verifyImpl.sendPhoneCode(
            VerifyService.register_type, countryCode, phone, callback
        )
    }

    override fun sendEmailCode(email: String, callback: MyCallback) {
        IoTAuth.verifyImpl.sendEmailCode(VerifyService.register_type, email, callback)
    }

    override fun checkPhoneCode(
        countryCode: String, phone: String, code: String, callback: MyCallback
    ) {
        IoTAuth.verifyImpl.verifyPhoneCode(
            VerifyService.register_type, countryCode, phone, code, callback
        )
    }

    override fun checkEmailCode(email: String, code: String, callback: MyCallback) {
        IoTAuth.verifyImpl.verifyEmailCode(VerifyService.register_type, email, code, callback)
    }

    override fun registerPhone(
        countryCode: String, phone: String, code: String, pwd: String, callback: MyCallback
    ) {
        val param = commonParams("AppCreateCellphoneUser")
        param["CountryCode"] = countryCode
        param["PhoneNumber"] = phone
        param["VerificationCode"] = code
        param["Password"] = pwd
        postJson(param, callback, RequestCode.phone_register)
    }

    override fun registerEmail(
        email: String, code: String, pwd: String, callback: MyCallback
    ) {
        val param = commonParams("AppCreateEmailUser")
        param["Email"] = email
        param["VerificationCode"] = code
        param["Password"] = pwd
        postJson(param, callback, RequestCode.email_register)
    }
}