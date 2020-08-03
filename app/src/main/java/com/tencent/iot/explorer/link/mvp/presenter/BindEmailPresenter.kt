package com.tencent.iot.explorer.link.mvp.presenter

import com.tencent.iot.explorer.link.mvp.ParentPresenter
import com.tencent.iot.explorer.link.mvp.model.BindEmailModel
import com.tencent.iot.explorer.link.mvp.view.BindEmailView

class BindEmailPresenter : ParentPresenter<BindEmailModel, BindEmailView> {
    constructor(view: BindEmailView) : super(view)

    override fun getIModel(view: BindEmailView): BindEmailModel {
        return BindEmailModel(view)
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

    fun requestEmailVerifyCode() {
        model?.requestEmailVerifyCode()
    }

    fun bindEmail() {
        model?.bindEmail()
    }
}