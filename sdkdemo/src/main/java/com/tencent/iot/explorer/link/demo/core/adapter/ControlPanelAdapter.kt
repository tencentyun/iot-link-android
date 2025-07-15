package com.tencent.iot.explorer.link.demo.core.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import com.tencent.iot.explorer.link.demo.core.holder.BaseHolder
import com.tencent.iot.explorer.link.demo.core.holder.ControlPanelHolder
import com.tencent.iot.explorer.link.demo.databinding.ItemControlPanelBinding

class ControlPanelAdapter : BaseAdapter {

    constructor(context: Context, list: List<Any>) : super(context, list)

    override fun createHolder(parent: ViewGroup, viewType: Int): BaseHolder<*, *> {
        val binding = ItemControlPanelBinding.inflate(mInflater, parent, false)
        return ControlPanelHolder(binding)
    }
}