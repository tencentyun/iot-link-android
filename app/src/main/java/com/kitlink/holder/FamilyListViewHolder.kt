package com.kitlink.holder

import android.content.Context
import android.view.View
import android.view.ViewGroup
import com.kitlink.entity.FamilyEntity
import com.view.recyclerview.CRecyclerView
import kotlinx.android.synthetic.main.item_family_list.view.*

/**
 * 家庭管理列表
 */
class FamilyListViewHolder : CRecyclerView.CViewHolder<FamilyEntity> {

    constructor(context: Context, parent: ViewGroup, resId: Int) : super(context, parent, resId)

    override fun show(position: Int) {
        entity?.run {
            itemView.tv_family_name.text = FamilyName
            itemView.family_list_top_space.visibility =
                if (position == 0) View.VISIBLE else View.GONE
        }
        itemView.setOnClickListener { recyclerItemView?.doAction(this, itemView, position) }
    }
}