package com.tencent.iot.explorer.link.demo.core.holder

import com.tencent.iot.explorer.link.demo.R
import com.tencent.iot.explorer.link.demo.common.util.DateFormatUtil
import com.tencent.iot.explorer.link.core.link.entity.MessageEntity
import com.tencent.iot.explorer.link.demo.databinding.ItemMessageDeviceBinding

/**
 * 设备类型消息
 */
class MessageDeviceHolder(binding: ItemMessageDeviceBinding) : BaseHolder<MessageEntity, ItemMessageDeviceBinding>(binding) {
    override fun show(holder: BaseHolder<*, *>, position: Int) {
        data.let {
            with(binding) {
                tvMessageTitle.text = it.MsgTitle
                tvMessageContent.text = it.MsgContent
                if (it.Category == 2)
                    ivIconMessage.setImageResource(R.drawable.icon_portrait)
                else
                    ivIconMessage.setImageResource(R.mipmap.icon_light)
                tvMessageTime.text =
                    DateFormatUtil.forString(it.MsgTimestamp, "yyyy-MM-dd  HH:mm")
                rlDeleteMessage.setOnClickListener {
                    it.tag = 2
                    clickItem(this@MessageDeviceHolder, rlDeleteMessage, position)
                }
            }
        }
    }
}