package com.tencent.iot.explorer.link.demo.core.holder

import com.squareup.picasso.Picasso
import com.tencent.iot.explorer.link.core.auth.entity.DeviceEntity
import com.tencent.iot.explorer.link.demo.R
import com.tencent.iot.explorer.link.demo.databinding.ItemDeviceBinding

class DeviceHolder(binding: ItemDeviceBinding) : BaseHolder<DeviceEntity, ItemDeviceBinding>(binding) {
    override fun show(holder: BaseHolder<*, *>,  position: Int) {
        binding.tvDeviceName.text = data.getAlias()
        binding.tvDeviceStatus.text = if (data.online == 1) {
            binding.tvDeviceStatus.setTextColor(getColor(R.color.green_1aad19))
            "在线"
        } else {
            binding.tvDeviceStatus.setTextColor(getColor(R.color.gray_cccccc))
            "离线"
        }
        Picasso.get().load(data.IconUrl).into(binding.ivItemDevice)
        itemView.setOnClickListener {
            clickItem(this, it, position)
        }
    }
}