package com.tencent.iot.explorer.link.core.log

import android.content.Context

object L {

    private const val DEFAULT_TAG = "TenextIoT"

    var isLog:Boolean = false

    var LOG_LEVEL     = 5 // 允许输出log的级别（1：输出所有日志， 6：不输出日志）
    var LEVEL_VERBOSE = 1 // 日志输出级别 V
    var LEVEL_DEBUG   = 2 // 日志输出级别 D
    var LEVEL_INFO    = 3 // 日志输出级别 I
    var LEVEL_WARN    = 4 // 日志输出级别 W
    var LEVEL_ERROR   = 5 // 日志输出级别 E
    var LEVEL_NONE    = 6 // 不输出日志

    fun init(context: Context) {
        TXLogImpl.init(context)
    }

    fun init(context: Context, duration: Int, logPath: String) {
        TXLogImpl.init(context, duration, logPath)
    }

    fun v(msg: String) { v(DEFAULT_TAG, msg) }
    fun v(tag: String, msg: String) {
        if (isLog && LEVEL_VERBOSE >= LOG_LEVEL) {
            TXLog.v(tag, msg)
        }
    }

    fun d(msg: String) { d(DEFAULT_TAG, msg) }
    fun d(tag: String, msg: String) {
        if (isLog && LEVEL_DEBUG >= LOG_LEVEL) {
            TXLog.d(tag, msg)
        }
    }

    fun i(msg: String) { i(DEFAULT_TAG, msg) }
    fun i(tag: String, msg: String) {
        if (isLog && LEVEL_INFO  >= LOG_LEVEL) {
            TXLog.i(tag, msg)
        }
    }

    fun w(msg: String) { i(DEFAULT_TAG, msg) }
    fun w(tag: String, msg: String) {
        if (isLog && LEVEL_WARN >= LOG_LEVEL) {
            TXLog.w(tag, msg)
        }
    }

    fun e(msg: String) { e(DEFAULT_TAG, msg) }
    fun e(tag: String, msg: String) {
        if (isLog && LEVEL_ERROR >= LOG_LEVEL) {
            TXLog.e(tag, msg)
        }
    }
}