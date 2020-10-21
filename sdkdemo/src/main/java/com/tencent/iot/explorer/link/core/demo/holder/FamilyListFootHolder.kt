package com.tencent.iot.explorer.link.core.demo.holder

import android.content.Context
import android.view.ViewGroup
import com.tencent.iot.explorer.link.core.auth.entity.FamilyEntity
import kotlinx.android.synthetic.main.foot_family_list.view.*

/**
 * 家庭 管理底部
 */
class FamilyListFootHolder : BaseHolder<FamilyEntity> {

    constructor(context: Context, parent: ViewGroup, resId: Int) : super(context, parent, resId)

    override fun show(holder: BaseHolder<*>, position: Int) {
        itemView.tv_add_family.setOnClickListener {
            clickItem(holder, it, position)
        }
    }

}