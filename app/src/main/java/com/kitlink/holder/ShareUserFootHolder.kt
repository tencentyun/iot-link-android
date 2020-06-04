package com.kitlink.holder

import android.content.Context
import android.view.ViewGroup
import com.view.recyclerview.CRecyclerView
import kotlinx.android.synthetic.main.foot_share_user_list.view.*

/**
 * 分享用户列表底部
 */
class ShareUserFootHolder : CRecyclerView.FootViewHolder<Any> {
    constructor(context: Context, parent: ViewGroup, resId: Int) : super(context, parent, resId)

    override fun show() {
        itemView.tv_item_share_add.setOnClickListener {
            footListener?.doAction(this, it, 0)
        }
    }
}