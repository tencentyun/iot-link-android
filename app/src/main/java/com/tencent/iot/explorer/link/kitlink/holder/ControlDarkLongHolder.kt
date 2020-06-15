package com.tencent.iot.explorer.link.kitlink.holder

import android.content.Context
import android.view.ViewGroup
import com.tencent.iot.explorer.link.R
import com.tencent.iot.explorer.link.kitlink.entity.DevicePropertyEntity
import com.view.recyclerview.CRecyclerView
import kotlinx.android.synthetic.main.control_dark_long.view.*

/**
 * 暗黑主题长按钮：布尔类型之外
 */
class ControlDarkLongHolder : CRecyclerView.CViewHolder<DevicePropertyEntity> {

    constructor(context: Context, parent: ViewGroup, resId: Int) : super(context, parent, resId)

    override fun show(position: Int) {
        entity?.run {
            itemView.tv_dark_long_name.text = name
            itemView.tv_dark_long_value.text = getValueText()
            when (id) {
                "color" -> itemView.iv_dark_long.setImageResource(R.mipmap.icon_control_color)
                else -> itemView.iv_dark_long.setImageResource(R.mipmap.icon_control_brightness)
            }
        }
        itemView.setOnClickListener { recyclerItemView?.doAction(this, it, position) }
    }

}