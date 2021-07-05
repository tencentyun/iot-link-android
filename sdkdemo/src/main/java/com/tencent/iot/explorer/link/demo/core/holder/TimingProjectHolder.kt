package com.tencent.iot.explorer.link.demo.core.holder

import android.content.Context
import android.view.View
import android.view.ViewGroup
import com.alibaba.fastjson.JSON
import com.tencent.iot.explorer.link.core.auth.IoTAuth
import com.tencent.iot.explorer.link.core.auth.entity.ControlPanel
import com.tencent.iot.explorer.link.demo.core.entity.TimingProject
import kotlinx.android.synthetic.main.item_timing_project.view.*
import java.lang.StringBuilder
import java.util.Comparator

class TimingProjectHolder : BaseHolder<TimingProject> {
    constructor(context: Context, root: ViewGroup, resLayout: Int) : super(context, root, resLayout)

    override fun show(holder: BaseHolder<*>, position: Int) {
        data.run {
            itemView.tv_timing_project_name.text = this.TimerName
            itemView.tv_timing_project_date.text = "${parseDays()} $TimePoint"
            itemView.tv_timing_project_detail.text = this@TimingProjectHolder.parseData()
            itemView.switch_timing_project.isChecked = (Status == 1)
            itemView.timing_list_top_space.visibility =
                if (position == 0) View.VISIBLE else View.GONE
        }
        itemView.switch_timing_project.setOnClickListener {
            clickItem(this, it, position)
        }
        itemView.cl_timing_list.setOnClickListener {
            clickItem(this, it, position)
        }
        itemView.rl_delete_timing_project.setOnClickListener {
            clickItem(this, it, position)
        }

    }

    /**
     * 解析定时任务
     */
    private fun parseData(): String {
        val sb = StringBuilder()
        data.run {
            val list = arrayListOf<ControlPanel>()
            val data = JSON.parseObject(Data)
            data.keys.forEach {
                val mPanel = ControlPanel()
                mPanel.id = it
                IoTAuth.deviceImpl.panelList().forEachIndexed { _, panel ->
                    if (panel.id == it) {
                        mPanel.name = panel.name
                        mPanel.type = panel.type
                        mPanel.valueType = panel.valueType
                        mPanel.define = panel.define
                        mPanel.value = data.getString(it)
                        return@forEachIndexed
                    }
                }
                list.add(mPanel)
            }
            list.sortWith(object : Comparator<ControlPanel> {
                override fun compare(o1: ControlPanel, o2: ControlPanel): Int {
                    when {
                        o1.isBoolType() -> {
                            if (o2.isBoolType()) {
                                return if (o1.id > o2.id) 1 else -1
                            }
                            return -1
                        }
                        o1.isNumberType() -> {
                            return if (o2.isBoolType()) {
                                1
                            } else if (o2.isNumberType()) {
                                if (o1.id > o2.id) 1 else -1
                            } else {
                                -1
                            }
                        }
                        o1.isEnumType() -> {
                            return if (o2.isEnumType()) {
                                if (o1.id > o2.id) 1 else -1
                            } else {
                                1
                            }
                        }
                        else -> {
                            return if (o1.id > o2.id) 1 else -1
                        }
                    }
                }
            })
            list.forEachIndexed { i, e ->
                sb.append(e.name)
                when {
                    e.isBoolType() -> {
                        sb.append(e.define?.getText(e.value))
                    }
                    e.isEnumType() -> {
                        sb.append("-").append(e.define?.getText(e.value))
                    }
                    e.isNumberType() -> {
                        sb.append(e.define?.getText(e.value))
                    }
                    else -> sb.append(":").append(e.value)
                }
                if (i < list.size - 1 && sb.isNotEmpty()) sb.append("/")
            }
            if (sb.isEmpty()) sb.append("未设置")
            return sb.toString()
        }
    }
}