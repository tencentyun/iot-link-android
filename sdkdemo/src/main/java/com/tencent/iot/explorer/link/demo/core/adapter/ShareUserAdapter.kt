package com.tencent.iot.explorer.link.demo.core.adapter

import android.content.Context
import android.view.ViewGroup
import com.tencent.iot.explorer.link.demo.core.holder.BaseHolder
import com.tencent.iot.explorer.link.demo.core.holder.ShareUserHolder
import com.tencent.iot.explorer.link.demo.databinding.ItemShareUserBinding

class ShareUserAdapter : BaseAdapter {
    constructor(context: Context, list: List<Any>) : super(context, list)

    override fun createHolder(parent: ViewGroup, viewType: Int): BaseHolder<*, *> {
        val binding = ItemShareUserBinding.inflate(mInflater, parent, false)
        return ShareUserHolder(binding)
    }
}