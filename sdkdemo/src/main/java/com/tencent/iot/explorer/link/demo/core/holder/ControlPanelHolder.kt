package com.tencent.iot.explorer.link.demo.core.holder

import com.tencent.iot.explorer.link.core.auth.entity.ControlPanel
import com.tencent.iot.explorer.link.demo.databinding.ItemControlPanelBinding

class ControlPanelHolder(binding: ItemControlPanelBinding) : BaseHolder<ControlPanel, ItemControlPanelBinding>(binding) {
    override fun show(holder: BaseHolder<*, *>, position: Int) {
        data.run {
            binding.tvTemplateName.text = name
            when (id) {
                "tp", "room", "share" -> {
                    binding.tvBtnType.text = ""
                    binding.tvTemplateValue.text = ""
                }
                "alias" -> {
                    binding.tvBtnType.text = ""
                    binding.tvTemplateValue.text = value
                }
                else -> {
//                    itemView.tv_btn_type.text = "按钮类型：$type，图标：$icon"
                    binding.tvTemplateValue.text = define?.getText(value) ?: "0"
                }
            }
        }

        itemView.setOnClickListener {
            clickItem(this, itemView, position)
        }
    }
}