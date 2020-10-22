package com.tencent.iot.explorer.link.kitlink.holder

import android.view.View
import com.tencent.iot.explorer.link.R
import com.tencent.iot.explorer.link.core.link.entity.MessageEntity
import com.tencent.iot.explorer.link.customview.recyclerview.CRecyclerView
import com.tencent.iot.explorer.link.kitlink.util.DateUtils
import kotlinx.android.synthetic.main.item_message_device.view.*

class MsgDeviceViewHolder : CRecyclerView.CViewHolder<MessageEntity> {

    constructor(itemView: View) : super(itemView)

    override fun show(position: Int) {
        entity?.let {
            itemView.run {
                tv_message_title.text = it.MsgTitle
                tv_message_content.text = it.MsgContent
                iv_icon_message.setImageResource(R.mipmap.icon_light)
                tv_message_time.text =
                    DateUtils.forString(it.MsgTimestamp, "yyyy-MM-dd  HH:mm")
            }
        }
        itemView.rl_delete_message.setOnClickListener {
            it.tag = 2
            recyclerItemView?.doAction(this, it, position)
        }
    }
}