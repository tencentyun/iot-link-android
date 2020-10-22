package com.tencent.iot.explorer.link.core.demo.adapter

import android.content.Context
import android.view.ViewGroup
import com.tencent.iot.explorer.link.core.demo.R
import com.tencent.iot.explorer.link.core.demo.holder.BaseHolder
import com.tencent.iot.explorer.link.core.demo.holder.MessageDeviceHolder
import com.tencent.iot.explorer.link.core.demo.holder.MessageFamilyHolder
import com.tencent.iot.explorer.link.core.demo.holder.MessageNotifyHolder
import com.tencent.iot.explorer.link.core.link.entity.MessageEntity

class MessageAdapter : BaseAdapter {

    constructor(context: Context, list: List<MessageEntity>) : super(context, list)

    override fun getItemViewType(position: Int): Int {
        return when ((data(position) as MessageEntity).Attachments == null) {
            true -> when ((data(position) as MessageEntity).Category) {
                1, 2 -> 0
                else -> 2
            }
            else -> 1
        }
    }

    override fun createHolder(parent: ViewGroup, viewType: Int): BaseHolder<*> {
        return when (viewType) {
            0 -> MessageDeviceHolder(mContext, parent, R.layout.item_message_device)
            1 -> MessageFamilyHolder(mContext, parent, R.layout.item_message_family)
            else -> MessageNotifyHolder(mContext, parent, R.layout.item_message_notify)
        }
    }
}