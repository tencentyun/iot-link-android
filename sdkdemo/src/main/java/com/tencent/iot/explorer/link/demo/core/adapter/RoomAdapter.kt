package com.tencent.iot.explorer.link.demo.core.adapter

import android.content.Context
import android.view.ViewGroup
import com.tencent.iot.explorer.link.core.auth.entity.RoomEntity
import com.tencent.iot.explorer.link.core.demo.R
import com.tencent.iot.explorer.link.demo.core.holder.BaseHolder
import com.tencent.iot.explorer.link.demo.core.holder.RoomHolder

class RoomAdapter(context: Context, list: List<RoomEntity>) : BaseAdapter(context, list) {

    override fun createHolder(parent: ViewGroup, viewType: Int): BaseHolder<*> {
        return RoomHolder(mContext, parent, R.layout.item_room)
    }
}