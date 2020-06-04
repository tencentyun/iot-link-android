package com.tenext.demo.entity

import com.alibaba.fastjson.JSON
import java.lang.StringBuilder

class TimingProject {

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
        }catch (e:Exception){
            e.printStackTrace()
        }
        return 0
    }

    /**
     * 解析星期
     */
    fun parseDays(): String {
        if (Days == "0111110") return "工作日"
        if (Days == "1000001") return "周末"
        if (Days == "1111111") return "每天"
        val sb = StringBuilder()
        for (i in 0 until Days.length) {
            if (Days.substring(i, i + 1) == "1") {
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

}