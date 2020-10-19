package com.tencent.iot.explorer.link.mvp.model

import com.tencent.iot.explorer.link.ErrorMessage
import com.tencent.iot.explorer.link.kitlink.consts.SocketConstants
import com.tencent.iot.explorer.link.kitlink.response.BaseResponse
import com.tencent.iot.explorer.link.kitlink.util.HttpRequest
import com.tencent.iot.explorer.link.kitlink.util.MyCallback
import com.tencent.iot.explorer.link.kitlink.util.RequestCode
import com.tencent.iot.explorer.link.mvp.ParentModel
import com.tencent.iot.explorer.link.mvp.view.ModifyEmailView
import com.tencent.iot.explorer.link.T

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
        T.show(msg)
    }

    override fun success(response: BaseResponse, reqCode: Int) {
        when (reqCode) {
            RequestCode.send_email_code -> {// 发送验证码
                if (!response.isSuccess()) {
                    val errMsg = ErrorMessage.parseErrorMessage(response.data.toString())
                    view?.sendVerifyCodeFail(errMsg)
                } else {
                    view?.sendVerifyCodeSuccess()
                }
            }
            RequestCode.update_user_info -> {// 更新邮箱号
                if (!response.isSuccess()) {
                    val errMsg = ErrorMessage.parseErrorMessage(response.data.toString())
                    view?.updateEmailFail(errMsg)
                } else {
                    view?.updateEmailSuccess()
                }
            }
        }
    }
}