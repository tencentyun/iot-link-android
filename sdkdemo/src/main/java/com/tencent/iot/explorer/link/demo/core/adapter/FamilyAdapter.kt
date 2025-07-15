package com.tencent.iot.explorer.link.demo.core.adapter

import android.content.Context
import android.view.ViewGroup
import com.tencent.iot.explorer.link.demo.R
import com.tencent.iot.explorer.link.core.auth.entity.FamilyEntity
import com.tencent.iot.explorer.link.demo.core.holder.BaseHolder
import com.tencent.iot.explorer.link.demo.core.holder.FamilyHolder
import com.tencent.iot.explorer.link.demo.databinding.ItemFamilyBinding

class FamilyAdapter(context: Context, list: List<FamilyEntity>) : BaseAdapter(context, list) {

    override fun getItemViewType(position: Int): Int {
        return 0
    }

    override fun createHolder(parent: ViewGroup, viewType: Int): BaseHolder<*, *> {
        val binding = ItemFamilyBinding.inflate(mInflater, parent, false)
        return FamilyHolder(binding)
    }
}