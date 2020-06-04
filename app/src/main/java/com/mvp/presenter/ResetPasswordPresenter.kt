package com.mvp.presenter

import com.mvp.ParentPresenter
import com.mvp.model.ResetPasswordModel
import com.mvp.view.ResetPasswordView

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