package com.tencent.iot.explorer.link.demo.core.holder

import android.content.Context
import android.view.ViewGroup
import kotlinx.android.synthetic.main.foot_add_timing.view.*

class AddTimingFooterHolder : BaseHolder<String> {

    constructor(context: Context, root: ViewGroup, resLayout: Int) : super(context, root, resLayout)

    override fun show(holder: BaseHolder<*>, position: Int) {
        itemView.tv_save_timing_project.setOnClickListener {
            clickItem(this, it, 0)
        }
    }
}