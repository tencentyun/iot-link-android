package com.tencent.iot.explorer.link.kitlink.holder

import android.content.Context
import android.view.View
import android.view.ViewGroup
import com.tencent.iot.explorer.link.kitlink.entity.RoomEntity
import com.tencent.iot.explorer.link.customview.recyclerview.CRecyclerView
import kotlinx.android.synthetic.main.item_room_list.view.*

/**
 *  房间列表显示itemView
 */
class RoomListViewHolder : CRecyclerView.CViewHolder<RoomEntity> {

    constructor(context: Context, parent: ViewGroup, resId: Int) : super(context, parent, resId)

    override fun show(position: Int) {
        entity?.run {
            itemView.tv_room_name.text = RoomName
            itemView.tv_room_device_count.text = "${DeviceNum}个设备"
            itemView.room_list_top_space.visibility = if (position == 0) View.VISIBLE else View.GONE
        }

        itemView.setOnClickListener { recyclerItemView?.doAction(this, itemView, position) }
    }
}