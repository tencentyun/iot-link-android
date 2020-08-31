package com.tencent.iot.explorer.link.kitlink.util

import android.provider.Settings
import android.text.TextUtils
import com.tencent.iot.explorer.link.util.T

object CommonUtils {
    fun isChineseSystem(): Boolean {
        return T.getContext().resources.configuration.locale.language == "zh"
    }

    fun getAndroidID(): String {
        val id = Settings.System.getString(T.getContext().contentResolver, Settings.System.ANDROID_ID)
        return if (TextUtils.isEmpty(id)) ""
        else id
    }
}