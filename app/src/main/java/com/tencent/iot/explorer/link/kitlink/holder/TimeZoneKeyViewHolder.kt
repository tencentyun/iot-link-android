package com.tencent.iot.explorer.link.kitlink.holder

import android.view.View
import com.tencent.iot.explorer.link.customview.recyclerview.CRecyclerView
import com.tencent.iot.explorer.link.kitlink.entity.TimeZoneEntity
import kotlinx.android.synthetic.main.item_country_code_key.view.*
import kotlinx.android.synthetic.main.item_time_zone_key.view.*

class TimeZoneKeyViewHolder : CRecyclerView.CViewHolder<TimeZoneEntity> {
    constructor(itemView: View) : super(itemView)

    override fun show(position: Int) {
        entity?.run {
            itemView.tv_key.text = Title ?: ""
        }
    }
}