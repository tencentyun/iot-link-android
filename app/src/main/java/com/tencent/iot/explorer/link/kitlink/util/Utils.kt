package com.tencent.iot.explorer.link.kitlink.util

import android.text.TextUtils
import com.tencent.iot.explorer.link.core.log.L
import java.util.*


object Utils {

    fun isEmpty(src: String): Boolean {
        if (src == null || src.equals("")) {
            return true
        }

        return false
    }

    fun isDigitsOnly(src: String): Boolean {
        var flag = src.toIntOrNull()
        if (flag != null) {
            return true
        }
        return false
    }


    // 从字符传中获取第一段连续的数字
    fun getFirstSeriesNumFromStr(src: String): Int {
        if (isEmpty(src)) {
            return 0;
        }

        var start = -1
        var end = -1
        for ((i, item) in src.withIndex()) {
            if (isDigitsOnly(item.toString()) && start < 0) {
                start = i
            } else if (!isDigitsOnly(item.toString()) && start >= 0) {
                end = i
                break   // 只进行一次遍历动作
            }
        }

        var retStr = ""
        if (start < 0 && end < 0) {
            return 0
        } else if (start >= 0 && end < 0) {
            retStr = src.substring(start)
        } else {
            retStr = src.substring(start, end)
        }

        if (isDigitsOnly(retStr)) {
            return retStr.toInt()
        }

        return 0
    }

    fun getLang(): String {
        var local = Locale.getDefault().toString()
        if (TextUtils.isEmpty(local)) {
            L.d("getLang return default lang(zh-CN)")
            return "zh-CN" // 默认时返回中文类型
        }
        var tmp = local
        var eleArray = tmp.split("_")
        if (eleArray.size >= 3) {
            tmp = eleArray.get(0) + "_" + eleArray.get(1)
        }
        var ret = tmp.replace("_", "-")

        L.d("getLang return $ret")
        return ret
    }

//    @JvmStatic
//    fun main(args: Array<String>) {
//        System.out.println(getFirstSeriesNumFromStr("XX0000XX"))
//    }
}