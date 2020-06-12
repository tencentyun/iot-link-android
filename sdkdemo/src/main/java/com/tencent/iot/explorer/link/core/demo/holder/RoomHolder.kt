package com.tencent.iot.explorer.link.core.demo.holder

import android.content.Context
import android.view.ViewGroup
import com.tencent.iot.explorer.link.core.auth.entity.Room
import com.tencent.iot.explorer.link.core.demo.App
import com.tencent.iot.explorer.link.core.demo.R
import kotlinx.android.synthetic.main.item_room.view.*

class RoomHolder : BaseHolder<Room> {

    constructor(context: Context, root: ViewGroup, resLayout: Int) : super(context, root, resLayout)

    override fun show(holder: BaseHolder<*>, position: Int) {
        itemView.tv_room_name.text = data.RoomName
        itemView.tv_room_name.setTextColor(
            if (App.data.getCurrentRoom().RoomId == data.RoomId) {
                getColor(R.color.blue_006EFF)
            } else {
                getColor(R.color.black_333333)
            }
        )
        itemView.tv_room_name.setOnClickListener {
            clickItem(this, it, position)
        }
    }
}