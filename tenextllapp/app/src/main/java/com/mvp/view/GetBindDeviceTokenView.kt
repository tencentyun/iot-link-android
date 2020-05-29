package com.mvp.view

import com.mvp.ParentView

interface GetBindDeviceTokenView : ParentView {

    fun onSuccess(token: String)

    fun onFail(msg: String)

}