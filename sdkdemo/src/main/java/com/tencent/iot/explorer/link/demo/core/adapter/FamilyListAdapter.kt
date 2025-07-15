package com.tencent.iot.explorer.link.demo.core.adapter

import android.content.Context
import android.view.ViewGroup
import com.tencent.iot.explorer.link.core.auth.entity.FamilyEntity
import com.tencent.iot.explorer.link.demo.core.holder.BaseHolder
import com.tencent.iot.explorer.link.demo.core.holder.FamilyListHolder
import com.tencent.iot.explorer.link.demo.databinding.ItemFamilyListBinding

class FamilyListAdapter(context: Context, list: List<FamilyEntity>) : BaseAdapter(context, list) {

    override fun createHolder(parent: ViewGroup, viewType: Int): BaseHolder<*, *> {
        val binding = ItemFamilyListBinding.inflate(mInflater, parent, false)
        return FamilyListHolder(binding)
    }

}