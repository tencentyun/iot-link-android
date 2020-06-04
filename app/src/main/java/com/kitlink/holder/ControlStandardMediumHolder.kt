package com.kitlink.holder

import android.content.Context
import android.view.View
import android.view.ViewGroup
import com.kitlink.R
import com.kitlink.entity.DevicePropertyEntity
import com.util.date.DateFormatUtil
import com.view.recyclerview.CRecyclerView
import kotlinx.android.synthetic.main.control_standard_medium.view.*

/**
 * 暗黑主题中按钮：
 */
class ControlStandardMediumHolder : CRecyclerView.CViewHolder<DevicePropertyEntity> {

    constructor(context: Context, parent: ViewGroup, resId: Int) : super(context, parent, resId)

    override fun show(position: Int) {
        entity?.run {
            itemView.tv_standard_medium_name.text = name
            if (isBoolType()) {
                itemView.standard_medium_switch.visibility = View.VISIBLE
                itemView.tv_standard_medium_value.visibility = View.GONE
                itemView.iv_standard_medium_next.visibility = View.GONE
                itemView.standard_medium_switch.isChecked = (getValue() == "1")
                itemView.standard_medium_switch.setOnClickListener {
                    recyclerItemView?.doAction(this@ControlStandardMediumHolder, it, position)
                }
                itemView.setOnClickListener(null)
            } else {
                itemView.standard_medium_switch.visibility = View.GONE
                itemView.tv_standard_medium_value.visibility = View.VISIBLE
                itemView.iv_standard_medium_next.visibility = View.VISIBLE
                itemView.standard_medium_switch.setOnClickListener(null)
                itemView.setOnClickListener {
                    recyclerItemView?.doAction(this@ControlStandardMediumHolder, it, position)
                }
            }
            itemView.tv_standard_medium_value.text = getValueText()
            when (id) {
                "color" -> itemView.iv_standard_medium.setImageResource(R.mipmap.icon_control_color)
                "power_switch" -> itemView.iv_standard_medium.setImageResource(R.mipmap.icon_control_switch)
                else -> itemView.iv_standard_medium.setImageResource(R.mipmap.icon_control_brightness)
            }
        }
    }

}