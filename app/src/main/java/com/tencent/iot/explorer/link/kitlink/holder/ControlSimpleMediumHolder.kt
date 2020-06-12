package com.tencent.iot.explorer.link.kitlink.holder

import android.content.Context
import android.view.View
import android.view.ViewGroup
import com.tencent.iot.explorer.link.kitlink.R
import com.tencent.iot.explorer.link.kitlink.entity.DevicePropertyEntity
import com.view.recyclerview.CRecyclerView
import kotlinx.android.synthetic.main.control_simple_medium.view.*

/**
 * 暗黑主题中按钮：
 */
class ControlSimpleMediumHolder : CRecyclerView.CViewHolder<DevicePropertyEntity> {

    constructor(context: Context, parent: ViewGroup, resId: Int) : super(context, parent, resId)

    override fun show(position: Int) {
        entity?.run {
            itemView.tv_simple_medium_name.text = name
            if (isBoolType()) {
                itemView.simple_medium_switch.visibility = View.VISIBLE
                itemView.tv_simple_medium_value.visibility = View.GONE
                itemView.iv_simple_medium_next.visibility = View.GONE
                itemView.simple_medium_switch.isChecked = (getValue() == "1")
                itemView.simple_medium_switch.setOnClickListener {
                    recyclerItemView?.doAction(this@ControlSimpleMediumHolder, it, position)
                }
                itemView.setOnClickListener(null)
            } else {
                itemView.simple_medium_switch.visibility = View.GONE
                itemView.tv_simple_medium_value.visibility = View.VISIBLE
                itemView.iv_simple_medium_next.visibility = View.VISIBLE
                itemView.simple_medium_switch.setOnClickListener(null)
                itemView.setOnClickListener {
                    recyclerItemView?.doAction(this@ControlSimpleMediumHolder, it, position)
                }
            }
            itemView.tv_simple_medium_value.text = getValueText()
            when (id) {
                "color" -> itemView.iv_simple_medium.setImageResource(R.mipmap.icon_control_color)
                "power_switch" -> itemView.iv_simple_medium.setImageResource(R.mipmap.icon_control_switch)
                else -> itemView.iv_simple_medium.setImageResource(R.mipmap.icon_control_brightness)
            }
        }
    }

}