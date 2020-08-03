package com.tencent.iot.explorer.link.mvp.model

import com.tencent.iot.explorer.link.kitlink.consts.SocketConstants
import com.tencent.iot.explorer.link.kitlink.response.BaseResponse
import com.tencent.iot.explorer.link.kitlink.util.HttpRequest
import com.tencent.iot.explorer.link.kitlink.util.MyCallback
import com.tencent.iot.explorer.link.mvp.ParentModel
import com.tencent.iot.explorer.link.mvp.view.BindEmailView

class BindEmailModel(view: BindEmailView) : ParentModel<BindEmailView>(view), MyCallback {

    var email: String = ""
    var passwd: String = ""
    var verifyCode: String = ""

    fun requestEmailVerifyCode() {
        HttpRequest.instance.sendEmailCode(SocketConstants.register, email, this)
    }

    fun bindEmail() {
        HttpRequest.instance.bindEmail(email, verifyCode, passwd, this)
    }

    override fun fail(msg: String?, reqCode: Int) {
        TODO("Not yet implemented")
    }

    override fun success(response: BaseResponse, reqCode: Int) {
        if (response.isSuccess()) {
        }
    }
}