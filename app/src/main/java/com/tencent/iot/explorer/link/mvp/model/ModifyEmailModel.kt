package com.tencent.iot.explorer.link.mvp.model

import com.tencent.iot.explorer.link.kitlink.consts.SocketConstants
import com.tencent.iot.explorer.link.kitlink.response.BaseResponse
import com.tencent.iot.explorer.link.kitlink.util.HttpRequest
import com.tencent.iot.explorer.link.kitlink.util.MyCallback
import com.tencent.iot.explorer.link.mvp.ParentModel
import com.tencent.iot.explorer.link.mvp.view.ModifyEmailView
import com.tencent.iot.explorer.link.util.T

class ModifyEmailModel(view: ModifyEmailView) : ParentModel<ModifyEmailView>(view), MyCallback {

    var email: String = ""
    var verifyCode: String = ""

    fun requestEmailVerifyCode() {
        HttpRequest.instance.sendEmailCode(SocketConstants.register, email, this)
    }

    fun modifyEmail() {
        HttpRequest.instance.modifyEmail(email, verifyCode, this)
    }

    override fun fail(msg: String?, reqCode: Int) {

    }

    override fun success(response: BaseResponse, reqCode: Int) {
        if (response.isSuccess()) {
        }
    }
}