package com.tencent.iot.explorer.link.kitlink.holder

import android.content.Context
import android.view.ViewGroup
import com.view.recyclerview.CRecyclerView
import kotlinx.android.synthetic.main.foot_room_list.view.*

/**
 * 房间管理底部
 */
class RoomListFootHolder : CRecyclerView.FootViewHolder<Any> {

    constructor(context: Context, parent: ViewGroup, resId: Int) : super(context, parent, resId)

    override fun show() {
        itemView.tv_add_room.setOnClickListener {
            footListener?.doAction(this, it, 0)
        }
    }
}