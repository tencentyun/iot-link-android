package com.tencent.iot.explorer.link.mvp.presenter

import com.tencent.iot.explorer.link.mvp.ParentPresenter
import com.tencent.iot.explorer.link.mvp.model.LogoutModel
import com.tencent.iot.explorer.link.mvp.view.LogoutView

class LogoutPresenter: ParentPresenter<LogoutModel, LogoutView> {
    constructor(view: LogoutView) : super(view)

    override fun getIModel(view: LogoutView): LogoutModel {
        return LogoutModel(view)
    }

    fun cancelAccount() {
        model?.cancelAccount()
    }
}