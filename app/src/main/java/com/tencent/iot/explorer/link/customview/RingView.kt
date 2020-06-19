package com.tencent.iot.explorer.link.customview

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View

/**
 * 圆环控件
 */
class RingView : View {

    private val color1 = 0x17ffffff
    private val color2 = -0x744201
    private var paint1: Paint? = null
    private var paint2: Paint? = null

    private val strokeWidth1 = 3
    private val strokeWidth2 = 4

    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)
        val width = if (widthMode == MeasureSpec.EXACTLY) {
            widthSize
        } else {
            dp2px(100)
        }
        val height = if (heightMode == MeasureSpec.EXACTLY) {
            heightSize
        } else {
            dp2px(100)
        }
        setMeasuredDimension(width, height)
    }

    override fun onDraw(canvas: Canvas?) {
        canvas?.let {
            val cx = (width / 2).toFloat()
            val cy = (height / 2).toFloat()
            it.drawCircle(cx, cy, cx - dp2px(3) + dp2px(strokeWidth1) / 2, getPaint1())
            it.drawCircle(cx, cy, cx - dp2px(7) + dp2px(strokeWidth2) / 2, getPaint2())
        }
    }

    private fun getPaint1(): Paint {
        if (paint1 == null) {
            paint1 = Paint(Paint.ANTI_ALIAS_FLAG)
            paint1?.let {
                it.color = color1
                it.strokeWidth = dp2px(strokeWidth1).toFloat()
                it.style = Paint.Style.STROKE
            }
        }
        return paint1!!
    }

    private fun getPaint2(): Paint {
        if (paint2 == null) {
            paint2 = Paint(Paint.ANTI_ALIAS_FLAG)
            paint2?.let {
                it.color = color2
                it.strokeWidth = dp2px(strokeWidth2).toFloat()
                it.style = Paint.Style.STROKE
            }
        }
        return paint2!!
    }

    private fun dp2px(dp: Int): Int {
        return (context.resources.displayMetrics.density * dp + 0.5).toInt()
    }

}