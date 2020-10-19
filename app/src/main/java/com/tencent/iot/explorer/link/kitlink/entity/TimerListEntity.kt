package com.tencent.iot.explorer.link.kitlink.entity

import com.alibaba.fastjson.JSON
import com.tencent.iot.explorer.link.R
import com.tencent.iot.explorer.link.T
import java.lang.StringBuilder

/**
 * 云端定时实体
 */
class TimerListEntity {

    var TimerId = ""
    var TimerName = ""
    var ProductId = ""
    var DeviceName = ""
    var Days = "0000000"
    var TimePoint = ""
    var Repeat = 0
    var Data = ""
    var Status = 0
    var CreateTime = 0L
    var UpdateTime = 0L

    /**
     * 解析  {\"brightness\": 28}
     */
    fun getValueForData(id: String): String {
        JSON.parseObject(Data)?.run {
            keys.forEach {
                if (id == it) {
                    return getString(id)
                }
            }
        }
        return ""
    }

    /**
     * 解析  {\"brightness\": 28}
     */
    fun getIntForData(id: String): Int {
        try {
            JSON.parseObject(Data)?.run {
                keys.forEach {
                    if (id == it) {
                        return getString(id).toDouble().toInt()
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return 0
    }

    /**
     * 解析星期
     */
    fun parseDays(): String {
        if (Repeat == 0) return T.getContext().getString(R.string.only_one_time)//"仅一次"
        if (Days == "0111110") return T.getContext().getString(R.string.work_day)//"工作日"
        if (Days == "1000001") return T.getContext().getString(R.string.weekend);//"周末"
        if (Days == "1111111") return T.getContext().getString(R.string.everyday)//"每天"
        val sb = StringBuilder()
        for (i in Days.indices) {
            if (Days.substring(i, i + 1) == "1") {
                when (i) {
                    0 -> sb.append(T.getContext().getString(R.string.sunday)) //周日
                    1 -> sb.append(T.getContext().getString(R.string.monday)) //周一
                    2 -> sb.append(T.getContext().getString(R.string.tuesday)) //周二
                    3 -> sb.append(T.getContext().getString(R.string.wednesday)) //周三
                    4 -> sb.append(T.getContext().getString(R.string.thursday)) //周四
                    5 -> sb.append(T.getContext().getString(R.string.friday)) //周五
                    6 -> sb.append(T.getContext().getString(R.string.saturday)) //周六
                }
            }
        }
        return sb.toString()
    }
}