package com.tencent.iot.explorer.link.core.demo.adapter

import android.content.Context
import android.view.ViewGroup
import android.widget.TimePicker
import com.alibaba.fastjson.JSONObject
import com.tencent.iot.explorer.link.core.auth.entity.ControlPanel
import com.tencent.iot.explorer.link.core.demo.R
import com.tencent.iot.explorer.link.core.demo.entity.TimingProject
import com.tencent.iot.explorer.link.core.demo.holder.AddTimingFooterHolder
import com.tencent.iot.explorer.link.core.demo.holder.AddTimingActionHolder
import com.tencent.iot.explorer.link.core.demo.holder.AddTimingHeaderHolder
import com.tencent.iot.explorer.link.core.demo.holder.BaseHolder

class AddTimingProjectAdapter : BaseAdapter {

    var onTimeChangedListener: TimePicker.OnTimeChangedListener? = null
    lateinit var deviceAction: JSONObject

    constructor(context: Context, list: List<Any>) : super(context, list)

    override fun getItemViewType(position: Int): Int {
        return when (data(position)) {
            is TimingProject -> 0
            is ControlPanel -> 1
            else -> 2
        }
    }

    override fun createHolder(parent: ViewGroup, viewType: Int): BaseHolder<*> {
        return when (viewType) {
            0 -> {
                val holder = AddTimingHeaderHolder(mContext, parent, R.layout.head_add_timing)
                holder.onTimeChangedListener = onTimeChangedListener
                holder
            }
            1 -> {
                val holder = AddTimingActionHolder(mContext, parent, R.layout.item_device_action)
                holder.deviceAction = deviceAction
                holder
            }
            else -> AddTimingFooterHolder(mContext, parent, R.layout.foot_add_timing)
        }
    }

}