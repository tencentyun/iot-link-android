package com.tencent.iot.explorer.link.demo.core.adapter

import android.view.View
import com.tencent.iot.explorer.link.core.auth.entity.FamilyEntity
import com.tencent.iot.explorer.link.demo.core.holder.BaseHolder
import com.tencent.iot.explorer.link.demo.databinding.ItemFamilyInfoBinding

class FamilyInfoHolder(binding: ItemFamilyInfoBinding) : BaseHolder<FamilyEntity, ItemFamilyInfoBinding>(binding) {
    override fun show(holder: BaseHolder<*, *>, position: Int) {
        with(binding) {
            data.run {
                tvHeadFamilyName.text = FamilyName
                clHeadFamilyInvite.visibility = if (Role == 1) View.VISIBLE else View.GONE
            }
            tvHeadFamilyNameTitle.setOnClickListener {
                clickItem(this@FamilyInfoHolder, it, -1)
            }
            tvHeadRoomManageTitle.setOnClickListener {
                clickItem(this@FamilyInfoHolder, it, -2)
            }
            tvHeadFamilyInvite.setOnClickListener {
                clickItem(this@FamilyInfoHolder, it, -3)
            }
        }
    }
}