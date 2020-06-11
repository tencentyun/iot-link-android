package com.tencent.iot.explorer.link.kitlink.holder

import android.content.Context
import android.text.TextUtils
import android.view.View
import android.view.ViewGroup
import com.tencent.iot.explorer.link.kitlink.R
import com.tencent.iot.explorer.link.kitlink.entity.DeviceEntity
import com.tencent.iot.explorer.link.mvp.presenter.HomeFragmentPresenter
import com.view.recyclerview.CRecyclerView
import com.yho.image.imp.ImageManager
import kotlinx.android.synthetic.main.item_device.view.*

class DeviceViewHolder : CRecyclerView.CViewHolder<DeviceEntity> {

    constructor(context: Context, parent: ViewGroup, resId: Int) : super(context, parent, resId)

    override fun show(position: Int) {
        entity?.let {
            itemView.tv_device_name.text = it.getAlias()
            itemView.tv_device_status.text = if (it.online == 1) {
                itemView.tv_device_status.setTextColor(itemView.resources.getColor(R.color.green_1aad19))
                itemView.context.getString(R.string.online)
            } else {
                itemView.tv_device_status.setTextColor(itemView.resources.getColor(R.color.gray_cccccc))
                itemView.context.getString(R.string.offline)
            }
            ImageManager.setImagePath(itemView.context, itemView.iv_item_device, it.IconUrl, 0)
        }
        itemView.setOnClickListener {
            recyclerItemView?.doAction(this, it, position)
        }
    }
}