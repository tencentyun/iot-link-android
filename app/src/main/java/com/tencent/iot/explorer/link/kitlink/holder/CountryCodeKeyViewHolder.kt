package com.kitlink.holder

import android.view.View
import com.view.recyclerview.CRecyclerView
import kotlinx.android.synthetic.main.item_country_code_key.view.*

class CountryCodeKeyViewHolder : CRecyclerView.CViewHolder<String> {

    constructor(itemView: View) : super(itemView)

    override fun show(position: Int) {
        itemView.tv_country_code_key.text = entity ?: ""
    }
}