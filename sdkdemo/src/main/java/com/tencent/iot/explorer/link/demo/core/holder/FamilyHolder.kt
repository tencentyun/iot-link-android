package com.tencent.iot.explorer.link.demo.core.holder

import com.tencent.iot.explorer.link.core.auth.entity.FamilyEntity
import com.tencent.iot.explorer.link.demo.App
import com.tencent.iot.explorer.link.demo.R
import com.tencent.iot.explorer.link.demo.databinding.ItemFamilyBinding

class FamilyHolder(binding: ItemFamilyBinding) : BaseHolder<FamilyEntity, ItemFamilyBinding>(binding) {
    override fun show(holder: BaseHolder<*, *>, position: Int) {
        binding.tvFamilyName.run {
            text = data.FamilyName
            setTextColor(
                if (App.data.getCurrentFamily().FamilyId == data.FamilyId) {
                    getColor(R.color.blue_006EFF)
                } else {
                    getColor(R.color.black_333333)
                }
            )
            setOnClickListener {
                clickItem(this@FamilyHolder, it, position)
            }
        }
    }
}