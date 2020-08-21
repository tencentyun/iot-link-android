package com.tencent.iot.explorer.link.mvp.presenter

import com.tencent.iot.explorer.link.mvp.model.LoginModel
import com.tencent.iot.explorer.link.mvp.ParentPresenter
import com.tencent.iot.explorer.link.mvp.view.LoginView

class LoginPresenter : ParentPresenter<LoginModel, LoginView> {
    constructor(view: LoginView) : super(view)

    override fun getIModel(view: LoginView): LoginModel {
        return LoginModel(view)
    }

    fun getCountryCode(): String {
        return model!!.getCountryCode()
    }

    fun setCountry(country: String) {
        model?.setCountry(country)
    }

    fun setPhone(phone: String) {
        model?.phone = phone
    }

    fun setEmail(email: String) {
        model?.email = email
    }

    fun setPhoneData(phone: String, pwd: String) {
        model?.phone = phone
        model?.pwd = pwd
    }

    fun setEmailData(email: String, pwd: String) {
        model?.email = email
        model?.pwd = pwd
    }

    fun setVerifyCode(verifyCode: String) {
        model?.verifyCode = verifyCode
    }

    fun phoneCommit() {
        model?.phoneCommit()
    }

    fun emailCommit() {
        model?.emailCommit()
    }

    fun wechatLogin(reqCode: String) {
        model?.wechatLogin(reqCode)
    }

    fun requestPhoneCode() {
        model?.requestPhoneCode()
    }

    fun requestEmailCode() {
        model?.requestEmailCode()
    }

    fun phoneVerifyCodeCommit() {
        model?.phoneVerifyCodeCommit()
    }

    fun emailVerifyCodeCommit() {
        model?.emailVerifyCodeCommit()
    }
}