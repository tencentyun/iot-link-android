package com.mvp.model

import com.kitlink.consts.SocketConstants
import com.kitlink.response.BaseResponse
import com.kitlink.util.HttpRequest
import com.kitlink.util.MyCallback
import com.kitlink.util.RequestCode
import com.mvp.ParentModel
import com.mvp.view.ForgotPasswordView
import com.util.L

class ForgotPasswordModel(view: ForgotPasswordView) : ParentModel<ForgotPasswordView>(view),
    MyCallback {

    private var countryCode = "86"
    private var countryName = "中国大陆"
    var phone = ""
    var email = ""
    private var agree = true


    fun getCountryCode(): String {
        return countryCode
    }

    fun setAgreement() {
        agree = !agree
        view?.agreement(agree)
    }

    fun isAgreement(): Boolean {
        if (!agree) {
            view?.unselectedAgreement()
        }
        return agree
    }

    fun setCountryCode(countryCode: String) {
        if (!countryCode.contains("+")) return
        countryCode.split("+").let {
            this.countryName = it[0]
            this.countryCode = it[1]
            view?.showCountryCode(this.countryCode, this.countryName)
        }
    }

    fun requestPhoneCode() {
        HttpRequest.instance.sendMobileCode(SocketConstants.reset_pwd, countryCode, phone, this)
    }

    fun requestEmailCode() {
        HttpRequest.instance.sendEmailCode(SocketConstants.reset_pwd, email, this)
    }

    override fun fail(msg: String?, reqCode: Int) {
        L.e(msg ?: "")
    }

    override fun success(response: BaseResponse, reqCode: Int) {
        if (response.isSuccess()) {
            when (reqCode) {
                RequestCode.send_email_code -> view?.sendEmailCodeSuccess(email)
                RequestCode.send_mobile_code -> view?.sendSmsCodeSuccess(phone, countryCode)
            }
        } else {
            view?.sendCodeFail(response.msg)
        }
    }
}