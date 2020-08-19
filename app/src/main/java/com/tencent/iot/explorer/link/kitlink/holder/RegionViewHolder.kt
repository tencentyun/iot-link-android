package com.tencent.iot.explorer.link.kitlink.holder

import android.view.View
import com.tencent.iot.explorer.link.customview.recyclerview.CRecyclerView
import com.tencent.iot.explorer.link.kitlink.entity.RegionEntity
import kotlinx.android.synthetic.main.item_time_zone.view.*

class RegionViewHolder : CRecyclerView.CViewHolder<RegionEntity> {

    constructor(itemView: View) : super(itemView)

    override fun show(position: Int) {
        entity?.run {
            itemView.tv_title.text = Title
            itemView.tv_tz.text = ""
            itemView.setOnClickListener {
                recyclerItemView?.doAction(this@RegionViewHolder, it, position)
            }
        }
    }
}