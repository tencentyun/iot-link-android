package com.tenext.demo.adapter

import android.content.Context
import android.view.ViewGroup
import com.tenext.auth.entity.Family
import com.tenext.demo.R
import com.tenext.demo.holder.BaseHolder
import com.tenext.demo.holder.FamilyListHolder

class FamilyListAdapter(context: Context, list: List<Family>) : BaseAdapter(context, list) {

    override fun createHolder(parent: ViewGroup, viewType: Int): BaseHolder<*> {
        return FamilyListHolder(mContext, parent, R.layout.item_family_list)
    }

}