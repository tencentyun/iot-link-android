package com.tencent.iot.explorer.link.kitlink.response

import com.tencent.iot.explorer.link.kitlink.entity.MessageEntity

/**
 * 消息列表响应实体
 */
class MessageListResponse {

    var Data = MessageData()
    var RequestId = ""

    inner class MessageData {
        var Listover = true
        var Msgs = arrayListOf<MessageEntity>()
    }
}