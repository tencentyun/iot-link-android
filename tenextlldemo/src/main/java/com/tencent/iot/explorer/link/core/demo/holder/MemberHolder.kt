package com.tencent.iot.explorer.link.core.demo.holder

import android.content.Context
import android.text.TextUtils
import android.view.ViewGroup
import com.squareup.picasso.Picasso
import com.tencent.iot.explorer.link.core.demo.R
import com.tencent.iot.explorer.link.core.demo.entity.Member
import kotlinx.android.synthetic.main.item_member.view.*

class MemberHolder : BaseHolder<Member> {

    constructor(context: Context, root: ViewGroup, resLayout: Int) : super(context, root, resLayout)

    override fun show(holder: BaseHolder<*>, position: Int) {
        data.run {
            itemView.tv_member_item_name.text = NickName
            if (!TextUtils.isEmpty(Avatar))
                Picasso.with(itemView.context).load(Avatar).into(itemView.iv_member_item_portrait)
            itemView.tv_member_item_role.text = if (Role == 1)
                getString(R.string.role_owner) else getString(R.string.role_member)
        }
        itemView.setOnClickListener { clickItem(this, itemView, position) }
    }
}