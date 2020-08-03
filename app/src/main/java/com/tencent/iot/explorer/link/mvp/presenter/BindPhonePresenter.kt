package com.tencent.iot.explorer.link.mvp.presenter

import com.tencent.iot.explorer.link.mvp.ParentPresenter
import com.tencent.iot.explorer.link.mvp.model.BindPhoneModel
import com.tencent.iot.explorer.link.mvp.view.BindPhoneView

class BindPhonePresenter : ParentPresenter<BindPhoneModel, BindPhoneView> {
    constructor(view: BindPhoneView) : super(view)

    override fun getIModel(view: BindPhoneView): BindPhoneModel {
        return BindPhoneModel(view)
    }

    fun setCountryCode(countryCode: String) {
        model?.setCountryCode(countryCode)
    }

    fun getCountryCode(): String {
        return model!!.getCountryCode()
    }

    fun setPhone(phone: String) {
        model?.phone = phone
    }

    fun setVerifyCode(code: String) {
        model?.verifyCode = code
    }

    fun setPassword(passwd: String) {
        model?.passwd = passwd
    }

    fun requestPhoneCode() {
        model?.requestPhoneCode()
    }

    fun bindPhone() {
        model?.bindPhone()
    }
}