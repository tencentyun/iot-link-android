package com.tencent.iot.explorer.link.demo.core.adapter

import android.content.Context
import android.view.ViewGroup
import android.widget.TimePicker
import com.alibaba.fastjson.JSONObject
import com.tencent.iot.explorer.link.core.auth.entity.ControlPanel
import com.tencent.iot.explorer.link.demo.core.entity.TimingProject
import com.tencent.iot.explorer.link.demo.core.holder.AddTimingFooterHolder
import com.tencent.iot.explorer.link.demo.core.holder.AddTimingActionHolder
import com.tencent.iot.explorer.link.demo.core.holder.AddTimingHeaderHolder
import com.tencent.iot.explorer.link.demo.core.holder.BaseHolder
import com.tencent.iot.explorer.link.demo.databinding.FootAddTimingBinding
import com.tencent.iot.explorer.link.demo.databinding.HeadAddTimingBinding
import com.tencent.iot.explorer.link.demo.databinding.ItemDeviceActionBinding

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

    override fun createHolder(parent: ViewGroup, viewType: Int): BaseHolder<*, *> {
        return when (viewType) {
            0 -> {
                val binding = HeadAddTimingBinding.inflate(mInflater, parent, false)
                AddTimingHeaderHolder(binding).apply { this.onTimeChangedListener = this@AddTimingProjectAdapter.onTimeChangedListener }
            }

            1 -> {
                val binding = ItemDeviceActionBinding.inflate(mInflater, parent, false)
                AddTimingActionHolder(binding).apply { this.deviceAction = this@AddTimingProjectAdapter.deviceAction }
            }

            else -> {
                val binding = FootAddTimingBinding.inflate(mInflater, parent, false)
                AddTimingFooterHolder(binding)
            }
        }
    }

}