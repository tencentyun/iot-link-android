package com.tencent.iot.explorer.link.customview

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import com.tencent.iot.explorer.link.R

class BgView : View {

    private var paint: Paint? = null
    private var bgHeight = 50f
    private var bgColor = Color.GRAY

    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        init(attrs)
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        init(attrs)
    }

    private fun init(attrs: AttributeSet?) {
        attrs?.let {
            val t = context.obtainStyledAttributes(attrs, R.styleable.BgView)
            for (i in 0.until(t.indexCount)) {
                when (t.getIndex(i)) {
                    R.styleable.BgView_bg_height -> {
                        bgHeight = t.getDimension(R.styleable.BgView_bg_height, 50f)
                    }
                    R.styleable.BgView_bg_color -> {
                        bgColor = t.getColor(R.styleable.BgView_bg_color, Color.GRAY)
                    }
                }
            }
            t.recycle()
        }
    }

    override fun onDraw(canvas: Canvas?) {
        canvas?.let {
            it.drawCircle(width / 2f, height - bgHeight, height - bgHeight, getPaint())
            it.drawRect(0f, height - bgHeight, width.toFloat(), height.toFloat(), getPaint())
        }
    }

    private fun getPaint(): Paint {
        if (paint == null) {
            paint = Paint(Paint.ANTI_ALIAS_FLAG)
        }
        paint?.color = bgColor
        return paint!!
    }

}