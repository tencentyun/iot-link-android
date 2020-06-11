package com.tencent.iot.explorer.link.kitlink.holder

import android.content.Context
import android.view.View
import android.view.ViewGroup
import com.alibaba.fastjson.JSON
import com.tencent.iot.explorer.link.kitlink.entity.DevicePropertyEntity
import com.tencent.iot.explorer.link.kitlink.entity.TimerListEntity
import com.tencent.iot.explorer.link.kitlink.util.DataHolder
import com.view.recyclerview.CRecyclerView
import kotlinx.android.synthetic.main.item_timer_list.view.*
import java.lang.StringBuilder
import java.util.*

/**
 * 云端定时列表
 */
class TimerListViewHolder : CRecyclerView.CViewHolder<TimerListEntity> {

    private var devicePropertyList: LinkedList<DevicePropertyEntity>? = null

    constructor(context: Context, parent: ViewGroup, resId: Int) : super(context, parent, resId) {
        devicePropertyList = DataHolder.instance.get("property")
    }

    override fun show(position: Int) {
        entity?.run {
            itemView.tv_timing_project_name.text = this.TimerName
            itemView.tv_timing_project_date.text = "${parseDays()} $TimePoint"
            itemView.tv_timing_project_detail.text = parseData()
            itemView.switch_timing_project.isChecked = (Status == 1)
            itemView.timing_list_top_space.visibility =
                if (position == 0) View.VISIBLE else View.GONE
        }
        itemView.switch_timing_project.setOnClickListener {
            recyclerItemView?.doAction(this, it, position)
        }
        itemView.cl_timing_list.setOnClickListener {
            recyclerItemView?.doAction(this, it, position)
        }
        itemView.rl_delete_timing_project.setOnClickListener {
            recyclerItemView?.doAction(this, it, position)
        }
    }

    /**
     * 解析定时任务
     */
    private fun parseData(): String {
        val sb = StringBuilder()
        entity?.run {
            val list = arrayListOf<DevicePropertyEntity>()
            val data = JSON.parseObject(Data)
            data.keys.forEach {
                run inner@{
                    devicePropertyList?.forEachIndexed { _, devicePropertyEntity ->
                        if (it == devicePropertyEntity.id) {
                            devicePropertyEntity.clone().run {
                                setValue(data.getString(it))
                                list.add(this)
                                return@inner
                            }
                        }
                    }
                }
            }
            list.sortWith(object : Comparator<DevicePropertyEntity> {
                override fun compare(o1: DevicePropertyEntity, o2: DevicePropertyEntity): Int {
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
                        sb.append(e.boolEntity!!.getValueText(e.getValue()))
                    }
                    e.isEnumType() -> {
                        sb.append("-").append(e.enumEntity!!.getValueText(e.getValue()))
                    }
                    e.isNumberType() -> {
                        sb.append(e.getValueText())
                    }
                    else -> sb.append(":").append(e.getValue())
                }
                if (i < list.size - 1 && sb.isNotEmpty()) sb.append("/")
            }
        }
        if (sb.isEmpty()) sb.append("未设置")
        return sb.toString()
    }
}