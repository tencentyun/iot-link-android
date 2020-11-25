package com.tencent.iot.explorer.link.mvp.model

import com.tencent.iot.explorer.link.ErrorMessage
import com.tencent.iot.explorer.link.kitlink.consts.SocketConstants
import com.tencent.iot.explorer.link.kitlink.util.HttpRequest
import com.tencent.iot.explorer.link.core.auth.callback.MyCallback
import com.tencent.iot.explorer.link.kitlink.util.RequestCode
import com.tencent.iot.explorer.link.mvp.ParentModel
import com.tencent.iot.explorer.link.mvp.view.BindEmailView
import com.tencent.iot.explorer.link.T
import com.tencent.iot.explorer.link.core.auth.response.BaseResponse

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
        T.show(msg)
    }

    override fun success(response: BaseResponse, reqCode: Int) {
        when (reqCode) {
            RequestCode.send_email_code -> {// 发送邮箱验证码
                if (response.isSuccess()) {
                    view?.sendVerifyCodeSuccess()
                } else {
                    val errMsg = ErrorMessage.parseErrorMessage(response.data.toString())
                    view?.sendVerifyCodeFail(errMsg)
                }
            }
            RequestCode.update_user_info -> {// 绑定邮箱响应
                if (response.isSuccess()) {
                    view?.bindSuccess()
                } else {
                    val errMsg = ErrorMessage.parseErrorMessage(response.data.toString())
                    view?.bindFail(errMsg)
                }
            }
        }
    }
}