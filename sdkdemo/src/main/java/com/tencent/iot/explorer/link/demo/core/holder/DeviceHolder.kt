package com.tencent.iot.explorer.link.demo.core.holder

import android.content.Context
import android.view.ViewGroup
import com.squareup.picasso.Picasso
import com.tencent.iot.explorer.link.core.auth.entity.DeviceEntity
import com.tencent.iot.explorer.link.demo.R
import kotlinx.android.synthetic.main.item_device.view.*

class DeviceHolder : BaseHolder<DeviceEntity> {

    constructor(context: Context, root: ViewGroup, resLayout: Int) : super(context, root, resLayout)

    override fun show(holder: BaseHolder<*>,  position: Int) {
        itemView.tv_device_name.text = data.getAlias()
        itemView.tv_device_status.text = if (data.online == 1) {
            itemView.tv_device_status.setTextColor(getColor(R.color.green_1aad19))
            "在线"
        } else {
            itemView.tv_device_status.setTextColor(getColor(R.color.gray_cccccc))
            "离线"
        }
        Picasso.get().load(data.IconUrl).into(itemView.iv_item_device)
        itemView.setOnClickListener {
            clickItem(this, it, position)
        }
    }
}