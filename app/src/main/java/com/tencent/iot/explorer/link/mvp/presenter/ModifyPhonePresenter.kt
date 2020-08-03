package com.tencent.iot.explorer.link.mvp.presenter

import com.tencent.iot.explorer.link.mvp.ParentPresenter
import com.tencent.iot.explorer.link.mvp.model.ModifyPhoneModel
import com.tencent.iot.explorer.link.mvp.view.ModifyEmailView
import com.tencent.iot.explorer.link.mvp.view.ModifyPhoneView

class ModifyPhonePresenter : ParentPresenter<ModifyPhoneModel, ModifyPhoneView> {
    constructor(view: ModifyPhoneView) : super(view)

    override fun getIModel(view: ModifyPhoneView): ModifyPhoneModel {
        return ModifyPhoneModel(view)
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
        model?.verifyCode= code
    }

    fun requestPhoneCode() {
        model?.requestPhoneCode()
    }

    fun modifyPhone() {
        model?.modifyPhone()
    }
}