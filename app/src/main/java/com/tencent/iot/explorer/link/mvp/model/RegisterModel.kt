package com.tencent.iot.explorer.link.mvp.model

import com.tencent.iot.explorer.link.kitlink.consts.SocketConstants
import com.tencent.iot.explorer.link.kitlink.response.BaseResponse
import com.tencent.iot.explorer.link.kitlink.util.HttpRequest
import com.tencent.iot.explorer.link.kitlink.util.MyCallback
import com.tencent.iot.explorer.link.kitlink.util.RequestCode
import com.tencent.iot.explorer.link.mvp.ParentModel
import com.tencent.iot.explorer.link.mvp.view.RegisterView

class RegisterModel(view: RegisterView) : ParentModel<RegisterView>(view), MyCallback {

    var phone = ""
    var email = ""
    private var isAgree = false
    private var countryCode = "86"
    private var countryName = "中国大陆"
    private val type = SocketConstants.register

    fun setCountryCode(countryCode: String) {
        if (!countryCode.contains("+")) return
        countryCode.split("+").let {
            this.countryName = it[0]
            this.countryCode = it[1]
            view?.showCountryCode(this.countryCode, this.countryName)
        }
    }

    fun getCountryCode(): String {
        return countryCode
    }

    fun agreement() {
        isAgree = !isAgree
        view?.agreement(isAgree)
    }

    fun isAgreement(): Boolean {
        if (!isAgree) {//未同意协议
            view?.unselectedAgreement()
        }
        return isAgree
    }

    /**
     * 获取手机验证码
     */
    fun requestPhoneCode() {
        HttpRequest.instance.sendMobileCode(type, countryCode, phone, this)
    }

    /**
     * 获取邮箱验证码
     */
    fun requestEmailCode() {
        HttpRequest.instance.sendEmailCode(type, email, this)
    }

    override fun fail(msg: String?, reqCode: Int) {
    }

    override fun success(response: BaseResponse, reqCode: Int) {
        if (response.isSuccess()) {
            when (reqCode) {
                RequestCode.send_mobile_code -> view?.sendSmsCodeSuccess()
                RequestCode.send_email_code -> view?.sendEmailCodeSuccess()
                else -> {
                }
            }
        } else {
            view?.sendCodeFail(response.msg)
        }

    }

}