package com.tencent.iot.explorer.link.mvp.presenter

import com.tencent.iot.explorer.link.mvp.ParentPresenter
import com.tencent.iot.explorer.link.mvp.model.GetBindDeviceTokenModel
import com.tencent.iot.explorer.link.mvp.view.GetBindDeviceTokenView

class GetBindDeviceTokenPresenter : ParentPresenter<GetBindDeviceTokenModel, GetBindDeviceTokenView> {

    constructor(view: GetBindDeviceTokenView) : super(view)

    override fun getIModel(view: GetBindDeviceTokenView): GetBindDeviceTokenModel {
        return GetBindDeviceTokenModel(view)
    }

    fun getBindDeviceToken () {
        return model!!.getBindDeviceToken()
    }
}