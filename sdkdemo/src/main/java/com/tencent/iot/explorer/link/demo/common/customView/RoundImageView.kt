package com.tencent.iot.explorer.link.demo.common.customView

import android.content.Context
import android.graphics.*
import android.util.AttributeSet

import androidx.appcompat.widget.AppCompatImageView

/**
 * 圆角ImageView
 */
class RoundImageView : AppCompatImageView {

    private var defaultRadius = 0
    private var radius = -1

    private val rectF = RectF()
    private val raddii = floatArrayOf(0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f)
    private val clipPath = Path()

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        defaultRadius = if (width > height) height / 2 else width / 2
        if (radius == -1) {
            radius = defaultRadius
        }
        rectF.set(0f, 0f, width.toFloat(), height.toFloat())
        measurePath()
    }


    override fun onDraw(canvas: Canvas) {
        canvas.clipPath(clipPath)
        super.onDraw(canvas)
    }

    fun setRadius(radius: Int) {
        this.radius = (resources.displayMetrics.density * radius + 0.5).toInt()
        measurePath()
        invalidate()
    }

    private fun measurePath() {
        clipPath.reset()
        for (i in 0 until 8) {
            raddii[i] = radius.toFloat()
        }
        clipPath.addRoundRect(rectF, raddii, Path.Direction.CW)
    }
}
