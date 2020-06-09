package com.kitlink.holder

import android.content.Context
import android.view.ViewGroup
import com.kitlink.entity.DeviceScannedEntity
import com.view.recyclerview.CRecyclerView
import kotlinx.android.synthetic.main.item_scanned_device.view.*

class DeviceListViewHolder : CRecyclerView.CViewHolder<DeviceScannedEntity> {
    constructor(context: Context, parent: ViewGroup, resId: Int) : super(context, parent, resId)

    override fun show(position: Int) {
        entity?.run {
            itemView.tv_scanned_device_name.text = DeviceName
        }
        itemView.tv_connect.setOnClickListener { recyclerItemView?.doAction(this, itemView, position) }
    }
}