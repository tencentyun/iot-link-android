package com.tencent.iot.explorer.link.mvp.view

import com.tencent.iot.explorer.link.mvp.ParentView

interface GetBindDeviceTokenView : ParentView {

    fun onSuccess(token: String)

    fun onFail(msg: String)

}