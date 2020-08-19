package com.tencent.iot.explorer.link.kitlink.holder

import android.view.View
import com.tencent.iot.explorer.link.customview.recyclerview.CRecyclerView
import com.tencent.iot.explorer.link.kitlink.entity.TimeZoneEntity
import kotlinx.android.synthetic.main.item_country_code_value.view.*
import kotlinx.android.synthetic.main.item_time_zone.view.*


class TimeZoneViewHolder : CRecyclerView.CViewHolder<TimeZoneEntity> {

    constructor(itemView: View) : super(itemView)

    override fun show(position: Int) {
        entity?.run {
            itemView.tv_title.text = Title
            itemView.tv_tz.text = TZ
            itemView.setOnClickListener {
                recyclerItemView?.doAction(this@TimeZoneViewHolder, it, position)
            }
        }
    }
}