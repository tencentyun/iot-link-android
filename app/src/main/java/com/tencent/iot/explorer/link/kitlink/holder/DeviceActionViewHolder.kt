package com.tencent.iot.explorer.link.kitlink.holder

import android.content.Context
import android.view.View
import android.view.ViewGroup
import com.alibaba.fastjson.JSONObject
import com.tencent.iot.explorer.link.kitlink.entity.DevicePropertyEntity
import com.tencent.iot.explorer.link.kitlink.entity.TimerListEntity
import com.tencent.iot.explorer.link.util.L
import com.tencent.iot.explorer.link.customview.recyclerview.CRecyclerView
import kotlinx.android.synthetic.main.item_device_action.view.*

/**
 * 设备动作viewholder
 */
class DeviceActionViewHolder : CRecyclerView.CViewHolder<DevicePropertyEntity> {

    lateinit var deviceAction: JSONObject

    constructor(context: Context, parent: ViewGroup, resId: Int) : super(context, parent, resId)

    override fun show(position: Int) {
        entity?.run {
            itemView.tv_add_timer_name.text = name
            deviceAction.run {
                if (this.containsKey(id)) {
                    itemView.tv_add_device_action.visibility = View.VISIBLE
                    itemView.iv_add_device_action_next.visibility = View.VISIBLE
                    itemView.iv_add_device_action.visibility = View.INVISIBLE
                    itemView.tv_add_device_action.text =
                        getDeviceAction(entity!!, this.getString(id))
                } else {
                    itemView.tv_add_device_action.visibility = View.INVISIBLE
                    itemView.iv_add_device_action_next.visibility = View.INVISIBLE
                    itemView.iv_add_device_action.visibility = View.VISIBLE
                }
            }
        }
        itemView.setOnClickListener { recyclerItemView?.doAction(this, it, position) }
    }

    private fun getDeviceAction(entity: DevicePropertyEntity, value: String): String {
        entity.run {
            return when {
                isNumberType() -> "$value${getUnit()}"
                isBoolType() -> boolEntity!!.getValueText(value)
                isEnumType() -> enumEntity!!.getValueText(value)
                else -> value
            }
        }
    }

}