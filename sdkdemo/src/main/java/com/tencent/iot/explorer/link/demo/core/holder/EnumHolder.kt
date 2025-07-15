package com.tencent.iot.explorer.link.demo.core.holder

import com.tencent.iot.explorer.link.core.auth.entity.Mapping
import com.tencent.iot.explorer.link.demo.R
import com.tencent.iot.explorer.link.demo.common.log.L
import com.tencent.iot.explorer.link.demo.core.popup.EnumPopupWindow
import com.tencent.iot.explorer.link.demo.databinding.ItemEnumBinding

class EnumHolder : BaseHolder<Mapping, ItemEnumBinding> {

    private var window: EnumPopupWindow

    constructor(binding: ItemEnumBinding, window: EnumPopupWindow) : super(binding) {
        this.window = window
    }

    override fun show(holder: BaseHolder<*, *>, position: Int) {
        data.run {
            L.e("key=$key,value=$value")
            binding.tvEnumTitle.text = key
            binding.ivEnumStatus.setImageResource(
                if (window.selectValue == value) {
                    R.mipmap.icon_checked
                } else {
                    R.mipmap.icon_unchecked
                }
            )
        }
        itemView.setOnClickListener {
            clickItem(this, itemView, position)
        }
    }
}