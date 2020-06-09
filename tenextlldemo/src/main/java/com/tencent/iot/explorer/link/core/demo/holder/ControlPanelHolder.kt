package com.tencent.iot.explorer.link.core.demo.holder

import android.content.Context
import android.view.ViewGroup
import com.tencent.iot.explorer.link.core.auth.entity.ControlPanel
import kotlinx.android.synthetic.main.item_control_panel.view.*

class ControlPanelHolder : BaseHolder<ControlPanel> {

    constructor(context: Context, root: ViewGroup, resLayout: Int) : super(context, root, resLayout)

    override fun show(holder: BaseHolder<*>, position: Int) {
        data.run {
            itemView.tv_template_name.text = name
            when (id) {
                "tp", "room", "share" -> {
                    itemView.tv_btn_type.text = ""
                    itemView.tv_template_value.text = ""
                }
                "alias" -> {
                    itemView.tv_btn_type.text = ""
                    itemView.tv_template_value.text = value
                }
                else -> {
                    itemView.tv_btn_type.text = "按钮类型：$type，图标：$icon"
                    itemView.tv_template_value.text = define?.getText(value) ?: "0"
                }
            }
        }
        itemView.setOnClickListener {
            clickItem(this, itemView, position)
        }
    }
}