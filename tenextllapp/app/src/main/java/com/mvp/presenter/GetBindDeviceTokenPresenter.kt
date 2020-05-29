package com.mvp.presenter

import com.mvp.ParentPresenter
import com.mvp.model.GetBindDeviceTokenModel
import com.mvp.view.GetBindDeviceTokenView

class GetBindDeviceTokenPresenter : ParentPresenter<GetBindDeviceTokenModel, GetBindDeviceTokenView> {

    constructor(view: GetBindDeviceTokenView) : super(view)

    override fun getIModel(view: GetBindDeviceTokenView): GetBindDeviceTokenModel {
        return GetBindDeviceTokenModel(view)
    }

    fun getBindDeviceToken () {
        return model!!.getBindDeviceToken()
    }
}