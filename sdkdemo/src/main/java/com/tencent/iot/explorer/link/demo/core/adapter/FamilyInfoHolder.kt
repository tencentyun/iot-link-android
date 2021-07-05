package com.tencent.iot.explorer.link.demo.core.adapter

import android.content.Context
import android.view.View
import android.view.ViewGroup
import com.tencent.iot.explorer.link.core.auth.entity.FamilyEntity
import com.tencent.iot.explorer.link.demo.core.holder.BaseHolder
import kotlinx.android.synthetic.main.item_family_info.view.*

class FamilyInfoHolder : BaseHolder<FamilyEntity> {

    constructor(context: Context, root: ViewGroup, resLayout: Int) : super(context, root, resLayout)

    override fun show(holder: BaseHolder<*>, position: Int) {
        data.run {
            itemView.tv_head_family_name.text = FamilyName
            itemView.cl_head_family_invite.visibility = if (Role == 1) View.VISIBLE else View.GONE
        }
        itemView.tv_head_family_name_title.setOnClickListener {
            clickItem(this, it, -1)
        }
        itemView.tv_head_room_manage_title.setOnClickListener {
            clickItem(this, it, -2)
        }
        itemView.tv_head_family_invite.setOnClickListener {
            clickItem(this, it, -3)
        }
    }
}