package com.tencent.iot.explorer.link.customview.dialog

import android.util.Log
import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.serializer.IntegerCodec

class WorkTimeMode {
    var timeType = 0 // 0 全天  1 自定义
    var startTimeHour = 0 // 当 timeType 为 1 生效
    var startTimerMin = 0 // 同上
    var endTimeHour = 23 // 同上
    var endTimeMin = 59 // 同上
    var workDayType = 0 // 0 每天  1 工作日  2 周末  3 自定义
    var workDays = "1111111"  // 当 workDayType 为 3 时生效，7 位，位上为 1 表示档当前日期工作，左侧为开始位（第一天），第一天为周日

    companion object {
        fun getCurrentTimeLongStr(workTimeMode: WorkTimeMode): String {
            return String.format("%02d:%02d-%02d:%02d", workTimeMode.startTimeHour, workTimeMode.startTimerMin, workTimeMode.endTimeHour, workTimeMode.endTimeMin)
        }

        fun ifTimeLegal(workTimeMode: WorkTimeMode): Boolean {
            if (workTimeMode.endTimeHour < workTimeMode.startTimeHour) {
                return false
            }

            if (workTimeMode.endTimeHour == workTimeMode.startTimeHour &&
                workTimeMode.endTimeMin < workTimeMode.startTimerMin) {
                return false
            }

            return true;
        }

        fun isAllDay(workTimeMode: WorkTimeMode): Boolean {
            if (workTimeMode.startTimeHour == workTimeMode.startTimerMin && workTimeMode.startTimerMin == 0
                && workTimeMode.endTimeMin == 59 && workTimeMode.endTimeHour == 23) {
                return true
            }
            return false
        }

        fun getDayType(workDays: String): Int {
            if (workDays.equals("0000000")) {  // 没有选择任何一天
                return -1
            }

            if (workDays.equals("1000001")) {
                return 2
            } else if (workDays.equals("0111110")) {
                return 1
            } else if (workDays.equals("1111111")) {
                return 0
            }
            return 3
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

    }
}