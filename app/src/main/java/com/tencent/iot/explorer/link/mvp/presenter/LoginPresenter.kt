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

    fun setCountryCode(countryCode: String) {
        model?.setCountryCode(countryCode)
    }

    fun setPhoneData(phone: String, pwd: String) {
        model?.phone = phone
        model?.pwd = pwd
    }

    fun setEmailData(email: String, pwd: String) {
        model?.email = email
        model?.pwd = pwd
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

}