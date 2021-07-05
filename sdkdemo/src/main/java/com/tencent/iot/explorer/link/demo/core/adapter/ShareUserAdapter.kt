package com.tencent.iot.explorer.link.demo.core.adapter

import android.content.Context
import android.view.ViewGroup
import com.tencent.iot.explorer.link.demo.R
import com.tencent.iot.explorer.link.demo.core.holder.BaseHolder
import com.tencent.iot.explorer.link.demo.core.holder.ShareUserHolder

class ShareUserAdapter : BaseAdapter {
    constructor(context: Context, list: List<Any>) : super(context, list)

    override fun createHolder(parent: ViewGroup, viewType: Int): BaseHolder<*> {
        return ShareUserHolder(mContext, parent, R.layout.item_share_user)
    }
}