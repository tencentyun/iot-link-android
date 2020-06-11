package com.tencent.iot.explorer.link.kitlink.holder

import android.content.Context
import android.view.ViewGroup
import com.tencent.iot.explorer.link.kitlink.entity.DeviceEntity
import com.tencent.iot.explorer.link.kitlink.entity.DevicePropertyEntity
import com.tencent.iot.explorer.link.kitlink.util.DataHolder
import com.view.recyclerview.CRecyclerView
import kotlinx.android.synthetic.main.control_dark_long_switch.view.*

/**
 * 暗黑主题长按钮：布尔
 */
class ControlDarkSwitchLongHolder : CRecyclerView.CViewHolder<DevicePropertyEntity> {

    constructor(context: Context, parent: ViewGroup, resId: Int) : super(context, parent, resId)

    override fun show(position: Int) {
        entity?.run {
            itemView.tv_dark_long_switch_name.text = name
            itemView.dark_long_switch.isChecked = (getValue() == "1")
        }
        itemView.dark_long_switch.isEnabled =
            DataHolder.instance.get<DeviceEntity>("device")?.online ?: 0 == 1
        itemView.dark_long_switch.setOnClickListener {
            recyclerItemView?.doAction(this, it, position)
        }
    }

}