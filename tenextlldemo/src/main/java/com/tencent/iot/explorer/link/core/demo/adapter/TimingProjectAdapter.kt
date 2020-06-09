package com.tenext.demo.adapter

import android.content.Context
import android.view.ViewGroup
import com.tenext.demo.R
import com.tenext.demo.holder.BaseHolder
import com.tenext.demo.holder.TimingProjectHolder

class TimingProjectAdapter : BaseAdapter {

    constructor(context: Context, list: List<Any>) : super(context, list)

    override fun createHolder(parent: ViewGroup, viewType: Int): BaseHolder<*> {
        return TimingProjectHolder(mContext, parent, R.layout.item_timing_project)
    }
}