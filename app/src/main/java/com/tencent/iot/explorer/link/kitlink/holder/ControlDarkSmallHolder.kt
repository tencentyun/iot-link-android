package com.tencent.iot.explorer.link.kitlink.holder

import android.content.Context
import android.view.View
import android.view.ViewGroup
import com.tencent.iot.explorer.link.R
import com.tencent.iot.explorer.link.kitlink.entity.DevicePropertyEntity
import com.view.recyclerview.CRecyclerView
import kotlinx.android.synthetic.main.control_dark_medium.view.*

/**
 * 暗黑主题小按钮
 */
class ControlDarkSmallHolder : CRecyclerView.CViewHolder<DevicePropertyEntity> {

    constructor(context: Context, parent: ViewGroup, resId: Int) : super(context, parent, resId)

    override fun show(position: Int) {
        entity?.run {
            itemView.tv_dark_medium_name.text = name
            if (isBoolType()) {
                itemView.dark_medium_switch.visibility = View.VISIBLE
                itemView.tv_dark_medium_value.visibility = View.GONE
                itemView.iv_dark_medium_next.visibility = View.GONE
                itemView.dark_medium_switch.isChecked = (getValue() == "1")
                itemView.dark_medium_switch.setOnClickListener {
                    recyclerItemView?.doAction(this@ControlDarkSmallHolder, it, position)
                }
                itemView.setOnClickListener(null)
            } else {
                itemView.dark_medium_switch.visibility = View.GONE
                itemView.tv_dark_medium_value.visibility = View.VISIBLE
                itemView.iv_dark_medium_next.visibility = View.VISIBLE
                itemView.dark_medium_switch.setOnClickListener(null)
                itemView.setOnClickListener {
                    recyclerItemView?.doAction(this@ControlDarkSmallHolder, it, position)
                }
            }
            itemView.tv_dark_medium_value.text = getValueText()
            when (id) {
                "color" -> itemView.iv_dark_medium.setImageResource(R.mipmap.icon_control_color)
                "power_switch" -> itemView.iv_dark_medium.setImageResource(R.mipmap.icon_control_switch)
                else -> itemView.iv_dark_medium.setImageResource(R.mipmap.icon_control_brightness)
            }
        }
    }

}