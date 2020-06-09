package com.tencent.iot.explorer.link.core.demo.holder

import android.content.Context
import android.view.View
import android.view.ViewGroup
import com.tencent.iot.explorer.link.core.auth.entity.Room
import kotlinx.android.synthetic.main.item_room_list.view.*

class RoomListHolder : BaseHolder<Room> {

    constructor(context: Context, root: ViewGroup, resLayout: Int) : super(context, root, resLayout)

    override fun show(holder: BaseHolder<*>, position: Int) {
        data.run {
            itemView.tv_room_name.text = RoomName
            itemView.tv_room_device_count.text = "${DeviceNum}个设备"
            itemView.room_list_top_space.visibility = if (position == 0) View.VISIBLE else View.GONE
        }

        itemView.setOnClickListener { clickItem(this, itemView, position) }
    }
}