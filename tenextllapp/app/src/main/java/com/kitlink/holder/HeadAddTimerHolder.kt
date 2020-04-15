package com.kitlink.holder

import android.content.Context
import android.view.ViewGroup
import android.widget.TimePicker
import com.alibaba.fastjson.JSONObject
import com.kitlink.activity.WeekRepeatActivity
import com.kitlink.entity.TimerListEntity
import com.view.recyclerview.CRecyclerView
import kotlinx.android.synthetic.main.head_add_timer.view.*
import java.lang.StringBuilder

/**
 * 添加定时任务头部
 */
class HeadAddTimerHolder : CRecyclerView.HeadViewHolder<TimerListEntity> {

    constructor(context: Context, parent: ViewGroup, resId: Int) : super(context, parent, resId)

    var onTimeChangedListener: OnTimeChangedListener? = null

    override fun show() {
        itemView.tp_add_timer.setIs24HourView(true)
        entity?.run {
            TimePoint.run {
                val time = split(":")
                if (time.size == 2) {
                    itemView.tp_add_timer.currentHour = time[0].toInt()
                    itemView.tp_add_timer.currentMinute = time[1].toInt()
                }
            }

            itemView.tv_add_timer_name.text = TimerName
            itemView.tv_add_timer_repeat.text = parseDays()
        }
        itemView.run {
            tp_add_timer.setOnTimeChangedListener { view, hourOfDay, minute ->
                onTimeChangedListener?.onTimeChangedListener(view, hourOfDay, minute)
            }
            tv_add_timer_name_title.setOnClickListener {
                headListener?.doAction(this@HeadAddTimerHolder, it, 0)
            }
            tv_add_timer_repeat_title.setOnClickListener {
                headListener?.doAction(this@HeadAddTimerHolder, it, 1)
            }
        }
    }

    /**
     * 解析星期
     */
   /* private fun parseDays(): String {
        entity?.Days?.let {
            if (it == "0111110") return "工作日"
            if (it == "1000001") return "周末"
            val sb = StringBuilder()
            for (i in 0 until it.length) {
                if (it.substring(i, i + 1) == "1") {
                    when (i) {
                        0 -> sb.append("周日")
                        1 -> sb.append("周一")
                        2 -> sb.append("周二")
                        3 -> sb.append("周三")
                        4 -> sb.append("周四")
                        5 -> sb.append("周五")
                        6 -> sb.append("周六")
                    }
                }
            }
            return sb.toString()
        }
        return ""
    }*/
}

interface OnTimeChangedListener {
    fun onTimeChangedListener(view: TimePicker, hour: Int, minute: Int)
}
