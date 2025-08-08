package com.tencent.iot.explorer.link.demo.common.log

import android.util.Log

internal object L {

    private const val DEFAULT_TAG = "demo"

    var isLog = false
    var LOG_LEVEL           = 3 // 允许输出log的级别（1：输出所有日志， 6：不输出日志）
    const val LEVEL_VERBOSE = 1 // 日志输出级别 V
    const val LEVEL_INFO    = 2 // 日志输出级别 I
    const val LEVEL_DEBUG   = 3 // 日志输出级别 D
    const val LEVEL_WARN    = 4 // 日志输出级别 W
    const val LEVEL_ERROR   = 5 // 日志输出级别 E
    const val LEVEL_NONE    = 6 // 不输出日志


    fun e(msgBuilder: () -> String) {
        e({ DEFAULT_TAG }, msgBuilder)
    }

    fun e(tagBuilder: () -> String, msgBuilder: () -> String) {
        if (isLog && LEVEL_ERROR >= LOG_LEVEL) {
            Log.e(tagBuilder(), msgBuilder())
        }
    }

    fun d(msgBuilder: () -> String) {
        d({ DEFAULT_TAG }, msgBuilder)
    }

    fun d(tagBuilder: () -> String, msgBuilder: () -> String) {
        if (isLog && LEVEL_DEBUG >= LOG_LEVEL) {
            Log.d(tagBuilder(), msgBuilder())
        }
    }

    fun i(msgBuilder: () -> String) {
        i({ DEFAULT_TAG }, msgBuilder)
    }

    fun i(tagBuilder: () -> String, msgBuilder: () -> String) {
        if (isLog && LEVEL_INFO >= LOG_LEVEL) {
            Log.i(tagBuilder(), msgBuilder())
        }
    }

    inline fun <reified T> T.lv(msgBuilder: () -> String) {
        lv(tagBuilder = { T::class.simpleName ?: DEFAULT_TAG }, msgBuilder = msgBuilder)
    }

    inline fun <reified T> T.lv(tagBuilder: () -> String, msgBuilder: () -> String) {
        if (isLog && LEVEL_VERBOSE >= LOG_LEVEL) {
            Log.v(tagBuilder(), msgBuilder())
        }
    }

    inline fun <reified T> T.li(msgBuilder: () -> String) {
        li(tagBuilder = { T::class.simpleName ?: DEFAULT_TAG }, msgBuilder = msgBuilder)
    }

    inline fun <reified T> T.li(tagBuilder: () -> String, msgBuilder: () -> String) {
        if (isLog && LEVEL_INFO >= LOG_LEVEL) {
            Log.i(tagBuilder(), msgBuilder())
        }
    }

    inline fun <reified T> T.ld(msgBuilder: () -> String) {
        ld(tagBuilder = { T::class.simpleName ?: DEFAULT_TAG }, msgBuilder = msgBuilder)
    }

    inline fun <reified T> T.ld(tagBuilder: () -> String, msgBuilder: () -> String) {
        if (isLog && LEVEL_DEBUG >= LOG_LEVEL) {
            Log.d(tagBuilder(), msgBuilder())
        }
    }

    inline fun <reified T> T.lw(msgBuilder: () -> String) {
        lw(tagBuilder = { T::class.simpleName ?: DEFAULT_TAG }, msgBuilder = msgBuilder)
    }

    inline fun <reified T> T.lw(msgBuilder: () -> String, throwable: Throwable? = null) {
        lw(tagBuilder = { T::class.simpleName ?: DEFAULT_TAG }, msgBuilder = msgBuilder, throwable)
    }

    inline fun <reified T> T.lw(tagBuilder: () -> String, msgBuilder: () -> String, throwable: Throwable? = null) {
        if (isLog && LEVEL_WARN >= LOG_LEVEL) {
            throwable?.let {
                Log.w(tagBuilder(), msgBuilder())
            } ?: Log.w(tagBuilder(), msgBuilder(), throwable)
        }
    }

    inline fun <reified T> T.le(msgBuilder: () -> String) {
        le(tagBuilder = { T::class.simpleName ?: DEFAULT_TAG }, msgBuilder = msgBuilder)
    }

    inline fun <reified T> T.le(msgBuilder: () -> String, throwable: Throwable? = null) {
        le(tagBuilder = { T::class.simpleName ?: DEFAULT_TAG }, msgBuilder = msgBuilder, throwable)
    }

    inline fun <reified T> T.le(tagBuilder: () -> String, msgBuilder: () -> String, throwable: Throwable? = null) {
        if (isLog && LEVEL_ERROR >= LOG_LEVEL) {
            throwable?.let {
                Log.e(tagBuilder(), msgBuilder())
            } ?: Log.e(tagBuilder(), msgBuilder(), throwable)
        }
    }

}