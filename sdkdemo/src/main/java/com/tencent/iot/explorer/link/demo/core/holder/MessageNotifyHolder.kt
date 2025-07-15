package com.tencent.iot.explorer.link.demo.core.holder

import com.tencent.iot.explorer.link.demo.common.util.DateFormatUtil
import com.tencent.iot.explorer.link.core.link.entity.MessageEntity
import com.tencent.iot.explorer.link.demo.databinding.ItemMessageNotifyBinding

/**
 * 通知类型消息
 */
class MessageNotifyHolder(binding: ItemMessageNotifyBinding) : BaseHolder<MessageEntity, ItemMessageNotifyBinding>(binding) {
    override fun show(holder: BaseHolder<*, *>, position: Int) {
        with(binding) {
            data.let {
                tvMessageContent.text = it.MsgContent
                tvMessageTime.text =
                    DateFormatUtil.forString(it.MsgTimestamp, "yyyy-MM-dd  HH:mm")
                rlDeleteMessage.setOnClickListener {
                    clickItem(this@MessageNotifyHolder, rlDeleteMessage, position)
                }
            }
        }
    }
}