package com.tencent.iot.explorer.link.demo.core.holder

import android.view.View
import com.tencent.iot.explorer.link.core.auth.entity.FamilyEntity
import com.tencent.iot.explorer.link.demo.databinding.ItemFamilyListBinding

/**
 * 家庭管理列表
 */
class FamilyListHolder(binding: ItemFamilyListBinding) : BaseHolder<FamilyEntity, ItemFamilyListBinding>(binding) {
    override fun show(holder: BaseHolder<*, *>, position: Int) {
        data.run {
            binding.tvFamilyName.text = FamilyName
            binding.familyListTopSpace.visibility =
                if (position == 0) View.VISIBLE else View.GONE
        }
        itemView.setOnClickListener { clickItem(this, itemView, position) }
    }

}