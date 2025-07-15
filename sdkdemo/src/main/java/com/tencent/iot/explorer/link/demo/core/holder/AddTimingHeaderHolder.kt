package com.tencent.iot.explorer.link.demo.core.holder

import android.widget.TimePicker
import com.tencent.iot.explorer.link.demo.core.entity.TimingProject
import com.tencent.iot.explorer.link.demo.databinding.HeadAddTimingBinding

class AddTimingHeaderHolder(binding: HeadAddTimingBinding) : BaseHolder<TimingProject, HeadAddTimingBinding>(binding) {

    var onTimeChangedListener: TimePicker.OnTimeChangedListener? = null

    override fun show(holder: BaseHolder<*, *>, position: Int) {
        binding.tpAddTimer.setIs24HourView(true)
        data.run {
            TimePoint.run {
                val time = split(":")
                if (time.size == 2) {
                    binding.tpAddTimer.currentHour = time[0].toInt()
                    binding.tpAddTimer.currentMinute = time[1].toInt()
                }
            }

            binding.tvAddTimerName.text = TimerName
            binding.tvAddTimerRepeat.text = parseDays()
        }

        itemView.run {
            binding.tpAddTimer.setOnTimeChangedListener(onTimeChangedListener)
            binding.tvAddTimerNameTitle.setOnClickListener {
                clickItem(this@AddTimingHeaderHolder, it, 0)
            }
            binding.tvAddTimerRepeatTitle.setOnClickListener {
                clickItem(this@AddTimingHeaderHolder, it, 1)
            }
        }
    }
}
