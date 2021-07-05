package com.tencent.iot.explorer.link.demo.core.holder

import android.content.Context
import android.view.ViewGroup
import com.tencent.iot.explorer.link.core.auth.entity.Mapping
import com.tencent.iot.explorer.link.demo.R
import com.tencent.iot.explorer.link.demo.common.log.L
import com.tencent.iot.explorer.link.demo.core.popup.EnumPopupWindow
import kotlinx.android.synthetic.main.item_enum.view.*

class EnumHolder : BaseHolder<Mapping> {

    private var window: EnumPopupWindow

    constructor(context: Context, window: EnumPopupWindow, root: ViewGroup, resLayout: Int) : super(
        context,
        root,
        resLayout
    ) {
        this.window = window
    }

    override fun show(holder: BaseHolder<*>, position: Int) {
        data.run {
            L.e("key=$key,value=$value")
            itemView.tv_enum_title.text = key
            itemView.iv_enum_status.setImageResource(
                if (window.selectValue == value) {
                    R.mipmap.icon_checked
                } else {
                    R.mipmap.icon_unchecked
                }
            )
        }
        itemView.setOnClickListener {
            clickItem(this, itemView, position)
        }
    }
}