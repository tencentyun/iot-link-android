package com.mvp.model

import android.text.TextUtils
import android.util.Log
import com.alibaba.fastjson.JSON
import com.kitlink.App
import com.kitlink.response.BaseResponse
import com.kitlink.response.BindDeviceTokenResponse
import com.kitlink.util.HttpRequest
import com.kitlink.util.MyCallback
import com.mvp.ParentModel
import com.mvp.view.GetBindDeviceTokenView

class GetBindDeviceTokenModel(view: GetBindDeviceTokenView) : ParentModel<GetBindDeviceTokenView>(view), MyCallback {

    private var isCommit = false

    fun getBindDeviceToken () {
        if (isCommit) return
        HttpRequest.instance.getBindDevToken(this)
        isCommit = true
    }

    override fun fail(msg: String?, reqCode: Int) {
        isCommit = false
        Log.e("XXX", "GetBindDeviceTokenModel fail msg:" + msg)
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