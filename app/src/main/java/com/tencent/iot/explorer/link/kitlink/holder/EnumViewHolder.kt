package com.tencent.iot.explorer.link.kitlink.holder

import android.view.View
import com.tencent.iot.explorer.link.R
import com.tencent.iot.explorer.link.kitlink.popup.EnumPopupWindow
import com.tencent.iot.explorer.link.customview.recyclerview.CRecyclerView
import kotlinx.android.synthetic.main.item_enum.view.*

class EnumViewHolder : CRecyclerView.CViewHolder<String> {

    private var window: EnumPopupWindow

    constructor(window: EnumPopupWindow, itemView: View) : super(itemView) {
        this.window = window
    }

    override fun show(position: Int) {
        entity?.let {
            itemView.tv_enum_title.text = window.getShowText(it)
            itemView.iv_enum_status.setImageResource(
                if (window.selectKey == it) {
                    R.mipmap.icon_checked
                } else {
                    R.mipmap.icon_unchecked
                }
            )
            itemView.setOnClickListener {
                recyclerItemView?.doAction(this, itemView, position)
            }
        }
    }
}