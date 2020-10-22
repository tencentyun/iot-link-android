package com.tencent.iot.explorer.link.core.auth.response

import com.tencent.iot.explorer.link.core.link.entity.MessageEntity

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