package com.tenext.demo.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import com.tenext.demo.R
import com.tenext.demo.entity.IotMessage
import com.tenext.demo.holder.BaseHolder
import com.tenext.demo.holder.MessageDeviceHolder
import com.tenext.demo.holder.MessageFamilyHolder
import com.tenext.demo.holder.MessageNotifyHolder

class MessageAdapter : BaseAdapter {

    constructor(context: Context, list: List<IotMessage>) : super(context, list)

    override fun getItemViewType(position: Int): Int {
        return when ((data(position) as IotMessage).Attachments == null) {
            true -> when ((data(position) as IotMessage).Category) {
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