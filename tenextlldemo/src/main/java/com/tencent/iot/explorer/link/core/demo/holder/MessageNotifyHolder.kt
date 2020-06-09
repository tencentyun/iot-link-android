package com.tenext.demo.holder

import android.content.Context
import android.view.ViewGroup
import com.tenext.demo.entity.IotMessage
import com.tenext.demo.util.DateFormatUtil
import kotlinx.android.synthetic.main.item_message_notify.view.*

/**
 * 通知类型消息
 */
class MessageNotifyHolder : BaseHolder<IotMessage> {
    constructor(context: Context, root: ViewGroup, resLayout: Int) : super(context, root, resLayout)

    override fun show(holder: BaseHolder<*>, position: Int) {
        itemView.run {
            data.let {
                tv_message_content.text = it.MsgContent
                tv_message_time.text =
                    DateFormatUtil.forString(it.MsgTimestamp, "yyyy-MM-dd  HH:mm")
                rl_delete_message.setOnClickListener {
                    clickItem(this@MessageNotifyHolder, rl_delete_message, position)
                }
            }
        }
    }
}