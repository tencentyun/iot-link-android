package com.tenext.auth.service

import com.tenext.auth.IoTAuth
import com.tenext.auth.callback.LoginCallback
import com.tenext.auth.callback.MyCallback
import com.tenext.auth.consts.RequestCode
import com.tenext.auth.entity.User
import com.tenext.auth.impl.LoginImpl
import com.tenext.auth.response.BaseResponse
import com.tenext.auth.response.LoginResponse

internal class LoginService : BaseService(), LoginImpl {

    override fun loginPhone(
        countryCode: String, phone: String, pwd: String, callback: LoginCallback
    ) {
        val param = commonParams("AppGetToken")
        param["Type"] = "phone"
        param["CountryCode"] = countryCode
        param["PhoneNumber"] = phone
        param["Password"] = pwd
        postJson(param, object : MyCallback {
            override fun fail(msg: String?, reqCode: Int) {
                callback.fail(msg ?: "")
            }

            override fun success(response: BaseResponse, reqCode: Int) {
                if (response.isSuccess()) {
                    response.parse(LoginResponse::class.java)?.Data?.let {
                        loginSuccess(callback, it)
                        return
                    }
                }
                callback.fail(response.msg)
            }
        }, RequestCode.phone_login)
    }

    override fun loginEmail(email: String, pwd: String, callback: LoginCallback) {
        val param = commonParams("AppGetToken")
        param["Type"] = "email"
        param["Email"] = email
        param["Password"] = pwd
        postJson(param, object : MyCallback {
            override fun fail(msg: String?, reqCode: Int) {
                callback.fail(msg ?: "")
            }

            override fun success(response: BaseResponse, reqCode: Int) {
                if (response.isSuccess()) {
                    response.parse(LoginResponse::class.java)?.Data?.let {
                        //登录成功
                        loginSuccess(callback, it)
                        return
                    }
                }
                callback.fail(response.msg)
            }
        }, RequestCode.email_login)
    }

    override fun wechatLogin(code: String, callback: LoginCallback) {
        val param = commonParams("AppGetTokenByWeiXin")
        param["code"] = code
        param["busi"] = "studio"
        postJson(param, object : MyCallback {
            override fun fail(msg: String?, reqCode: Int) {
                callback.fail(msg ?: "")
            }

            override fun success(response: BaseResponse, reqCode: Int) {
                if (response.isSuccess()) {
                    response.parse(LoginResponse::class.java)?.Data?.let {
                        //登录成功
                        loginSuccess(callback, it)
                        return
                    }
                }
                callback.fail(response.msg)
            }
        }, RequestCode.wechat_login)
    }

    /**
     * 登录成功
     */
    private fun loginSuccess(callback: LoginCallback, user: User) {
        IoTAuth.user.ExpireAt = user.ExpireAt
        IoTAuth.user.Token = user.Token
        //登录成功
        callback.success(user)
    }
}