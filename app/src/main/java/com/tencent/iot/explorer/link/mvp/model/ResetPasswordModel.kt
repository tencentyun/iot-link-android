package com.tencent.iot.explorer.link.mvp.model

import com.tencent.iot.explorer.link.core.log.L
import com.tencent.iot.explorer.link.kitlink.response.BaseResponse
import com.tencent.iot.explorer.link.kitlink.util.HttpRequest
import com.tencent.iot.explorer.link.kitlink.util.MyCallback
import com.tencent.iot.explorer.link.mvp.ParentModel
import com.tencent.iot.explorer.link.mvp.view.ResetPasswordView

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