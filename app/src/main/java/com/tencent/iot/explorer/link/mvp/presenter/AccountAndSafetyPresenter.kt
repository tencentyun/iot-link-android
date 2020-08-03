package com.tencent.iot.explorer.link.mvp.presenter

import com.tencent.iot.explorer.link.mvp.ParentPresenter
import com.tencent.iot.explorer.link.mvp.model.AccountAndSafetyModel
import com.tencent.iot.explorer.link.mvp.view.AccountAndSafetyView

class AccountAndSafetyPresenter : ParentPresenter<AccountAndSafetyModel, AccountAndSafetyView> {
    constructor(view: AccountAndSafetyView) : super(view)

    override fun getIModel(view: AccountAndSafetyView): AccountAndSafetyModel {
        return AccountAndSafetyModel(view)
    }

    fun getUserInfo() {
        model?.getUserInfo()
    }
}