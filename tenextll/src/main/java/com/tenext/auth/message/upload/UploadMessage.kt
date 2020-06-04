package com.tenext.auth.message.upload

import com.tenext.auth.IoTAuth

open class UploadMessage : IotMsg() {

    init {
        commonParams["AccessToken"] = IoTAuth.user.Token
    }

}