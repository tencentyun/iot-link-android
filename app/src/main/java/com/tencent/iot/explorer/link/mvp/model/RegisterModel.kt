package com.tencent.iot.explorer.link.mvp.model

import com.tencent.iot.explorer.link.App
import com.tencent.iot.explorer.link.R
import com.tencent.iot.explorer.link.kitlink.consts.CommonField
import com.tencent.iot.explorer.link.kitlink.consts.SocketConstants
import com.tencent.iot.explorer.link.kitlink.response.BaseResponse
import com.tencent.iot.explorer.link.kitlink.util.HttpRequest
import com.tencent.iot.explorer.link.kitlink.util.MyCallback
import com.tencent.iot.explorer.link.kitlink.util.RequestCode
import com.tencent.iot.explorer.link.mvp.ParentModel
import com.tencent.iot.explorer.link.mvp.view.RegisterView
import com.tencent.iot.explorer.link.util.T

class RegisterModel(view: RegisterView) : ParentModel<RegisterView>(view), MyCallback {

    var phone = ""
    var email = ""
    private var isAgree = false
    private var countryCode = "86"
    private var countryName = T.getContext().getString(R.string.china_main_land) //"中国大陆"
    private var regionId = "1"
    private val type = SocketConstants.register

    fun setCountry(country: String) {
        country.split("+").let {
            this.countryName = it[0]
            this.regionId = it[1]
            this.countryCode = it[2]
            App.data.regionId = regionId
            App.data.region = it[3]
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