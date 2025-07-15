package com.tencent.iot.explorer.link.demo.core.adapter

import android.content.Context
import android.view.ViewGroup
import com.tencent.iot.explorer.link.demo.R
import com.tencent.iot.explorer.link.demo.core.holder.BaseHolder
import com.tencent.iot.explorer.link.demo.core.holder.TimingProjectHolder
import com.tencent.iot.explorer.link.demo.databinding.ItemTimingProjectBinding

class TimingProjectAdapter : BaseAdapter {

    constructor(context: Context, list: List<Any>) : super(context, list)

    override fun createHolder(parent: ViewGroup, viewType: Int): BaseHolder<*, *> {
        val binding = ItemTimingProjectBinding.inflate(mInflater, parent, false)
        return TimingProjectHolder(binding)
    }
}