package com.tencent.iot.explorer.link.auth.socket.entity

import com.tencent.iot.explorer.link.auth.message.upload.IotMsg
import com.tencent.iot.explorer.link.auth.socket.callback.MessageCallback

class RequestEntity(requestId: Int, msg: IotMsg) {

    var reqId = requestId
    var iotMsg: IotMsg = msg
    var messageCallback: MessageCallback? = null

}