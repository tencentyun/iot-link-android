package com.tencent.iot.explorer.link.kitlink.holder

import android.view.View
import com.tencent.iot.explorer.link.kitlink.entity.CountryCodeEntity
import com.view.recyclerview.CRecyclerView
import kotlinx.android.synthetic.main.item_country_code_value.view.*

class CountryCodeViewHolder : CRecyclerView.CViewHolder<CountryCodeEntity> {

    constructor(itemView: View) : super(itemView)

    override fun show(position: Int) {
        entity?.run {
            itemView.tv_country_name.text = countryName
            itemView.tv_country_code.text = countryCode
            itemView.setOnClickListener {
                recyclerItemView?.doAction(this@CountryCodeViewHolder, it, position)
            }
        }
    }
}