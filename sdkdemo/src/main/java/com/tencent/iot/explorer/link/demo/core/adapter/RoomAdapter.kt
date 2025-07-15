package com.tencent.iot.explorer.link.demo.core.adapter

import android.content.Context
import android.view.ViewGroup
import com.tencent.iot.explorer.link.core.auth.entity.RoomEntity
import com.tencent.iot.explorer.link.demo.core.holder.BaseHolder
import com.tencent.iot.explorer.link.demo.core.holder.RoomHolder
import com.tencent.iot.explorer.link.demo.databinding.ItemRoomBinding

class RoomAdapter(context: Context, list: List<RoomEntity>) : BaseAdapter(context, list) {

    override fun createHolder(parent: ViewGroup, viewType: Int): BaseHolder<*, *> {
        val binding = ItemRoomBinding.inflate(mInflater, parent, false)
        return RoomHolder(binding)
    }
}