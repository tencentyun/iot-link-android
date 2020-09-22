package com.tencent.iot.explorer.link.kitlink.holder

import android.view.View
import com.tencent.iot.explorer.link.kitlink.entity.DeviceScannedEntity
import com.tencent.iot.explorer.link.customview.recyclerview.CRecyclerView
import kotlinx.android.synthetic.main.item_scanned_device.view.*

class DeviceListViewHolder : CRecyclerView.CViewHolder<DeviceScannedEntity> {
    constructor(itemView: View) : super(itemView)

    override fun show(position: Int) {
        entity?.run {
            itemView.tv_scanned_device_name.text = DeviceName
        }
        itemView.tv_connect.setOnClickListener { recyclerItemView?.doAction(this, itemView, position) }
    }
}