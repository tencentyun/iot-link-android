package com.tencent.iot.explorer.link.core.demo.holder

import android.content.Context
import android.view.ViewGroup
import android.widget.TimePicker
import com.tencent.iot.explorer.link.core.demo.entity.TimingProject
import kotlinx.android.synthetic.main.head_add_timing.view.*

class AddTimingHeaderHolder : BaseHolder<TimingProject> {

    var onTimeChangedListener: TimePicker.OnTimeChangedListener? = null

    constructor(context: Context, root: ViewGroup, resLayout: Int) : super(context, root, resLayout)

    override fun show(holder: BaseHolder<*>, position: Int) {
        itemView.tp_add_timer.setIs24HourView(true)
        data.run {
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
            tp_add_timer.setOnTimeChangedListener(onTimeChangedListener)
            tv_add_timer_name_title.setOnClickListener {
                clickItem(this@AddTimingHeaderHolder, it, 0)
            }
            tv_add_timer_repeat_title.setOnClickListener {
                clickItem(this@AddTimingHeaderHolder, it, 1)
            }
        }
    }
}
