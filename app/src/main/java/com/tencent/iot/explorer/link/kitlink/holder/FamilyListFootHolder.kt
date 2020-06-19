package com.tencent.iot.explorer.link.kitlink.holder

import android.content.Context
import android.view.ViewGroup
import com.tencent.iot.explorer.link.customview.recyclerview.CRecyclerView
import kotlinx.android.synthetic.main.foot_family_list.view.*

/**
 * 家庭管理 底部
 */
class FamilyListFootHolder : CRecyclerView.FootViewHolder<Any> {

    constructor(context: Context, parent: ViewGroup, resId: Int) : super(context, parent, resId)

    override fun show() {
        itemView.tv_add_family.setOnClickListener {
            footListener?.doAction(this, it, 0)
        }
    }
}