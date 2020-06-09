package com.tenext.auth.service

import com.tenext.auth.IoTAuth
import com.tenext.auth.callback.MyCallback
import com.tenext.auth.consts.RequestCode
import com.tenext.auth.impl.UserImpl

/**
 * 用户信息相关Service
 */
internal class UserService : BaseService(), UserImpl {

    override fun logout(callback: MyCallback) {
        tokenPost(
            tokenParams("AppLogoutUser"),
            callback,
            RequestCode.logout
        )
    }

    override fun userInfo(callback: MyCallback) {
        tokenPost(tokenParams("AppGetUser"), callback, RequestCode.user_info)
    }

    override fun feedback(advise: String, phone: String, pic: String, callback: MyCallback) {
        val param = tokenParams("AppUserFeedBack")
        param["Type"] = "advise"
        param["Desc"] = advise
        param["Contact"] = phone
        param["LogUrl"] = pic
        tokenPost(param, callback, RequestCode.feedback)
    }

    override fun sendBindPhoneCode(countryCode: String, phone: String, callback: MyCallback) {
        IoTAuth.verifyImpl.sendPhoneCode(
            VerifyService.bind_phone_type, countryCode, phone, callback
        )
    }

    override fun checkBindPhoneCode(
        countryCode: String, phone: String, code: String, callback: MyCallback
    ) {
        IoTAuth.verifyImpl.verifyPhoneCode(
            VerifyService.bind_phone_type, countryCode, phone, code, callback
        )
    }

    override fun bindPhone(countryCode: String, phone: String, code: String, callback: MyCallback) {
        val param = tokenParams("AppUpdateUser")
        param["CountryCode"] = countryCode
        param["PhoneNumber"] = phone
        param["VerificationCode"] = code
        tokenPost(param, callback, RequestCode.bind_phone)
    }

    override fun modifyAlias(alias: String, callback: MyCallback) {
        val param = tokenParams("AppUpdateUser")
        param["NickName"] = alias
        updateUserInfo(param, callback, RequestCode.modify_alias)
    }

    override fun modifyPortrait(avatar: String, callback: MyCallback) {
        val param = tokenParams("AppUpdateUser")
        param["Avatar"] = avatar
        tokenPost(param, callback, RequestCode.modify_portrait)
    }

    /**
     * 更新用户信息
     */
    private fun updateUserInfo(param: HashMap<String, Any>, callback: MyCallback, reqCode: Int) {
        tokenPost(param, callback, reqCode)
    }

    /**
     *  查找手机用户
     */
    override fun findPhoneUser(phone: String, countryCode: String, callback: MyCallback) {
        val param = tokenParams("AppFindUser")
        param["CountryCode"] = countryCode
        param["PhoneNumber"] = phone
        param["Type"] = "phone"
        tokenPost(param, callback, RequestCode.find_phone_user)
    }

    /**
     *  查找邮箱用户
     */
    override fun findEmailUser(email: String, callback: MyCallback) {
        val param = tokenParams("AppFindUser")
        param["Email"] = email
        param["Type"] = "email"
        tokenPost(param, callback, RequestCode.find_email_user)
    }

}