package com.tencent.iot.explorer.link.core.demo.adapter

import android.content.Context
import android.view.ViewGroup
import com.tencent.iot.explorer.link.core.demo.R
import com.tencent.iot.explorer.link.core.auth.entity.Family
import com.tencent.iot.explorer.link.core.demo.holder.BaseHolder
import com.tencent.iot.explorer.link.core.demo.holder.FamilyHolder

class FamilyAdapter(context: Context, list: List<Family>) : BaseAdapter(context, list) {

    override fun getItemViewType(position: Int): Int {
        return 0
    }

    override fun createHolder(parent: ViewGroup, viewType: Int): BaseHolder<*> {
        return FamilyHolder(mContext, parent, R.layout.item_family)
    }
}