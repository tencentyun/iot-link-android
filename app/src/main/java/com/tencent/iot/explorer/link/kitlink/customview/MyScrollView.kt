package com.tencent.iot.explorer.link.kitlink.customview

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.ScrollView
import kotlin.math.abs

class MyScrollView(context: Context?, attrs: AttributeSet?) : ScrollView(context, attrs) {
    private var lastInterceptX = 0
    private var lastInterceptY = 0
    private var onScrollChangedListener: ScrollChangedListener? = null
    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        var intercept = false
        val x = ev.x.toInt()
        val y = ev.y.toInt()
        when (ev.action) {
            MotionEvent.ACTION_DOWN -> intercept = false
            MotionEvent.ACTION_MOVE -> {
                val deltaX = x - lastInterceptX
                val deltaY = y - lastInterceptY
                intercept = abs(deltaX) - abs(deltaY) < 0
            }
            MotionEvent.ACTION_UP -> intercept = false
        }
        lastInterceptX = x
        lastInterceptY = y
        super.onInterceptTouchEvent(ev)
        return intercept
    }

    override fun onScrollChanged(l: Int, t: Int, oldl: Int, oldt: Int) {
        super.onScrollChanged(l, t, oldl, oldt)
        if (onScrollChangedListener != null) {
            onScrollChangedListener!!.onScrollChanged(l, t, oldl, oldt)
        }
    }

    fun setScrollChangedListener(listener: ScrollChangedListener?) {
        onScrollChangedListener = listener
    }

    interface ScrollChangedListener {
        fun onScrollChanged(scrollX: Int, scrollY: Int, oldScrollX: Int, oldScrollY: Int)
    }
}