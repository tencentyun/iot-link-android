package com.tencent.iot.explorer.link.kitlink.holder

import android.view.View
import com.tencent.iot.explorer.link.core.link.entity.MessageEntity
import com.tencent.iot.explorer.link.customview.recyclerview.CRecyclerView
import com.tencent.iot.explorer.link.kitlink.util.DateUtils
import kotlinx.android.synthetic.main.item_message_notify.view.*

class MsgNotifyViewHolder : CRecyclerView.CViewHolder<MessageEntity> {

    constructor(itemView: View) : super(itemView)

    override fun show(position: Int) {
        itemView.run {
            entity?.let {
                tv_message_content.text = it.MsgContent
                tv_message_time.text =
                    DateUtils.forString(it.MsgTimestamp, "yyyy-MM-dd  HH:mm")
            }
            rl_delete_message.setOnClickListener {
                it.tag = 2
                recyclerItemView?.doAction(this@MsgNotifyViewHolder, it, position)
            }
        }
    }
}