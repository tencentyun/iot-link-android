package com.mvp.presenter

import com.mvp.ParentPresenter
import com.mvp.model.DeviceDetailModel
import com.mvp.view.DeviceDetailView

class DeviceDetailPresenter : ParentPresenter<DeviceDetailModel, DeviceDetailView> {
    constructor(view: DeviceDetailView) : super(view)

    override fun getIModel(view: DeviceDetailView): DeviceDetailModel {
        return DeviceDetailModel(view)
    }

    fun deleteDevice(productId: String, deviceName: String) {
        model?.deleteDevice(productId, deviceName)
    }
}