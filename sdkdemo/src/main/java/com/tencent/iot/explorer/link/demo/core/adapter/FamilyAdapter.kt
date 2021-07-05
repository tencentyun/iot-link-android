package com.tencent.iot.explorer.link.demo.core.adapter

import android.content.Context
import android.view.ViewGroup
import com.tencent.iot.explorer.link.demo.R
import com.tencent.iot.explorer.link.core.auth.entity.FamilyEntity
import com.tencent.iot.explorer.link.demo.core.holder.BaseHolder
import com.tencent.iot.explorer.link.demo.core.holder.FamilyHolder

class FamilyAdapter(context: Context, list: List<FamilyEntity>) : BaseAdapter(context, list) {

    override fun getItemViewType(position: Int): Int {
        return 0
    }

    override fun createHolder(parent: ViewGroup, viewType: Int): BaseHolder<*> {
        return FamilyHolder(mContext, parent, R.layout.item_family)
    }
}