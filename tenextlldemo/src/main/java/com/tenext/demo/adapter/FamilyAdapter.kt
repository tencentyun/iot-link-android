package com.tenext.demo.adapter

import android.content.Context
import android.view.ViewGroup
import com.tenext.demo.R
import com.tenext.auth.entity.Family
import com.tenext.demo.holder.BaseHolder
import com.tenext.demo.holder.FamilyHolder

class FamilyAdapter(context: Context, list: List<Family>) : BaseAdapter(context, list) {

    override fun getItemViewType(position: Int): Int {
        return 0
    }

    override fun createHolder(parent: ViewGroup, viewType: Int): BaseHolder<*> {
        return FamilyHolder(mContext, parent, R.layout.item_family)
    }
}