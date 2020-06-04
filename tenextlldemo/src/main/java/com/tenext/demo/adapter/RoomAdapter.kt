package com.tenext.demo.adapter

import android.content.Context
import android.view.ViewGroup
import com.tenext.auth.entity.Room
import com.tenext.demo.R
import com.tenext.demo.holder.BaseHolder
import com.tenext.demo.holder.RoomHolder

class RoomAdapter(context: Context, list: List<Room>) : BaseAdapter(context, list) {

    override fun createHolder(parent: ViewGroup, viewType: Int): BaseHolder<*> {
        return RoomHolder(mContext, parent, R.layout.item_room)
    }
}