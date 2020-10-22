package com.tencent.iot.explorer.link.kitlink.util

import android.content.Context
import android.os.Handler
import android.widget.TextView
import com.tencent.iot.explorer.link.R
import com.tencent.iot.explorer.link.core.utils.Utils

object AutomicUtils {

    private fun enableTextView(context: Context, textView: TextView, enable: Boolean) {
        if (textView == null) return

        if (enable) {
            textView.setTextColor(context.resources.getColor(R.color.blue_0052d9))
        } else {
            textView.setTextColor(context.resources.getColor(R.color.gray_bbbbbb))
        }
        textView.isEnabled = enable
    }

    fun automicChangeStatus(context: Context, handler: Handler, textView: TextView, seconds: Int) {
        var secondsCountDownCallback = object: Utils.SecondsCountDownCallback {
            override fun currentSeconds(seconds: Int) {
                handler.post(Runnable {
                    textView.setText(context.getString(R.string.resend) + "(${seconds}s)")
                })
            }

            override fun countDownFinished() {
                handler.post(Runnable {
                    textView.setText(context.getString(R.string.resend))
                    enableTextView(context, textView, true)
                })
            }
        }

        Utils.startCountBySeconds(seconds, secondsCountDownCallback)
        enableTextView(context, textView, false)
    }
}