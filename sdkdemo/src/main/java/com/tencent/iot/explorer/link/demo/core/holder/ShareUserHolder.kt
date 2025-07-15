package com.tencent.iot.explorer.link.demo.core.holder

import android.text.TextUtils
import com.squareup.picasso.Picasso
import com.tencent.iot.explorer.link.demo.common.util.DateFormatUtil
import com.tencent.iot.explorer.link.core.link.entity.ShareUserEntity
import com.tencent.iot.explorer.link.demo.databinding.ItemShareUserBinding

class ShareUserHolder(binding: ItemShareUserBinding) : BaseHolder<ShareUserEntity, ItemShareUserBinding>(binding) {
    override fun show(holder: BaseHolder<*, *>, position: Int) {
        with(binding) {
            data.run {
                tvItemUserAlias.text = NickName
                if (!TextUtils.isEmpty(Avatar))
                    Picasso.get().load(Avatar).into(ivItemShareUser)
                //2020-01-08  14：02
                tvItemUserDate.text =
                    DateFormatUtil.forString(BindTime * 1000, "yyyy-MM-dd HH:mm")
            }
            rlDeleteShareUser.setOnClickListener {
                clickItem(this@ShareUserHolder, itemView, position)
            }
        }
    }
}