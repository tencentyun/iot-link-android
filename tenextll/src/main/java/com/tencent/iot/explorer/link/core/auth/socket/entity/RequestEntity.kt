package com.tenext.auth.socket.entity

import com.tenext.auth.message.upload.IotMsg
import com.tenext.auth.socket.callback.MessageCallback

class RequestEntity(requestId: Int, msg: IotMsg) {

    var reqId = requestId
    var iotMsg: IotMsg = msg
    var messageCallback: MessageCallback? = null

}