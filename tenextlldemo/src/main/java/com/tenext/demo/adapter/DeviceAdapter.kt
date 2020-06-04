package com.tenext.demo.adapter

import android.content.Context
import android.view.ViewGroup
import com.tenext.auth.entity.Device
import com.tenext.demo.R
import com.tenext.demo.holder.BaseHolder
import com.tenext.demo.holder.DeviceHolder

class DeviceAdapter : BaseAdapter {

    constructor(context: Context, list: List<Device>) : super(context, list)

    override fun createHolder(parent: ViewGroup, viewType: Int): BaseHolder<*> {
        return DeviceHolder(mContext, parent, R.layout.item_device)
    }

}