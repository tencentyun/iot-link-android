package com.tencent.iot.explorer.link.kitlink.util

import android.content.Context
import com.tencent.iot.explorer.link.util.T
import java.util.*

object CommonUtils {
    fun isChineseSystem(): Boolean {
        return T.getContext().resources.configuration.locale == Locale.SIMPLIFIED_CHINESE
    }
}