package com.tencent.iot.explorer.link.core.utils

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.provider.Settings
import android.text.TextUtils
import com.tencent.iot.explorer.link.core.log.L
import java.util.*

object Utils {

    private fun isDigitsOnly(src: String): Boolean {
        val flag = src.toIntOrNull()
        if (flag != null) {
            return true
        }
        return false
    }


    // 从字符串中获取第一段连续的数字
    fun getFirstSeriesNumFromStr(src: String): Int {
        if (TextUtils.isEmpty(src)) {
            return 0
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

        val retStr: String
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
        val local = Locale.getDefault().toString()
        if (TextUtils.isEmpty(local)) {
            L.d("getLang return default lang(zh-CN)")
            return "zh-CN" // 默认时返回中文类型
        }
        var tmp = local
        val eleArray = tmp.split("_")
        if (eleArray.size >= 3) {
            tmp = eleArray[0] + "_" + eleArray[1]
        }
        val ret = tmp.replace("_", "-")
        L.d("getLang return $ret")
        return ret
    }

    // 获取 url 字符串参数对应的 value
    fun getUrlParamValue(url: String, name: String?): String? {
        val paramsStr = url.substring(url.indexOf("?") + 1, url.length)
        val split: MutableMap<String, String> = hashMapOf()
        val params = paramsStr.split("&")
        for (paramKV in params) {
            val kv = paramKV.split("=")
            if (kv.size == 2) {
                split[kv[0]] = kv[1]
            }
        }
        return split[name]
    }

    interface SecondsCountDownCallback {
        fun currentSeconds(seconds: Int)
        fun countDownFinished()
    }

    fun startCountBySeconds(max: Int, secondsCountDownCallback: SecondsCountDownCallback) {
        startCountBySeconds(max, 1, secondsCountDownCallback)
    }

    // 非单例线程，允许多处使用倒计时功能
    private fun startCountBySeconds(max: Int, step: Int, secondsCountDownCallback: SecondsCountDownCallback) {
        if (max <= 0) return  // 上线为负数或者 0 的时候不进行倒计时的功能

        var countDown = 0;
        Thread {        // 倒计时线程
            if (secondsCountDownCallback != null) {
                secondsCountDownCallback.currentSeconds(max - countDown)
            }
            while(countDown < max) {
                countDown += step
                Thread.sleep(step.toLong() * 1000)
                if (secondsCountDownCallback != null) {
                    secondsCountDownCallback.currentSeconds(max - countDown)
                }
            }
            if (secondsCountDownCallback != null) {
                secondsCountDownCallback.countDownFinished()
            }
        }.start()
    }

    fun getStringValueFromXml(context: Context, xmlName: String, keyName: String): String? {
        val dataSp = context.getSharedPreferences(xmlName, Context.MODE_PRIVATE)
        return dataSp.getString(keyName, null)
    }

    fun setXmlStringValue(context: Context, xmlName: String, keyName: String, value: String) {
        val dataSp = context.getSharedPreferences(xmlName, Context.MODE_PRIVATE)
        val editor = dataSp.edit()
        if (!TextUtils.isEmpty(value)) {
            editor.putString(keyName, value)
        } else {
            editor.remove(keyName)
        }
        editor.commit()
    }

    fun clearXmlStringValue(context: Context, xmlName: String, keyName: String) {
        setXmlStringValue(context, xmlName, keyName, "")
    }

    /*
     * 复制到粘贴板
     */
    fun copy(context: Context, data: String?) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clipData = ClipData.newPlainText(null, data)
        clipboard.setPrimaryClip(clipData)
    }

    fun isChineseSystem(context: Context): Boolean {
        return context.resources.configuration.locale.language == "zh"
    }

    fun getAndroidID(context: Context): String {
        val id = Settings.System.getString(context.contentResolver, Settings.System.ANDROID_ID)
        return if (TextUtils.isEmpty(id)) ""
        else id
    }
}