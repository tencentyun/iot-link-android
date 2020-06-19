package com.tencent.iot.explorer.link.kitlink.holder

import android.content.Context
import android.view.ViewGroup
import com.tencent.iot.explorer.link.customview.recyclerview.CRecyclerView
import kotlinx.android.synthetic.main.footer_add_timing_project.view.*

/**
 * 云端定时列表footer
 */
class FootAddTimingProjectHolder : CRecyclerView.FootViewHolder<Any> {

    constructor(context: Context, parent: ViewGroup, resId: Int) : super(context, parent, resId)

    override fun show() {
        itemView.tv_foot_add_timing_project.setOnClickListener {
            footListener?.doAction(this, it, 0)
        }
    }
}