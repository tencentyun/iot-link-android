package com.tencent.iot.explorer.link.mvp.model

import com.tencent.iot.explorer.link.kitlink.App
import com.tencent.iot.explorer.link.kitlink.response.BaseResponse
import com.tencent.iot.explorer.link.kitlink.util.HttpRequest
import com.tencent.iot.explorer.link.kitlink.util.MyCallback
import com.tencent.iot.explorer.link.mvp.ParentModel
import com.tencent.iot.explorer.link.mvp.view.DeviceDetailView
import com.util.L

class DeviceDetailModel : ParentModel<DeviceDetailView>, MyCallback {


    constructor(view: DeviceDetailView) : super(view)

    /**
     * 删除设备
     */
    fun deleteDevice(productId: String, deviceName: String) {
        HttpRequest.instance.deleteDevice(App.data.getCurrentFamily().FamilyId, productId, deviceName, this)
    }

    override fun fail(msg: String?, reqCode: Int) {
        L.e(msg ?: "")
    }

    override fun success(response: BaseResponse, reqCode: Int) {
        if (response.isSuccess()) {
            view?.deleteSuccess()
            return
        }
        view?.fail(response.msg)

    }

}