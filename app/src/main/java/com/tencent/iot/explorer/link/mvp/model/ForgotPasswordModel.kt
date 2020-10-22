package com.tencent.iot.explorer.link.mvp.model

import com.tencent.iot.explorer.link.R
import com.tencent.iot.explorer.link.core.log.L
import com.tencent.iot.explorer.link.kitlink.consts.SocketConstants
import com.tencent.iot.explorer.link.kitlink.util.HttpRequest
import com.tencent.iot.explorer.link.kitlink.util.MyCallback
import com.tencent.iot.explorer.link.kitlink.util.RequestCode
import com.tencent.iot.explorer.link.mvp.ParentModel
import com.tencent.iot.explorer.link.mvp.view.ForgotPasswordView
import com.tencent.iot.explorer.link.T
import com.tencent.iot.explorer.link.core.auth.response.BaseResponse

class ForgotPasswordModel(view: ForgotPasswordView) : ParentModel<ForgotPasswordView>(view),
    MyCallback {

    private var countryCode = "86"
    private var countryName = T.getContext().getString(R.string.china_main_land)//"中国大陆"
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

    fun getAgreementStatus(): Boolean {
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