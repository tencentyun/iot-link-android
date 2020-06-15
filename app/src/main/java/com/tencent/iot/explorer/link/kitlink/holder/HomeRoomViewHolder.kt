package com.tencent.iot.explorer.link.kitlink.holder

import android.content.Context
import android.graphics.Typeface
import android.text.TextUtils
import android.view.ViewGroup
import com.tencent.iot.explorer.link.R
import com.tencent.iot.explorer.link.kitlink.entity.RoomEntity
import com.view.recyclerview.CRecyclerView
import kotlinx.android.synthetic.main.item_home_room.view.*

class HomeRoomViewHolder : CRecyclerView.CViewHolder<RoomEntity> {

    constructor(context: Context, parent: ViewGroup, resId: Int) : super(context, parent, resId)

    override fun show(position: Int) {
        itemView.tv_home_room_name.text =
            if (TextUtils.isEmpty(entity?.RoomName)) "全部设备" else entity!!.RoomName
        itemView.tv_home_room_name.setTextColor(
            if (isSelected(position)) {
                itemView.tv_home_room_name.typeface = Typeface.DEFAULT_BOLD
                itemView.resources.getColor(R.color.dark_0052d9)
            } else {
                itemView.tv_home_room_name.typeface = Typeface.DEFAULT
                itemView.resources.getColor(R.color.black_333333)
            }
        )
        itemView.setOnClickListener { recyclerItemView?.doAction(this, itemView, position) }
    }

}