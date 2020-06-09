package com.mvp.view

import com.mvp.ParentView

interface ConnectView : ParentView {

    fun connectSuccess()

    fun connectStep(step: Int)

    fun deviceConnectToWifiFail()

    fun softApConnectToWifiFail(ssid: String)

    fun connectFail(code: String, message: String)

}