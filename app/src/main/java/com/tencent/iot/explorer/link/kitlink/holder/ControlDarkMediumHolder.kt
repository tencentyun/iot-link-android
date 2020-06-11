package com.tencent.iot.explorer.link.kitlink.holder

import android.content.Context
import android.text.TextUtils
import android.view.View
import android.view.ViewGroup
import com.tencent.iot.explorer.link.kitlink.R
import com.tencent.iot.explorer.link.kitlink.entity.DevicePropertyEntity
import com.util.date.DateFormatUtil
import com.view.recyclerview.CRecyclerView
import kotlinx.android.synthetic.main.control_dark_medium.view.*
import java.lang.Exception

/**
 * 暗黑主题中按钮：
 */
class ControlDarkMediumHolder : CRecyclerView.CViewHolder<DevicePropertyEntity> {

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
                    recyclerItemView?.doAction(this@ControlDarkMediumHolder, it, position)
                }
                itemView.setOnClickListener(null)
            } else {
                itemView.dark_medium_switch.visibility = View.GONE
                itemView.tv_dark_medium_value.visibility = View.VISIBLE
                itemView.iv_dark_medium_next.visibility = View.VISIBLE
                itemView.dark_medium_switch.setOnClickListener(null)
                itemView.setOnClickListener {
                    recyclerItemView?.doAction(this@ControlDarkMediumHolder, it, position)
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