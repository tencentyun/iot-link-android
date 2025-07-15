package com.tencent.iot.explorer.link.demo.core.holder

import android.text.TextUtils
import com.squareup.picasso.Picasso
import com.tencent.iot.explorer.link.demo.R
import com.tencent.iot.explorer.link.core.link.entity.MemberEntity
import com.tencent.iot.explorer.link.demo.databinding.ItemMemberBinding

class MemberHolder(binding: ItemMemberBinding) : BaseHolder<MemberEntity, ItemMemberBinding>(binding) {
    override fun show(holder: BaseHolder<*, *>, position: Int) {
        data.run {
            binding.tvMemberItemName.text = NickName
            if (!TextUtils.isEmpty(Avatar))
                Picasso.get().load(Avatar).into(binding.ivMemberItemPortrait)
            binding.tvMemberItemRole.text = if (Role == 1)
                getString(R.string.role_owner) else getString(R.string.role_member)
        }
        itemView.setOnClickListener { clickItem(this, itemView, position) }
    }
}