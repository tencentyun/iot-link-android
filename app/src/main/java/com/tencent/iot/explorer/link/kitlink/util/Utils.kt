package com.tencent.iot.explorer.link.kitlink.util

import android.content.Context
import android.content.Intent
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.tencent.iot.explorer.link.kitlink.consts.CommonField

class Utils {

    companion object {

        fun sendRefreshBroadcast(context: Context) {
            val intent = Intent("android.intent.action.CART_BROADCAST")
            intent.putExtra(CommonField.EXTRA_REFRESH, 1)
            LocalBroadcastManager.getInstance(context).sendBroadcast(intent)
            context.sendBroadcast(intent)
        }

        fun length(num: Float): Int {
            var len = 1
            val str = num.toString()
            val parts = str.split(".")
            if (parts != null && parts.size == 2) {
                for (i in parts[1].length - 1 downTo 1) {
                    if (parts[1][i].toString() != "0") {
                        len = i + 1
                        break
                    }
                }
            }
            if (len <= 0) {
                len = 1
            }
            return len
        }
    }
}