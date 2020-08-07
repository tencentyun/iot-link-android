package com.tencent.iot.explorer.link.mvp.model

import com.tencent.iot.explorer.link.kitlink.response.BaseResponse
import com.tencent.iot.explorer.link.kitlink.util.HttpRequest
import com.tencent.iot.explorer.link.kitlink.util.MyCallback
import com.tencent.iot.explorer.link.mvp.ParentModel
import com.tencent.iot.explorer.link.mvp.view.LogoutView
import com.tencent.iot.explorer.link.util.T

class LogoutModel(view: LogoutView) : ParentModel<LogoutView>(view), MyCallback {

    fun cancelAccount() {
        HttpRequest.instance.cancelAccount(this)
    }

    override fun fail(msg: String?, reqCode: Int) {
        T.show(msg)
    }

    override fun success(response: BaseResponse, reqCode: Int) {
        if (response.isSuccess()) {

        }
    }
}