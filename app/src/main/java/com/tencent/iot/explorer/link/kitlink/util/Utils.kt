package com.tencent.iot.explorer.link.kitlink.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.os.Build
import android.text.TextUtils
import com.tencent.iot.explorer.link.core.log.L
import java.io.ByteArrayOutputStream
import java.io.IOException
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

    // 获取 url 字符串参数对应的 value
    fun getUrlParamValue(url: String, name: String?): String? {
        val paramsStr = url.substring(url.indexOf("?") + 1, url.length)
        val split: MutableMap<String, String> = hashMapOf()
        var params = paramsStr.split("&")
        for (paramKV in params) {
            var kv = paramKV.split("=")
            if (kv.size == 2) {
                split[kv.get(0)] = kv.get(1)
            }
        }
        return split.get(name)
    }

    interface SecondsCountDownCallback {
        fun currentSeconds(seconds: Int)
        fun countDownFinished()
    }

    fun startCountBySeconds(max: Int, secondsCountDownCallback: SecondsCountDownCallback) {
        startCountBySeconds(max, 1, secondsCountDownCallback)
    }

    // 非单例线程，允许多处使用倒计时功能
    fun startCountBySeconds(max: Int, step: Int, secondsCountDownCallback: SecondsCountDownCallback) {
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

    fun bmpToByteArray(bitmap: Bitmap?): ByteArray? {

        // 要返回的字符串
        var reslut: ByteArray? = null
        var baos: ByteArrayOutputStream? = null
        try {
            if (bitmap != null) {
                baos = ByteArrayOutputStream()
                /**
                 * 压缩只对保存有效果bitmap还是原来的大小
                 */
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
                baos.flush()
                baos.close()
                // 转换为字节数组
                reslut = baos.toByteArray()
            } else {
                return null
            }
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            try {
                if (baos != null) {
                    baos.close()
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        return reslut
    }

    fun getBitmap(context: Context, vectorDrawableId: Int): Bitmap? {
        var bitmap: Bitmap? = null
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
            val vectorDrawable = context.getDrawable(vectorDrawableId)
            bitmap = Bitmap.createBitmap(
                vectorDrawable!!.intrinsicWidth,
                vectorDrawable.intrinsicHeight, Bitmap.Config.ARGB_8888
            )
            val canvas = Canvas(bitmap)
            vectorDrawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight())
            vectorDrawable.draw(canvas)
        } else {
            bitmap = BitmapFactory.decodeResource(context.resources, vectorDrawableId)
        }
        return bitmap
    }
//    @JvmStatic
//    fun main(args: Array<String>) {
//    }
}