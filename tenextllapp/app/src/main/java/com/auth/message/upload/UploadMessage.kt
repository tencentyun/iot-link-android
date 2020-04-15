package com.auth.message.upload

import com.auth.consts.SocketField
import com.kitlink.App

open class UploadMessage : IotMsg() {

    init {
        commonParams[SocketField.ACCESS_TOKEN] = App.data.getToken()
    }

}