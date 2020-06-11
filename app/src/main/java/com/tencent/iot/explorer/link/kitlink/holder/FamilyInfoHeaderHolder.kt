package com.tencent.iot.explorer.link.kitlink.holder

import android.content.Context
import android.view.View
import android.view.ViewGroup
import com.tencent.iot.explorer.link.kitlink.entity.FamilyEntity
import com.view.recyclerview.CRecyclerView
import kotlinx.android.synthetic.main.head_family.view.*

/**
 * 家庭详情头部
 */
class FamilyInfoHeaderHolder : CRecyclerView.HeadViewHolder<FamilyEntity> {

    constructor(context: Context, parent: ViewGroup, resId: Int) : super(context, parent, resId)

    override fun show() {
        data?.run {
            itemView.tv_head_family_name.text = FamilyName
            itemView.cl_head_family_invite.visibility = if (Role == 1) View.VISIBLE else View.GONE
        }
        itemView.tv_head_family_name_title.setOnClickListener {
            headListener?.doAction(this, it, 0)
        }
        itemView.tv_head_room_manage_title.setOnClickListener {
            headListener?.doAction(this, it, 1)
        }
        itemView.tv_head_family_invite.setOnClickListener {
            headListener?.doAction(this, it, 2)
        }
    }
}