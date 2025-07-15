package com.tencent.iot.explorer.link.demo.core.adapter

import android.content.Context
import android.view.ViewGroup
import com.tencent.iot.explorer.link.demo.core.holder.BaseHolder
import com.tencent.iot.explorer.link.demo.core.holder.EnumHolder
import com.tencent.iot.explorer.link.demo.core.popup.EnumPopupWindow
import com.tencent.iot.explorer.link.demo.databinding.ItemEnumBinding

class EnumAdapter : BaseAdapter {

    private var window: EnumPopupWindow

    constructor(context: Context, window: EnumPopupWindow, list: List<Any>) : super(context, list) {
        this.window = window
    }

    override fun createHolder(parent: ViewGroup, viewType: Int): BaseHolder<*, *> {
        val binding = ItemEnumBinding.inflate(mInflater, parent, false)
        return EnumHolder(binding, window)
    }
}