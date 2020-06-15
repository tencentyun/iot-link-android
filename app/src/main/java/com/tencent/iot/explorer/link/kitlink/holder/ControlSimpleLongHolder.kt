package com.tencent.iot.explorer.link.kitlink.holder

import android.content.Context
import android.view.ViewGroup
import com.tencent.iot.explorer.link.R
import com.tencent.iot.explorer.link.kitlink.entity.DevicePropertyEntity
import com.view.recyclerview.CRecyclerView
import kotlinx.android.synthetic.main.control_simple_long.view.*

/**
 * 暗黑主题长按钮：布尔类型之外
 */
class ControlSimpleLongHolder : CRecyclerView.CViewHolder<DevicePropertyEntity> {

    constructor(context: Context, parent: ViewGroup, resId: Int) : super(context, parent, resId)

    override fun show(position: Int) {
        entity?.run {
            itemView.tv_simple_long_name.text = name
            itemView.tv_simple_long_value.text = getValueText()
            when (id) {
                "color" -> itemView.iv_simple_long.setImageResource(R.mipmap.icon_control_color)
                "power_switch" -> itemView.iv_simple_long.setImageResource(R.mipmap.icon_control_switch)
                else -> itemView.iv_simple_long.setImageResource(R.mipmap.icon_control_brightness)
            }
        }
        itemView.setOnClickListener { recyclerItemView?.doAction(this, it, position) }
    }


}