package com.tencent.iot.explorer.link.demo.core.entity

class DevVideoHistory {
    var video_list: MutableList<DevTimeBlock>? = null

    fun getTimeBlocks(): MutableList<TimeBlock> {
        video_list?.let {
            var ret: MutableList<TimeBlock> = ArrayList()
            for (i in it.indices) {
                var item = TimeBlock()
                item.StartTime = it[i].start_time
                item.EndTime = it[i].end_time
                ret.add(item)
            }
            return ret
        }
        return ArrayList()
    }
}