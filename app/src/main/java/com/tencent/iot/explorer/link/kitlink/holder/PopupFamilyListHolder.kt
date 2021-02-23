package com.tencent.iot.explorer.link.kitlink.holder

import android.content.Context
import android.view.View
import android.view.ViewGroup
import com.tencent.iot.explorer.link.R
import com.tencent.iot.explorer.link.core.auth.entity.FamilyEntity
import com.tencent.iot.explorer.link.customview.recyclerview.CRecyclerView
import kotlinx.android.synthetic.main.item_popup_family_list.view.*

/**
 * 家庭列表选择
 */
class PopupFamilyListHolder : CRecyclerView.CViewHolder<FamilyEntity> {
    constructor(context: Context, parent: ViewGroup, resId: Int) : super(context, parent, resId)

    override fun show(position: Int) {
        entity?.run {
            itemView.tv_popup_family_name.text = FamilyName
            if (isSelected(position)) {
                itemView.iv_popup_family_list.setImageResource(R.mipmap.readed)
                itemView.iv_popup_family_list_status.visibility = View.VISIBLE
            } else {
                itemView.iv_popup_family_list.setImageResource(R.mipmap.dev_mode_unsel)
                itemView.iv_popup_family_list_status.visibility = View.GONE
            }
        }
        itemView.setOnClickListener {
            recyclerItemView?.doAction(this, it, position)
        }
    }
}