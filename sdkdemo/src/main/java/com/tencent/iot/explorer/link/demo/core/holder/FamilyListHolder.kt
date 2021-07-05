package com.tencent.iot.explorer.link.demo.core.holder

import android.content.Context
import android.view.View
import android.view.ViewGroup
import com.tencent.iot.explorer.link.core.auth.entity.FamilyEntity
import kotlinx.android.synthetic.main.item_family_list.view.*

/**
 * 家庭管理列表
 */
class FamilyListHolder : BaseHolder<FamilyEntity> {

    constructor(context: Context, parent: ViewGroup, resId: Int) : super(context, parent, resId)

    override fun show(holder: BaseHolder<*>, position: Int) {
        data.run {
            itemView.tv_family_name.text = FamilyName
            itemView.family_list_top_space.visibility =
                if (position == 0) View.VISIBLE else View.GONE
        }
        itemView.setOnClickListener { clickItem(this, itemView, position) }
    }

}