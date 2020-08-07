package com.tencent.iot.explorer.link.mvp.model

import com.tencent.iot.explorer.link.kitlink.consts.SocketConstants
import com.tencent.iot.explorer.link.kitlink.response.BaseResponse
import com.tencent.iot.explorer.link.kitlink.util.HttpRequest
import com.tencent.iot.explorer.link.kitlink.util.MyCallback
import com.tencent.iot.explorer.link.kitlink.util.RequestCode
import com.tencent.iot.explorer.link.mvp.ParentModel
import com.tencent.iot.explorer.link.mvp.view.ModifyPasswordView

class ModifyPasswordModel(view: ModifyPasswordView) : ParentModel<ModifyPasswordView>(view), MyCallback {

    var passwd: String = ""
    var verifyCode: String = ""
    var email: String = ""
    var phone: String = ""

    private var countryCode = "86"
    private var countryName = "中国大陆"

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
        HttpRequest.instance.sendMobileCode(SocketConstants.reset_pwd, countryCode, phone, this)
    }

    fun requestEmailCode() {
        HttpRequest.instance.sendEmailCode(SocketConstants.reset_pwd, email, this)
    }


    fun modifyPasswordByPhone() {
        HttpRequest.instance.resetPhonePassword(countryCode, phone, verifyCode, passwd, this)
    }

    fun modifyPasswordByEmail() {
        HttpRequest.instance.resetEmailPassword(email, verifyCode, passwd, this)
    }

    override fun fail(msg: String?, reqCode: Int) {
    }

    override fun success(response: BaseResponse, reqCode: Int) {
        when (reqCode) {
            RequestCode.phone_reset_pwd, RequestCode.email_reset_pwd -> {
                if (response.isSuccess()) {
                    view?.modifyPasswdSuccess()
                }
            }
        }
    }
}