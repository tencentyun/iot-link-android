package com.tencent.iot.explorer.link.kitlink.holder

import android.view.View
import com.tencent.iot.explorer.link.customview.recyclerview.CRecyclerView
import kotlinx.android.synthetic.main.item_country_code_key.view.*

class CountryCodeKeyViewHolder : CRecyclerView.CViewHolder<String> {

    constructor(itemView: View) : super(itemView)

    override fun show(position: Int) {
        itemView.tv_country_code_key.text = entity ?: ""
    }
}