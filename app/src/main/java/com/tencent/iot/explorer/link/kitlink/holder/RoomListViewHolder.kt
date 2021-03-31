package com.tencent.iot.explorer.link.kitlink.holder

import android.content.Context
import android.view.View
import android.view.ViewGroup
import com.tencent.iot.explorer.link.R
import com.tencent.iot.explorer.link.customview.recyclerview.CRecyclerView
import com.tencent.iot.explorer.link.T
import com.tencent.iot.explorer.link.core.auth.entity.RoomEntity
import kotlinx.android.synthetic.main.item_room_list.view.*

/**
 *  房间列表显示itemView
 */
class RoomListViewHolder : CRecyclerView.CViewHolder<RoomEntity> {
    var showFlag = true

    constructor(context: Context, parent: ViewGroup, resId: Int, showFlag: Boolean) : super(context, parent, resId) {
        this.showFlag = showFlag
    }

    override fun show(position: Int) {
        entity?.run {
            itemView.tv_room_name.text = RoomName
            itemView.tv_room_device_count.text = T.getContext().getString(R.string.num_devices, "" + DeviceNum)//"${DeviceNum}个设备"
            itemView.room_list_top_space.visibility = if (position == 0) View.VISIBLE else View.GONE
            itemView.v_item_line.visibility = if (position == 0) View.GONE else View.VISIBLE
        }
        itemView.iv_go.visibility = if (showFlag) View.VISIBLE else View.GONE
        itemView.setOnClickListener { recyclerItemView?.doAction(this, itemView, position) }
    }
}