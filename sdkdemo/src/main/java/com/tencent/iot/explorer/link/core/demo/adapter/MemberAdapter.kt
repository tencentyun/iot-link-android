package com.tencent.iot.explorer.link.core.demo.adapter

import android.content.Context
import android.view.ViewGroup
import com.tencent.iot.explorer.link.core.demo.R
import com.tencent.iot.explorer.link.core.demo.holder.BaseHolder
import com.tencent.iot.explorer.link.core.demo.holder.MemberHolder
import com.tencent.iot.explorer.link.core.link.entity.MemberEntity

class MemberAdapter : BaseAdapter {

    constructor(context: Context, list: List<Any>) : super(context, list)

    override fun getItemViewType(position: Int): Int {
        return if (data(position) is MemberEntity) {
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