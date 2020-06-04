package com.auth.socket.entity

import com.auth.message.upload.IotMsg
import com.auth.socket.callback.MessageCallback

class RequestEntity(requestId: Int, msg: IotMsg) {

    var reqId = requestId
    var iotMsg: IotMsg = msg
    var messageCallback: MessageCallback? = null

}