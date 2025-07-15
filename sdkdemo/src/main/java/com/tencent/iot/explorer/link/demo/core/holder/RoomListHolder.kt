package com.tencent.iot.explorer.link.demo.core.holder

import com.tencent.iot.explorer.link.core.auth.entity.RoomEntity
import com.tencent.iot.explorer.link.demo.App
import com.tencent.iot.explorer.link.demo.R
import com.tencent.iot.explorer.link.demo.databinding.ItemRoomListBinding

class RoomListHolder(binding: ItemRoomListBinding) : BaseHolder<RoomEntity, ItemRoomListBinding>(binding) {
    override fun show(holder: BaseHolder<*, *>, position: Int) {
        with(binding) {
            tvRoomName.text = data.RoomName
            tvRoomName.setTextColor(
                if (App.data.getCurrentRoom().RoomId == data.RoomId) {
                    getColor(R.color.blue_006EFF)
                } else {
                    getColor(R.color.black_333333)
                }
            )
            tvRoomName.setOnClickListener {
                clickItem(this@RoomListHolder, it, position)
            }
        }
    }
}