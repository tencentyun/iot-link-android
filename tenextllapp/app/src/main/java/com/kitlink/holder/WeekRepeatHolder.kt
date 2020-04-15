package com.kitlink.holder

import android.content.Context
import android.view.View
import android.view.ViewGroup
import com.kitlink.R
import com.kitlink.entity.WeekRepeatEntity
import com.view.recyclerview.CRecyclerView
import kotlinx.android.synthetic.main.item_week_repeat.view.*

/**
 *  重复设置
 */
class WeekRepeatHolder : CRecyclerView.CViewHolder<WeekRepeatEntity> {

    constructor(context: Context, parent: ViewGroup, resId: Int) : super(context, parent, resId)

    override fun show(position: Int) {
        entity?.run {
            itemView.tv_week_repeat_title.text = text
            itemView.iv_week_repeat_selected.setImageResource(
                if (value == 1) R.mipmap.icon_checked
                else R.mipmap.icon_unchecked
            )
            itemView.tv_week_repeat_commit.visibility =
                if (position == 6) View.VISIBLE else View.GONE
        }
        itemView.tv_week_repeat_commit.setOnClickListener {
            recyclerItemView?.doAction(this, it, -1)
        }
        itemView.tv_week_repeat_title.setOnClickListener {
            recyclerItemView?.doAction(this, it, position)
        }
    }
}