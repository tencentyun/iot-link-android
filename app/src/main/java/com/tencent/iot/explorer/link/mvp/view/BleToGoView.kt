package com.tencent.iot.explorer.link.mvp.view

import com.tencent.iot.explorer.link.mvp.ParentView

interface BleToGoView : ParentView {
    fun onGoH5Ble(productId: String)
    fun onNeedCheckProductConfig(productId: String)
    fun onGoNormalPage(productId: String)
    fun onRequestFailed(msg: String?)
}