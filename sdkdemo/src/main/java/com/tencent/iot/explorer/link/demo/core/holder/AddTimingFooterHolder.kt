package com.tencent.iot.explorer.link.demo.core.holder

import com.tencent.iot.explorer.link.demo.databinding.FootAddTimingBinding

class AddTimingFooterHolder(binding: FootAddTimingBinding) : BaseHolder<String, FootAddTimingBinding>(binding) {
    override fun show(holder: BaseHolder<*, *>, position: Int) {
        binding.tvSaveTimingProject.setOnClickListener {
            clickItem(this, it, 0)
        }
    }
}