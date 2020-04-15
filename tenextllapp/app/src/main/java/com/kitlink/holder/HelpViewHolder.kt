package com.kitlink.holder

import android.view.View
import com.view.recyclerview.CRecyclerView
import kotlinx.android.synthetic.main.item_help.view.*

class HelpViewHolder : CRecyclerView.CViewHolder<String> {
    constructor(itemView: View) : super(itemView)

    override fun show(position: Int) {
        entity?.let {
            itemView.tv_help_text.text = it
            itemView.setOnClickListener {
                recyclerItemView?.doAction(this, itemView, position)
            }
        }
    }
}