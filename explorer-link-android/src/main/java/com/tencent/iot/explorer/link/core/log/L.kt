package com.tencent.iot.explorer.link.core.log

import android.util.Log

object L {

    var isLog:Boolean = false

    private const val DEFAULT_TAG = "TenextIoT"

    fun e(msg: String) {
        e(DEFAULT_TAG, msg)
    }

    fun e(tag: String, msg: String) {
        if (isLog) {
            Log.e(tag, msg)
        }
    }

    fun d(msg: String) {
        d(DEFAULT_TAG, msg)
    }

    fun d(tag: String, msg: String) {
        if (isLog) {
            Log.d(tag, msg)
        }
    }

    fun i(msg: String) {
        i(DEFAULT_TAG, msg)
    }

    fun i(tag: String, msg: String) {
        if (isLog) {
            Log.i(tag, msg)
        }
    }

}