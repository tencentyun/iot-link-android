package com.tencent.iot.explorer.link.mvp.model

import com.tencent.iot.explorer.link.ErrorMessage
import com.tencent.iot.explorer.link.R
import com.tencent.iot.explorer.link.kitlink.consts.SocketConstants
import com.tencent.iot.explorer.link.kitlink.response.BaseResponse
import com.tencent.iot.explorer.link.kitlink.util.HttpRequest
import com.tencent.iot.explorer.link.kitlink.util.MyCallback
import com.tencent.iot.explorer.link.kitlink.util.RequestCode
import com.tencent.iot.explorer.link.mvp.ParentModel
import com.tencent.iot.explorer.link.mvp.view.BindPhoneView
import com.tencent.iot.explorer.link.util.T

class BindPhoneModel(view: BindPhoneView) : ParentModel<BindPhoneView>(view), MyCallback {

    var phone: String = ""
    private var countryCode = "86"
    private var countryName = T.getContext().getString(R.string.china_main_land)//"中国大陆"
    var passwd: String = ""
    var verifyCode: String = ""

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

    fun requestPhoneCode() {
        HttpRequest.instance.sendMobileCode(SocketConstants.register, countryCode, phone, this)
    }

    fun bindPhone() {
        HttpRequest.instance.bindPhone(countryCode, phone, verifyCode, passwd, this)
    }

    override fun fail(msg: String?, reqCode: Int) {
        T.show(msg)
    }

    override fun success(response: BaseResponse, reqCode: Int) {
        when (reqCode) {
            RequestCode.send_mobile_code -> {// 发送验证码
                if (response.isSuccess()) {
                    view?.sendVerifyCodeSuccess()
                } else {
                    val errMsg = ErrorMessage.parseErrorMessage(response.data.toString())
                    view?.sendVerifyCodeFail(errMsg)
                }
            }
            RequestCode.update_user_info -> {// 绑定
                if (response.isSuccess()) {
                    view?.bindSuccess()
                } else {
                    val errMsg = ErrorMessage.parseErrorMessage(response.data.toString())
                    view?.bindFail(errMsg)
                }
            }
        }

    }
}