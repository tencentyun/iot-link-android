package com.tencent.iot.explorer.link.kitlink.holder

import android.content.Context
import android.view.View
import android.view.ViewGroup
import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.JSONException
import com.tencent.iot.explorer.link.core.auth.entity.FamilyEntity
import com.tencent.iot.explorer.link.customview.recyclerview.CRecyclerView
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
            try {
                var address = JSON.parseObject(Address, com.tencent.iot.explorer.link.kitlink.entity.Address::class.java)
                itemView.tv_head_family_address.text = address?.name
                itemView.tv_head_family_address.visibility = if(showAddress) View.VISIBLE else View.GONE
            } catch (e : JSONException) {
                e.printStackTrace()
            }
            itemView.tv_head_room_manage.text = RoomsNum
        }
        itemView.tv_head_family_name_title.setOnClickListener {
            headListener?.doAction(this, it, 0)
        }
        itemView.tv_head_room_manage_title.setOnClickListener {
            headListener?.doAction(this, it, 1)
        }
        itemView.tv_head_family_location.setOnClickListener {
            headListener?.doAction(this, it, 2)
        }
        itemView.tv_head_family_invite.setOnClickListener {
            headListener?.doAction(this, it, 3)
        }
    }
}