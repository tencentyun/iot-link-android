package com.tencent.iot.explorer.link.mvp.view

import com.tencent.iot.explorer.link.mvp.ParentView

interface ConnectView : ParentView {

    fun connectSuccess()

    fun connectStep(step: Int)

    fun deviceConnectToWifiFail()

    fun softApConnectToWifiFail(ssid: String)

    fun connectFail(code: String, message: String)

}