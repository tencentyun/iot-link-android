package com.tencent.iot.explorer.link.mvp.presenter

import com.tencent.iot.explorer.link.mvp.ParentPresenter
import com.tencent.iot.explorer.link.mvp.model.ResetPasswordModel
import com.tencent.iot.explorer.link.mvp.view.ResetPasswordView

class ResetPasswordPresenter(view: ResetPasswordView) :
    ParentPresenter<ResetPasswordModel, ResetPasswordView>(view) {
    override fun getIModel(view: ResetPasswordView): ResetPasswordModel {
        return ResetPasswordModel(view)
    }

    fun setPassword(oldPwd: String, pwd: String, verifyPwd: String) {
        model?.let {
            it.oldPassword = oldPwd
            it.password = pwd
            it.verifyPassword = verifyPwd
        }
    }

    fun commit() {
        model?.let {
            if (!it.verifyPassword()) return
            it.commit()
        }
    }

}