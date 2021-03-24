package com.tencent.iot.explorer.link.mvp.model

import com.tencent.iot.explorer.link.core.auth.callback.MyCallback
import com.tencent.iot.explorer.link.core.auth.response.BaseResponse
import com.tencent.iot.explorer.link.core.auth.response.DeviceProductResponse
import com.tencent.iot.explorer.link.kitlink.util.HttpRequest
import com.tencent.iot.explorer.link.kitlink.util.RequestCode
import com.tencent.iot.explorer.link.mvp.ParentModel
import com.tencent.iot.explorer.link.mvp.view.BleToGoView

class BleToGoModel(view: BleToGoView) : ParentModel<BleToGoView>(view), MyCallback {

    fun checkProductConfig(procudtId: String) {
        val productsList  = arrayListOf<String>()
        productsList.add(procudtId)
        HttpRequest.instance.deviceProducts(productsList, this)
    }

    override fun fail(msg: String?, reqCode: Int) {
        view?.onRequestFailed(msg)
    }

    override fun success(response: BaseResponse, reqCode: Int) {
        when (reqCode) {
            RequestCode.device_product -> {
                if (!response.isSuccess()) {
                    view?.onRequestFailed(response.msg)
                    return
                }

                response.parse(DeviceProductResponse::class.java)?.run {
                    if (Products.isNotEmpty()) {
                        val product = Products[0]
                        if (product.NetType == "else") {
                            view?.onGoH5Ble(product.ProductId)
                        } else if (product.NetType == "ble") {
                            view?.onNeedCheckProductConfig(product.ProductId)
                        } else {
                            view?.onGoNormalPage(product.ProductId)
                        }
                    }
                }
            }
        }
    }
}