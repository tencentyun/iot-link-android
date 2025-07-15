package com.tencent.iot.explorer.link.demo.core.adapter

import android.content.Context
import android.view.ViewGroup
import com.tencent.iot.explorer.link.demo.core.holder.BaseHolder
import com.tencent.iot.explorer.link.demo.core.holder.RoomListHolder
import com.tencent.iot.explorer.link.demo.databinding.ItemRoomListBinding

class RoomListAdapter : BaseAdapter {

    constructor(context: Context, list: List<Any>) : super(context, list)

    override fun createHolder(parent: ViewGroup, viewType: Int): BaseHolder<*, *> {
        val binding = ItemRoomListBinding.inflate(mInflater, parent, false)
        return RoomListHolder(binding)
    }

}