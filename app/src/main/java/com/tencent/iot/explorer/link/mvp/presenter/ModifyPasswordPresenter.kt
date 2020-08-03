package com.tencent.iot.explorer.link.mvp.presenter

import com.tencent.iot.explorer.link.mvp.ParentPresenter
import com.tencent.iot.explorer.link.mvp.model.ModifyPasswordModel
import com.tencent.iot.explorer.link.mvp.view.ModifyPasswordView

class ModifyPasswordPresenter : ParentPresenter<ModifyPasswordModel, ModifyPasswordView> {
    constructor(view: ModifyPasswordView) : super(view)

    override fun getIModel(view: ModifyPasswordView): ModifyPasswordModel {
        return ModifyPasswordModel(view)
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

    fun setEmail(email: String) {
        model?.email = email
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

    fun requestEmailCode() {
        model?.requestEmailCode()
    }

    fun modifyPasswordByPhone() {
        model?.modifyPasswordByPhone()
    }

    fun modifyPasswordByEmail() {
        model?.modifyPasswordByEmail()
    }
}