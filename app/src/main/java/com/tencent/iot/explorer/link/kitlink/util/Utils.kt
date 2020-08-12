package com.tencent.iot.explorer.link.kitlink.util


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

//    @JvmStatic
//    fun main(args: Array<String>) {
//        System.out.println("XXXXXXXXXXXXXXXXXXXXXXXXXXXX")
//        System.out.println(getFirstSeriesNumFromStr("XX0000XX"))
//        System.out.println(getFirstSeriesNumFromStr("XX0000XX012xxx89xx"))
//        System.out.println(getFirstSeriesNumFromStr("XX0000XX3"))
//        System.out.println(getFirstSeriesNumFromStr("XX0000XX1"))
//        System.out.println(getFirstSeriesNumFromStr("0000XX2"))
//        System.out.println(getFirstSeriesNumFromStr("00123X"))
//        System.out.println(getFirstSeriesNumFromStr("00123"))
//        System.out.println(getFirstSeriesNumFromStr("001230X"))
//        System.out.println(getFirstSeriesNumFromStr("001230"))
//        System.out.println(getFirstSeriesNumFromStr("123"))
//        System.out.println(getFirstSeriesNumFromStr("1230"))
//        System.out.println(getFirstSeriesNumFromStr("XX001230"))
//        System.out.println(getFirstSeriesNumFromStr("XX00123"))
//        System.out.println(getFirstSeriesNumFromStr("XXXXXX"))
//        System.out.println(getFirstSeriesNumFromStr("null"))
//        System.out.println(getFirstSeriesNumFromStr("99"))
//        System.out.println(getFirstSeriesNumFromStr("99XX"))
//        System.out.println(getFirstSeriesNumFromStr("99XX123"))
//        System.out.println(getFirstSeriesNumFromStr("99XX123lal"))
//    }
}