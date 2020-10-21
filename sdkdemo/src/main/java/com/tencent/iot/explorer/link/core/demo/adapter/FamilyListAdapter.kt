package com.tencent.iot.explorer.link.core.demo.adapter

import android.content.Context
import android.view.ViewGroup
import com.tencent.iot.explorer.link.core.auth.entity.FamilyEntity
import com.tencent.iot.explorer.link.core.demo.R
import com.tencent.iot.explorer.link.core.demo.holder.BaseHolder
import com.tencent.iot.explorer.link.core.demo.holder.FamilyListHolder

class FamilyListAdapter(context: Context, list: List<FamilyEntity>) : BaseAdapter(context, list) {

    override fun createHolder(parent: ViewGroup, viewType: Int): BaseHolder<*> {
        return FamilyListHolder(mContext, parent, R.layout.item_family_list)
    }

}