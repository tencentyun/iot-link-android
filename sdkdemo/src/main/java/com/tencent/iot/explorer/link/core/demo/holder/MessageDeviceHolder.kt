package com.tencent.iot.explorer.link.core.demo.holder

import android.content.Context
import android.view.ViewGroup
import com.tencent.iot.explorer.link.core.demo.R
import com.tencent.iot.explorer.link.core.demo.util.DateFormatUtil
import com.tencent.iot.explorer.link.core.link.entity.MessageEntity
import kotlinx.android.synthetic.main.item_message_device.view.*

/**
 * 设备类型消息
 */
class MessageDeviceHolder : BaseHolder<MessageEntity> {

    constructor(context: Context, root: ViewGroup, resLayout: Int) : super(context, root, resLayout)

    override fun show(holder: BaseHolder<*>, position: Int) {
        data.let {
            itemView.run {
                tv_message_title.text = it.MsgTitle
                tv_message_content.text = it.MsgContent
                if (it.Category == 2)
                    iv_icon_message.setImageResource(R.drawable.icon_portrait)
                else
                    iv_icon_message.setImageResource(R.mipmap.icon_light)
                tv_message_time.text =
                    DateFormatUtil.forString(it.MsgTimestamp, "yyyy-MM-dd  HH:mm")
                rl_delete_message.setOnClickListener {
                    it.tag = 2
                    clickItem(this@MessageDeviceHolder, rl_delete_message, position)
                }
            }
        }
    }
}