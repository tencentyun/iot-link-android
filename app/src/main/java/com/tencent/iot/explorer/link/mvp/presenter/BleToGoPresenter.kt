package com.tencent.iot.explorer.link.mvp.presenter

import com.tencent.iot.explorer.link.mvp.ParentPresenter
import com.tencent.iot.explorer.link.mvp.model.BleToGoModel
import com.tencent.iot.explorer.link.mvp.view.BleToGoView

class BleToGoPresenter : ParentPresenter<BleToGoModel, BleToGoView> {
    constructor(view: BleToGoView) : super(view)

    override fun getIModel(view: BleToGoView): BleToGoModel {
        return BleToGoModel(view)
    }

    fun checkProductConfig(procudtId: String) {
        model?.checkProductConfig(procudtId)
    }

}