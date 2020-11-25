package com.tencent.iot.explorer.link.mvp.model

import com.tencent.iot.explorer.link.App
import com.tencent.iot.explorer.link.core.auth.response.BaseResponse
import com.tencent.iot.explorer.link.core.log.L
import com.tencent.iot.explorer.link.kitlink.response.UserInfoResponse
import com.tencent.iot.explorer.link.kitlink.util.HttpRequest
import com.tencent.iot.explorer.link.core.auth.callback.MyCallback
import com.tencent.iot.explorer.link.kitlink.util.RequestCode
import com.tencent.iot.explorer.link.mvp.ParentModel
import com.tencent.iot.explorer.link.mvp.view.AccountAndSafetyView

class AccountAndSafetyModel(view: AccountAndSafetyView) : ParentModel<AccountAndSafetyView>(view), MyCallback {

    override fun fail(msg: String?, reqCode: Int) {
        L.e(msg ?: "")
    }

    override fun success(response: BaseResponse, reqCode: Int) {
        if (response.isSuccess()) {
            when (reqCode) {
                RequestCode.user_info -> {
                    response.parse(UserInfoResponse::class.java)?.Data?.run {
                        App.data.userInfo = this
                        view?.showUserInfo()
                    }
                }
            }
        }
    }

    fun getUserInfo() {
        HttpRequest.instance.userInfo(this)
    }
}