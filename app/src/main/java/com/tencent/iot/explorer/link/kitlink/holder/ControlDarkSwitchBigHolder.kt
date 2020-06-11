package com.tencent.iot.explorer.link.kitlink.holder

import android.view.KeyEvent
import android.view.View
import com.tencent.iot.explorer.link.kitlink.R
import com.tencent.iot.explorer.link.kitlink.entity.DeviceEntity
import com.tencent.iot.explorer.link.kitlink.entity.DevicePropertyEntity
import com.tencent.iot.explorer.link.kitlink.util.DataHolder
import com.view.recyclerview.CRecyclerView
import kotlinx.android.synthetic.main.control_dark_big_switch.view.*

/**
 * 暗黑主题大按钮：布尔 开关类型
 */
class ControlDarkSwitchBigHolder : CRecyclerView.CViewHolder<DevicePropertyEntity> {

    constructor(itemView: View) : super(itemView)

    override fun show(position: Int) {//1125*1482
        entity?.run {
            itemView.tv_dark_big_switch_text.text = if (getValue() == "1") {
                itemView.iv_dark_big_switch.setImageResource(R.drawable.icon_control_dark_switch_on_1)
                "$name：${getString(R.string.on)}"
            } else {
                itemView.iv_dark_big_switch.setImageResource(R.drawable.icon_control_dark_switch_off_1)
                "$name：${getString(R.string.off)}"
            }
            DataHolder.instance.get<DeviceEntity>("device")?.let {
                if (it.online == 1) {
                    itemView.tab_dark_switch.setOnTouchListener { v, event ->
                        when (event.action) {
                            KeyEvent.ACTION_DOWN -> {
                                if (getValue() == "1")
                                    itemView.iv_dark_big_switch.setImageResource(R.drawable.icon_control_dark_switch_on_2)
                                else
                                    itemView.iv_dark_big_switch.setImageResource(R.drawable.icon_control_dark_switch_off_2)
                            }
                            KeyEvent.ACTION_UP -> {
                                if (getValue() == "1")
                                    itemView.iv_dark_big_switch.setImageResource(R.drawable.icon_control_dark_switch_on_1)
                                else
                                    itemView.iv_dark_big_switch.setImageResource(R.drawable.icon_control_dark_switch_off_1)
                                recyclerItemView?.doAction(
                                    this@ControlDarkSwitchBigHolder,
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

    private fun dp2px(dp: Int): Int {
        return (itemView.resources.displayMetrics.density * dp + 0.5).toInt()
    }

    private fun ivHeight(): Int {
        return ivWidth() * 1482 / 1125
    }

    private fun ivWidth(): Int {
        return itemView.resources.displayMetrics.widthPixels
    }
}