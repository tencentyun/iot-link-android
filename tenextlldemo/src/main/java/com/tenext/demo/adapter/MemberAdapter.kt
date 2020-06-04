package com.tenext.demo.adapter

import android.content.Context
import android.view.ViewGroup
import com.tenext.demo.R
import com.tenext.demo.entity.Member
import com.tenext.demo.holder.BaseHolder
import com.tenext.demo.holder.MemberHolder

class MemberAdapter : BaseAdapter {

    constructor(context: Context, list: List<Any>) : super(context, list)

    override fun getItemViewType(position: Int): Int {
        return if (data(position) is Member) {
            1
        } else {
            0
        }
    }

    override fun createHolder(parent: ViewGroup, viewType: Int): BaseHolder<*> {

        return if (viewType == 1)
            MemberHolder(mContext, parent, R.layout.item_member)
        else
            FamilyInfoHolder(mContext, parent, R.layout.item_family_info)
    }

}