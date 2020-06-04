package com.kitlink.holder

import android.content.Context
import android.view.ViewGroup
import com.view.recyclerview.CRecyclerView
import kotlinx.android.synthetic.main.foot_family.view.*

/**
 * 家庭详情底部
 */
class FamilyFootHolder : CRecyclerView.FootViewHolder<Any> {

    constructor(context: Context, parent: ViewGroup, resId: Int) : super(context, parent, resId)

    override fun show() {
        itemView.tv_delete_family.setOnClickListener {
            footListener?.doAction(this, it, 0)
        }
    }
}