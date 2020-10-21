package com.tencent.iot.explorer.link.core.demo.adapter

import android.content.Context
import android.view.ViewGroup
import com.tencent.iot.explorer.link.core.auth.entity.DeviceEntity
import com.tencent.iot.explorer.link.core.demo.R
import com.tencent.iot.explorer.link.core.demo.holder.BaseHolder
import com.tencent.iot.explorer.link.core.demo.holder.DeviceHolder

class DeviceAdapter : BaseAdapter {

    constructor(context: Context, list: List<DeviceEntity>) : super(context, list)

    override fun createHolder(parent: ViewGroup, viewType: Int): BaseHolder<*> {
        return DeviceHolder(mContext, parent, R.layout.item_device)
    }

}