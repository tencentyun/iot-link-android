package com.mvp.model

import com.kitlink.App
import com.kitlink.response.BaseResponse
import com.kitlink.util.HttpRequest
import com.kitlink.util.MyCallback
import com.mvp.ParentModel
import com.mvp.view.DeviceDetailView
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