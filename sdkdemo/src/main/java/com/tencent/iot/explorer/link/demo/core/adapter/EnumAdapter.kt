package com.tencent.iot.explorer.link.demo.core.adapter

import android.content.Context
import android.view.ViewGroup
import com.tencent.iot.explorer.link.demo.R
import com.tencent.iot.explorer.link.demo.core.holder.BaseHolder
import com.tencent.iot.explorer.link.demo.core.holder.EnumHolder
import com.tencent.iot.explorer.link.demo.core.popup.EnumPopupWindow

class EnumAdapter : BaseAdapter {

    private var window: EnumPopupWindow

    constructor(context: Context, window: EnumPopupWindow, list: List<Any>) : super(context, list) {
        this.window = window
    }

    override fun createHolder(parent: ViewGroup, viewType: Int): BaseHolder<*> {
        return EnumHolder(mContext,window, parent, R.layout.item_enum)
    }
}