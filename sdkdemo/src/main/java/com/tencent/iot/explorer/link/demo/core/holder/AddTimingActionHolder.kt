package com.tencent.iot.explorer.link.demo.core.holder

import android.view.View
import com.alibaba.fastjson.JSONObject
import com.tencent.iot.explorer.link.core.auth.entity.ControlPanel
import com.tencent.iot.explorer.link.demo.databinding.ItemDeviceActionBinding

class AddTimingActionHolder(binding: ItemDeviceActionBinding) : BaseHolder<ControlPanel, ItemDeviceActionBinding>(binding) {

    lateinit var deviceAction: JSONObject

    override fun show(holder: BaseHolder<*, *>, position: Int) {
        data.run {
            binding.tvAddTimerName.text = name
            deviceAction.run {
                if (this.containsKey(id)) {
                    binding.tvAddDeviceAction.visibility = View.VISIBLE
                    binding.ivAddDeviceActionNext.visibility = View.VISIBLE
                    binding.ivAddDeviceAction.visibility = View.INVISIBLE
                    binding.tvAddDeviceAction.text =define?.getText(getString(id))
                } else {
                    binding.tvAddDeviceAction.visibility = View.INVISIBLE
                    binding.ivAddDeviceActionNext.visibility = View.INVISIBLE
                    binding.ivAddDeviceAction.visibility = View.VISIBLE
                }
            }
        }
        itemView.setOnClickListener { clickItem(this, it, position) }
    }

}