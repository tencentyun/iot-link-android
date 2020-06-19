package com.tencent.iot.explorer.link.kitlink.holder

import android.content.Context
import android.view.ViewGroup
import com.tencent.iot.explorer.link.customview.recyclerview.CRecyclerView
import kotlinx.android.synthetic.main.foot_add_timing.view.*

/**
 * 添加定时底部
 */
class AddTimingFootHolder : CRecyclerView.FootViewHolder<Any> {

    constructor(context: Context, parent: ViewGroup, resId: Int) : super(context, parent, resId)

    override fun show() {
        itemView.tv_save_timing_project.setOnClickListener {
            footListener?.doAction(this, it, 0)
        }
    }
}