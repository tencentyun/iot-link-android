package com.tencent.iot.explorer.link.kitlink.util

import android.provider.Settings
import com.tencent.iot.explorer.link.util.T

object CommonUtils {
    fun isChineseSystem(): Boolean {
        return T.getContext().resources.configuration.locale.language == "zh"
    }

    fun getAndroidID(): String {
        return Settings.System.getString(T.getContext().contentResolver, Settings.System.ANDROID_ID)
    }
}