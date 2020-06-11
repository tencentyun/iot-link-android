package com.tencent.iot.explorer.link.mvp.presenter

import com.tencent.iot.explorer.link.mvp.ParentPresenter
import com.tencent.iot.explorer.link.mvp.model.ControlPanelModel
import com.tencent.iot.explorer.link.mvp.view.ControlPanelView

class ControlPanelPresenter(view: ControlPanelView) : ParentPresenter<ControlPanelModel, ControlPanelView>(view) {
    override fun getIModel(view: ControlPanelView): ControlPanelModel {
        return ControlPanelModel(view)
    }

    fun setProductId(productId: String) {
        model?.productId = productId
    }

    fun setDeviceName(deviceName: String) {
        model?.deviceName = deviceName
    }

    fun setDeviceId(deviceId: String) {
        model?.deviceId = deviceId
    }

    fun requestDeviceData() {
        model?.requestDeviceData()
    }

    fun requestControlPanel() {
        model?.requestControlPanel()
    }

    /**
     * 注册设备监听
     */
    fun registerActivePush() {
        model?.registerActivePush()
    }

    /**
     *  上报数据
     */
    fun controlDevice(id: String, value: String) {
        model?.controlDevice(id, value)
    }
}