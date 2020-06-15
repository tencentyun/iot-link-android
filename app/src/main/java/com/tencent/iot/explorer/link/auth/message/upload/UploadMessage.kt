package com.tencent.iot.explorer.link.auth.message.upload

import com.tencent.iot.explorer.link.auth.consts.SocketField
import com.tencent.iot.explorer.link.App

open class UploadMessage : IotMsg() {

    init {
        commonParams[SocketField.ACCESS_TOKEN] = App.data.getToken()
    }

}