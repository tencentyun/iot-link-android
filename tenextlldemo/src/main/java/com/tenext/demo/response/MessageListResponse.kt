package com.tenext.demo.response

import com.tenext.demo.entity.IotMessage

/**
 * 消息列表响应实体
 */
class MessageListResponse {

    var Data = MessageData()
    var RequestId = ""

    inner class MessageData {
        var Listover = true
        var Msgs = arrayListOf<IotMessage>()
    }
}