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

    fun getProductId(): String? {
        return model?.productId
    }

    fun setDeviceName(deviceName: String) {
        model?.deviceName = deviceName
    }

    fun getDeviceName(): String? {
        return model?.deviceName
    }

    fun setDeviceId(deviceId: String) {
        model?.deviceId = deviceId
    }

    fun getCategoryId(): Int? {
        return model?.categoryId
    }

    fun requestDeviceData() {
        model?.requestDeviceData()
    }

    fun requestDeviceDataByP2P() {
        model?.otherNeedP2PConnect = true
        model?.requestDeviceData()
    }

    fun removeReconnectCycleTasktask() {
        model?.removeReconnectCycleTasktask()
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

    fun getUserSetting() {
        model?.getUserSetting()
    }
}