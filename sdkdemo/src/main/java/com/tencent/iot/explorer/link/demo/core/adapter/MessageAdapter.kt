package com.tencent.iot.explorer.link.demo.core.adapter

import android.content.Context
import android.view.ViewGroup
import com.tencent.iot.explorer.link.demo.R
import com.tencent.iot.explorer.link.demo.core.holder.BaseHolder
import com.tencent.iot.explorer.link.demo.core.holder.MessageDeviceHolder
import com.tencent.iot.explorer.link.demo.core.holder.MessageFamilyHolder
import com.tencent.iot.explorer.link.demo.core.holder.MessageNotifyHolder
import com.tencent.iot.explorer.link.core.link.entity.MessageEntity
import com.tencent.iot.explorer.link.demo.databinding.ItemMessageDeviceBinding
import com.tencent.iot.explorer.link.demo.databinding.ItemMessageFamilyBinding
import com.tencent.iot.explorer.link.demo.databinding.ItemMessageNotifyBinding

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

    override fun createHolder(parent: ViewGroup, viewType: Int): BaseHolder<*, *> {
        return when (viewType) {
            0 -> {
                val binding = ItemMessageDeviceBinding.inflate(mInflater, parent, false)
                MessageDeviceHolder(binding)
            }

            1 -> {
                val binding = ItemMessageFamilyBinding.inflate(mInflater, parent, false)
                MessageFamilyHolder(binding)
            }

            else -> {
                val binding = ItemMessageNotifyBinding.inflate(mInflater, parent, false)
                MessageNotifyHolder(binding)
            }
        }
    }
}