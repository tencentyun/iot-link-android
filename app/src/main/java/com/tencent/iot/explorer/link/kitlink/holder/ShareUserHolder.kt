package com.tencent.iot.explorer.link.kitlink.holder

import android.content.Context
import android.view.ViewGroup
import com.tencent.iot.explorer.link.R
import com.tencent.iot.explorer.link.core.link.entity.ShareUserEntity
import com.tencent.iot.explorer.link.customview.recyclerview.CRecyclerView
import com.tencent.iot.explorer.link.kitlink.util.DateUtils
import com.tencent.iot.explorer.link.kitlink.util.picture.imp.ImageManager
import kotlinx.android.synthetic.main.item_share_user.view.*

/**
 * 分享用户viewholder
 */
class ShareUserHolder : CRecyclerView.CViewHolder<ShareUserEntity> {

    constructor(context: Context, parent: ViewGroup, resId: Int) : super(context, parent, resId)

    override fun show(position: Int) {
        entity?.run {
            itemView.tv_item_user_alias.text = NickName
            ImageManager.setImagePath(
                itemView.context,
                itemView.iv_item_share_user,
                Avatar,
                R.mipmap.image_default_portrait
            )
            //2020-01-08  14：02
            itemView.tv_item_user_date.text =
                DateUtils.forString(BindTime * 1000, "yyyy-MM-dd HH:mm")
        }
        itemView.rl_delete_share_user.setOnClickListener {
            recyclerItemView?.doAction(
                this,
                itemView,
                position
            )
        }
    }

}