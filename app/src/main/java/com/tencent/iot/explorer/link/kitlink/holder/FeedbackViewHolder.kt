package com.tencent.iot.explorer.link.kitlink.holder

import android.text.TextUtils
import android.view.View
import com.tencent.iot.explorer.link.R
import com.tencent.iot.explorer.link.kitlink.activity.FeedbackActivity
import com.tencent.iot.explorer.link.customview.recyclerview.CRecyclerView
import com.tencent.iot.explorer.link.util.picture.imp.ImageManager
import kotlinx.android.synthetic.main.item_feekback.view.*

class FeedbackViewHolder : CRecyclerView.CViewHolder<FeedbackActivity.PathUrlEntity> {
    constructor(itemView: View) : super(itemView)

    override fun show(position: Int) {
        entity?.run {
            if (!TextUtils.isEmpty(url)) {
                itemView.iv_feedback.let {
                    ImageManager.setImagePath(itemView.context, it, url, it.width, it.height)
                }
                itemView.iv_feedback_delete.visibility = View.VISIBLE
            } else {
                itemView.iv_feedback.setImageResource(R.drawable.image_add)
                itemView.iv_feedback_delete.visibility = View.INVISIBLE
            }
            itemView.setOnClickListener {
                recyclerItemView?.doAction(
                    this@FeedbackViewHolder,
                    itemView,
                    position
                )
            }
            itemView.iv_feedback_delete.setOnClickListener {
                recyclerItemView?.doAction(
                    this@FeedbackViewHolder,
                    itemView.iv_feedback_delete,
                    position
                )
            }
        }
    }
}