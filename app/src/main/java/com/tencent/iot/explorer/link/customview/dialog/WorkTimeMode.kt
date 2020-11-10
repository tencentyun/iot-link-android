package com.tencent.iot.explorer.link.customview.dialog

class WorkTimeMode {
    var timeType = 0 // 0 全天  1 自定义
    var startTimeHour = 0 // 当 timeType 为 1 生效
    var startTimerMin = 0 // 同上
    var endTimeHour = 0 // 同上
    var endTimeMin = 0 // 同上
    var workDayType = 0 // 0 每天  1 工作日  2 周末  3 自定义
    var workDays = "1111111"  // 当 workDayType 为 3 时生效，7 位，位上为 1 表示档当前日期工作，左侧为开始位（第一天），第一天为周日
}