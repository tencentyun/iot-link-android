package com.mvp.model

import com.kitlink.response.BaseResponse
import com.kitlink.util.HttpRequest
import com.kitlink.util.MyCallback
import com.mvp.ParentModel
import com.mvp.view.ResetPasswordView
import com.util.L

class ResetPasswordModel(view: ResetPasswordView) : ParentModel<ResetPasswordView>(view),
    MyCallback {

    var oldPassword = ""
    var password = ""
    var verifyPassword = ""

    fun verifyPassword(): Boolean {
        if (password != verifyPassword) {
            view?.verifyPwdFail()
            return false
        }
        return true
    }

    fun commit() {
        HttpRequest.instance.resetPassword(oldPassword, password, this)
    }

    override fun fail(msg: String?, reqCode: Int) {
        L.e(msg ?: "")
    }

    override fun success(response: BaseResponse, reqCode: Int) {
        if (response.isSuccess()) {
            view?.success()
        } else {
            view?.fail(response.msg)
        }
    }

}