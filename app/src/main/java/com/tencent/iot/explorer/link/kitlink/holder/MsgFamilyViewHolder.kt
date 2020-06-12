package com.tencent.iot.explorer.link.kitlink.holder

import android.view.View
import com.tencent.iot.explorer.link.kitlink.R
import com.tencent.iot.explorer.link.kitlink.entity.MessageEntity
import com.tencent.iot.explorer.link.util.date.DateFormatUtil
import com.view.recyclerview.CRecyclerView
import kotlinx.android.synthetic.main.item_message_family.view.*

class MsgFamilyViewHolder : CRecyclerView.CViewHolder<MessageEntity> {

    constructor(itemView: View) : super(itemView)

    override fun show(position: Int) {
        entity?.let {
            itemView.run {
                tv_message_title.text = it.MsgTitle
                tv_message_content.text = it.MsgContent
                iv_icon_message.setImageResource(R.mipmap.icon_light)
                tv_message_time.text =
                    DateFormatUtil.forString(it.MsgTimestamp, "yyyy-MM-dd  HH:mm")
            }
        }
        itemView.rl_delete_message.setOnClickListener {
            it.tag = 2
            recyclerItemView?.doAction(this, it, position)
        }
        itemView.tv_refuse_family.setOnClickListener {
            it.tag = 0
            recyclerItemView?.doAction(this, it, position)
        }
        itemView.tv_invite_family.setOnClickListener {
            it.tag = 1
            recyclerItemView?.doAction(this, it, position)
        }

    }
}