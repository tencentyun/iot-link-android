package com.tencent.iot.explorer.link.mvp.model

import android.text.TextUtils
import com.tencent.iot.explorer.link.App
import com.tencent.iot.explorer.link.core.auth.response.BindDeviceTokenResponse
import com.tencent.iot.explorer.link.kitlink.response.BaseResponse
import com.tencent.iot.explorer.link.kitlink.util.HttpRequest
import com.tencent.iot.explorer.link.kitlink.util.MyCallback
import com.tencent.iot.explorer.link.mvp.ParentModel
import com.tencent.iot.explorer.link.mvp.view.GetBindDeviceTokenView

class GetBindDeviceTokenModel(view: GetBindDeviceTokenView) : ParentModel<GetBindDeviceTokenView>(view), MyCallback {

    private var isCommit = false

    fun getBindDeviceToken () {
        if (isCommit) return
        HttpRequest.instance.getBindDevToken(this)
        isCommit = true
    }

    override fun fail(msg: String?, reqCode: Int) {
        isCommit = false
        view?.onFail(msg!!)
    }

    override fun success(response: BaseResponse, reqCode: Int) {
        isCommit = false
        if (response.isSuccess()) {
            response.parse(BindDeviceTokenResponse::class.java)?.Token.let {
                if (!TextUtils.isEmpty(it)) {
                    App.data.bindDeviceToken = it!!
                    view?.onSuccess(it!!)
                } else {
                    view?.onFail("token is empty")
                }
            }
        }
    }
}