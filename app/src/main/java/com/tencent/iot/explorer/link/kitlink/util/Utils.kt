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
    }
}