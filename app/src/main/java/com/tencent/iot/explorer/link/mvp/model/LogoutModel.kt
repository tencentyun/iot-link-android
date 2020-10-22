package com.tencent.iot.explorer.link.mvp.model

import com.tencent.iot.explorer.link.kitlink.util.HttpRequest
import com.tencent.iot.explorer.link.kitlink.util.MyCallback
import com.tencent.iot.explorer.link.kitlink.util.RequestCode
import com.tencent.iot.explorer.link.mvp.ParentModel
import com.tencent.iot.explorer.link.mvp.view.LogoutView
import com.tencent.iot.explorer.link.T
import com.tencent.iot.explorer.link.core.auth.response.BaseResponse

class LogoutModel(view: LogoutView) : ParentModel<LogoutView>(view), MyCallback {

    private var isAgree = false

    fun agreement() {
        isAgree = !isAgree
        view?.agreement(isAgree)
    }

    fun isAgreement(): Boolean {
        if (!isAgree) {//未同意协议
            view?.unselectedAgreement()
        }
        return isAgree
    }

    fun cancelAccount() {
        HttpRequest.instance.cancelAccount(this)
    }

    override fun fail(msg: String?, reqCode: Int) {
        T.show(msg)
    }

    override fun success(response: BaseResponse, reqCode: Int) {
        if (response.isSuccess()) {
            when (reqCode) {
                RequestCode.cancel_account -> view?.cancelAccountSuccess()
            }
        } else {
            view?.cancelAccountFail(response.msg)
        }
    }
}