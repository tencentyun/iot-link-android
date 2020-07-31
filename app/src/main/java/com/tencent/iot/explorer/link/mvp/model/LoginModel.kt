package com.tencent.iot.explorer.link.mvp.model

import com.tencent.iot.explorer.link.App
import com.tencent.iot.explorer.link.kitlink.consts.SocketConstants
import com.tencent.iot.explorer.link.kitlink.entity.ParentRespEntity
import com.tencent.iot.explorer.link.kitlink.response.BaseResponse
import com.tencent.iot.explorer.link.kitlink.response.LoginResponse
import com.tencent.iot.explorer.link.kitlink.util.HttpRequest
import com.tencent.iot.explorer.link.kitlink.util.MyCallback
import com.tencent.iot.explorer.link.mvp.ParentModel
import com.tencent.iot.explorer.link.mvp.view.LoginView

class LoginModel(view: LoginView) : ParentModel<LoginView>(view), MyCallback {

    var phone: String = ""
    var email: String = ""
    var pwd: String = ""
    var verifyCode: String = ""

    private var countryName = "中国大陆"
    private var countryCode = "86"
    private var isCommit = false

    fun getCountryCode(): String {
        return countryCode
    }

    fun setCountryCode(countryCode: String) {
        if (!countryCode.contains("+")) return
        countryCode.split("+").let {
            this.countryName = it[0]
            this.countryCode = it[1]
            view?.showCountryCode(this.countryName, this.countryCode)
        }
    }

    fun phoneCommit() {
        if (isCommit) return
        HttpRequest.instance.phoneLogin(countryCode, phone, pwd, this)
        isCommit = true
    }

    fun emailCommit() {
        if (isCommit) return
        HttpRequest.instance.emailLogin(email, pwd, this)
        isCommit = true
    }

    fun wechatLogin(reqCode: String) {
        if (isCommit) return
        HttpRequest.instance.wechatLogin(reqCode, this)
        isCommit = true
    }

    fun requestPhoneCode() {
        HttpRequest.instance.sendMobileCode(SocketConstants.login, countryCode, phone, this)
    }

    fun requestEmailCode() {
        HttpRequest.instance.sendEmailCode(SocketConstants.login, email, this)
    }

    fun phoneVerifyCodeCommit() {
        if (isCommit) return
        HttpRequest.instance.phoneVerifyCodeLogin(countryCode, phone, verifyCode, this)
        isCommit = true
    }

    fun emailVerifyCodeCommit() {
        if (isCommit) return
        HttpRequest.instance.emailVerifyCodeLogin(email, verifyCode, this)
        isCommit = true
    }

    override fun fail(msg: String?, reqCode: Int) {
        isCommit = false
    }

    override fun success(response: BaseResponse, reqCode: Int) {
        isCommit = false
        if (response.isSuccess()) {
            response.parse(LoginResponse::class.java)?.Data?.let {
                //登录成功
                App.data.setAppUser(it)
                view?.loginSuccess(it)
                return
            }
        }
        view?.loginFail(response)
    }

}