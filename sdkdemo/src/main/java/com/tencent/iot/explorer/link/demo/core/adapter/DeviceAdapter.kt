package com.tencent.iot.explorer.link.demo.core.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import com.tencent.iot.explorer.link.core.auth.entity.DeviceEntity
import com.tencent.iot.explorer.link.demo.core.holder.BaseHolder
import com.tencent.iot.explorer.link.demo.core.holder.DeviceHolder
import com.tencent.iot.explorer.link.demo.databinding.ItemDeviceBinding

class DeviceAdapter : BaseAdapter {

    constructor(context: Context, list: List<DeviceEntity>) : super(context, list)

    override fun createHolder(parent: ViewGroup, viewType: Int): BaseHolder<*, *> {
        val binding = ItemDeviceBinding.inflate(mInflater, parent, false)
        return DeviceHolder(binding)
    }

}