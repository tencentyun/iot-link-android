package com.tencent.iot.explorer.link.mvp.presenter

import com.tencent.iot.explorer.link.mvp.ParentPresenter
import com.tencent.iot.explorer.link.mvp.model.DeviceDetailModel
import com.tencent.iot.explorer.link.mvp.view.DeviceDetailView

class DeviceDetailPresenter : ParentPresenter<DeviceDetailModel, DeviceDetailView> {
    constructor(view: DeviceDetailView) : super(view)

    override fun getIModel(view: DeviceDetailView): DeviceDetailModel {
        return DeviceDetailModel(view)
    }

    fun deleteDevice(productId: String, deviceName: String) {
        model?.deleteDevice(productId, deviceName)
    }
}