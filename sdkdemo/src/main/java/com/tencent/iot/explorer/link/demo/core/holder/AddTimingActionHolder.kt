package com.tencent.iot.explorer.link.demo.core.holder

import android.content.Context
import android.view.View
import android.view.ViewGroup
import com.alibaba.fastjson.JSONObject
import com.tencent.iot.explorer.link.core.auth.entity.ControlPanel
import kotlinx.android.synthetic.main.item_device_action.view.*

class AddTimingActionHolder : BaseHolder<ControlPanel> {

    lateinit var deviceAction: JSONObject

    constructor(context: Context, root: ViewGroup, resLayout: Int) : super(context, root, resLayout)

    override fun show(holder: BaseHolder<*>, position: Int) {
        data.run {
            itemView.tv_add_timer_name.text = name
            deviceAction.run {
                if (this.containsKey(id)) {
                    itemView.tv_add_device_action.visibility = View.VISIBLE
                    itemView.iv_add_device_action_next.visibility = View.VISIBLE
                    itemView.iv_add_device_action.visibility = View.INVISIBLE
                    itemView.tv_add_device_action.text =define?.getText(getString(id))
                } else {
                    itemView.tv_add_device_action.visibility = View.INVISIBLE
                    itemView.iv_add_device_action_next.visibility = View.INVISIBLE
                    itemView.iv_add_device_action.visibility = View.VISIBLE
                }
            }
        }
        itemView.setOnClickListener { clickItem(this, it, position) }
    }

}