package com.tencent.iot.explorer.link.demo.core.adapter

import android.content.Context
import android.view.ViewGroup
import com.tencent.iot.explorer.link.demo.R
import com.tencent.iot.explorer.link.demo.core.holder.BaseHolder
import com.tencent.iot.explorer.link.demo.core.holder.MemberHolder
import com.tencent.iot.explorer.link.core.link.entity.MemberEntity
import com.tencent.iot.explorer.link.demo.databinding.ItemFamilyInfoBinding
import com.tencent.iot.explorer.link.demo.databinding.ItemMemberBinding

class MemberAdapter : BaseAdapter {

    constructor(context: Context, list: List<Any>) : super(context, list)

    override fun getItemViewType(position: Int): Int {
        return if (data(position) is MemberEntity) {
            1
        } else {
            0
        }
    }

    override fun createHolder(parent: ViewGroup, viewType: Int): BaseHolder<*, *> {
        return when(viewType) {
            1 -> {
                val binding = ItemMemberBinding.inflate(mInflater, parent, false)
                MemberHolder(binding)
            }

            else -> {
                val binding = ItemFamilyInfoBinding.inflate(mInflater, parent, false)
                FamilyInfoHolder(binding)
            }
        }
    }

}