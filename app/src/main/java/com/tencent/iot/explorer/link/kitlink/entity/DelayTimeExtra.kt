package com.tencent.iot.explorer.link.kitlink.entity

class DelayTimeExtra {
    var startHours = 0
    var endHours = 23
    var startMinute = 0
    var endMinute = 59
    var editAble = true
    var currentHour = 0
    var currentMinute = 1
    var pos = -1

    companion object {

        fun convertManualTask2DelayTimeExtra(manualTask: ManualTask) : DelayTimeExtra {
            var ret = DelayTimeExtra()
            if (manualTask == null) {
                return ret
            }
            ret.pos = manualTask.pos
            ret.currentHour = manualTask.hour
            ret.currentMinute = manualTask.min
            return ret
        }
    }
}