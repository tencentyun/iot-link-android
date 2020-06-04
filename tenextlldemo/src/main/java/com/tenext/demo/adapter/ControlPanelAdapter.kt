package com.tenext.demo.adapter

import android.content.Context
import android.view.ViewGroup
import com.tenext.demo.R
import com.tenext.demo.holder.BaseHolder
import com.tenext.demo.holder.ControlPanelHolder

class ControlPanelAdapter : BaseAdapter {

    constructor(context: Context, list: List<Any>) : super(context, list)

    override fun createHolder(parent: ViewGroup, viewType: Int): BaseHolder<*> {
        return ControlPanelHolder(mContext, parent, R.layout.item_control_panel)
    }
}