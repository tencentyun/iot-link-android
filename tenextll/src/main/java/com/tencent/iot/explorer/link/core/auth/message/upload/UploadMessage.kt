package com.tencent.iot.explorer.link.core.auth.message.upload

import com.tencent.iot.explorer.link.core.auth.IoTAuth

open class UploadMessage : IotMsg() {

    init {
        commonParams["AccessToken"] = IoTAuth.user.Token
    }

}