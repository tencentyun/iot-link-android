package com.tencent.iot.explorer.link.kitlink.holder

import android.view.View
import com.tencent.iot.explorer.link.customview.recyclerview.CRecyclerView
import com.tencent.iot.explorer.link.kitlink.entity.TimeZoneEntity
import kotlinx.android.synthetic.main.item_country_code_value.view.*


class TimeZoneViewHolder : CRecyclerView.CViewHolder<TimeZoneEntity> {

    constructor(itemView: View) : super(itemView)

    override fun show(position: Int) {
        entity?.run {
            itemView.tv_country_name.text = Title
            itemView.tv_country_code.text = TZ
            itemView.setOnClickListener {
                recyclerItemView?.doAction(this@TimeZoneViewHolder, it, position)
            }
        }
    }
}