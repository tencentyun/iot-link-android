package com.tencent.iot.explorer.link.core.demo.holder

import android.content.Context
import android.text.TextUtils
import android.view.ViewGroup
import com.squareup.picasso.Picasso
import com.tencent.iot.explorer.link.core.demo.entity.ShareUser
import com.tencent.iot.explorer.link.core.demo.util.DateFormatUtil
import kotlinx.android.synthetic.main.item_share_user.view.*

class ShareUserHolder : BaseHolder<ShareUser> {
    constructor(context: Context, root: ViewGroup, resLayout: Int) : super(context, root, resLayout)

    override fun show(holder: BaseHolder<*>, position: Int) {
        data.run {
            itemView.tv_item_user_alias.text = NickName
            if (!TextUtils.isEmpty(Avatar))
                Picasso.with(itemView.context).load(Avatar).into(itemView.iv_item_share_user)
            //2020-01-08  14：02
            itemView.tv_item_user_date.text =
                DateFormatUtil.forString(BindTime * 1000, "yyyy-MM-dd HH:mm")
        }
        itemView.rl_delete_share_user.setOnClickListener {
            clickItem(this, itemView, position)
        }
    }
}