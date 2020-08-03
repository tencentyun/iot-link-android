package com.tencent.iot.explorer.link.mvp.presenter

import com.tencent.iot.explorer.link.mvp.ParentPresenter
import com.tencent.iot.explorer.link.mvp.model.ModifyEmailModel
import com.tencent.iot.explorer.link.mvp.view.ModifyEmailView

class ModifyEmailPresenter : ParentPresenter<ModifyEmailModel, ModifyEmailView> {
    constructor(view: ModifyEmailView) : super(view)

    override fun getIModel(view: ModifyEmailView): ModifyEmailModel {
        return ModifyEmailModel(view)
    }

    fun setEmail(email: String) {
        model?.email = email
    }

    fun setVerifyCode(code: String) {
        model?.verifyCode = code
    }

    fun requestEmailVerifyCode() {
        model?.requestEmailVerifyCode()
    }

    fun modifyEmail() {
        model?.modifyEmail()
    }
}