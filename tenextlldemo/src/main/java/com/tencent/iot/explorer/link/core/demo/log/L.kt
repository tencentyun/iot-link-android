package com.tenext.demo.log

import android.util.Log

internal object L {

    var isLog = false

    private const val DEFAULT_TAG = "demo"

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