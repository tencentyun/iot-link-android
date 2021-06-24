package com.tencent.iot.explorer.link.kitlink.entity

import java.util.*
import kotlin.collections.HashSet

class TimerExtra {
    var pos = 0
    var hours = 0
    var minute = 0
    var repeatType = 0  // 0 执行一次  1 每天  2 工作日  3 周末  4 自定义
    var workDays = "0000000"  // 当 workDayType 为 4 时生效，7 位，位上为 1 表示档当前日期工作，左侧为开始位（第一天），第一天为周日

    init {
        var date = Date()
        this.hours = date.hours
        this.minute = date.minutes
    }

    companion object {
        fun getDayType(workDays: String): Int {
            if (workDays.equals("0000000")) {  // 没有选择任何一天
                return 0
            }

            if (workDays.equals("1000001")) {
                return 3
            } else if (workDays.equals("0111110")) {
                return 2
            } else if (workDays.equals("1111111")) {
                return 1
            }
            return 4
        }

        fun convetDaySet2Days(days: Set<Integer>): String {
            var ret = 0L
            for (i in days) {
                ret = ret + (1 * Math.pow(10.0, 6 - i.toDouble())).toLong()
            }
            return String.format("%07d", ret)
        }

        fun convertDays2DaySet(days: String): Set<Int> {
            var ret = HashSet<Int>()
            for (i in 0 .. days.length - 1) {
                if (days.get(i).toString() == "1") {
                    ret.add(i)
                }
            }
            return ret
        }

        fun convertManualTask2TimerExtra(manualTask: ManualTask) : TimerExtra {
            var ret = TimerExtra()
            if (manualTask == null) {
                return ret
            }
            ret.pos = manualTask.pos
            ret.hours = manualTask.hour
            ret.minute = manualTask.min
            ret.repeatType = manualTask.workDayType
            ret.workDays = manualTask.workDays
            return ret
        }
    }
}