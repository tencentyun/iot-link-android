package com.tencent.iot.explorer.link.kitlink.holder

import android.content.Context
import android.view.KeyEvent
import android.view.ViewGroup
import com.tencent.iot.explorer.link.R
import com.tencent.iot.explorer.link.kitlink.entity.DeviceEntity
import com.tencent.iot.explorer.link.kitlink.entity.DevicePropertyEntity
import com.tencent.iot.explorer.link.DataHolder
import com.tencent.iot.explorer.link.customview.recyclerview.CRecyclerView
import kotlinx.android.synthetic.main.control_standard_big_switch.view.*

/**
 * 标准主题大按钮：布尔类型
 */
class ControlStandardSwitchBigHolder : CRecyclerView.CViewHolder<DevicePropertyEntity> {

    constructor(context: Context, parent: ViewGroup, resId: Int) : super(context, parent, resId)

    override fun show(position: Int) {
        entity?.run {
            itemView.tv_standard_big_switch_text.text = if (getValue() == "1") {
                itemView.iv_standard_big_switch.setImageResource(R.drawable.icon_control_standard_switch_on_1)
                "$name：${getString(R.string.on)}"
            } else {
                itemView.iv_standard_big_switch.setImageResource(R.drawable.icon_control_standard_switch_off_1)
                "$name：${getString(R.string.off)}"
            }
            DataHolder.instance.get<DeviceEntity>("device")?.let {
                if (it.online == 1) {
                    itemView.tab_standard_switch.setOnTouchListener { v, event ->
                        when (event.action) {
                            KeyEvent.ACTION_DOWN -> {
                                if (getValue() == "1")
                                    itemView.iv_standard_big_switch.setImageResource(R.drawable.icon_control_standard_switch_on_2)
                                else
                                    itemView.iv_standard_big_switch.setImageResource(R.drawable.icon_control_standard_switch_off_2)
                            }
                            KeyEvent.ACTION_UP -> {
                                if (getValue() == "1")
                                    itemView.iv_standard_big_switch.setImageResource(R.drawable.icon_control_standard_switch_on_1)
                                else
                                    itemView.iv_standard_big_switch.setImageResource(R.drawable.icon_control_standard_switch_off_1)
                                recyclerItemView?.doAction(
                                    this@ControlStandardSwitchBigHolder,
                                    v,
                                    position
                                )
                            }
                        }
                        true
                    }
                }
            }
        }
    }
}