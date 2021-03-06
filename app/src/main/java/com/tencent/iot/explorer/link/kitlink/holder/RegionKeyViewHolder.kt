package com.tencent.iot.explorer.link.kitlink.holder

import android.view.View
import com.tencent.iot.explorer.link.customview.recyclerview.CRecyclerView
import com.tencent.iot.explorer.link.kitlink.entity.RegionEntity
import kotlinx.android.synthetic.main.item_time_zone_key.view.*

class RegionKeyViewHolder : CRecyclerView.CViewHolder<RegionEntity> {
    constructor(itemView: View) : super(itemView)

    override fun show(position: Int) {
        entity?.run {
            itemView.tv_key.text = Title
        }
    }
}