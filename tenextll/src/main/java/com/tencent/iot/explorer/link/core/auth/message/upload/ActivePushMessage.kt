package com.tenext.auth.message.upload

class ActivePushMessage(deviceIds: ArrayString) : UploadMessage() {

    init {
        action = "ActivePush"
        commonParams["DeviceIds"] = deviceIds
    }

}