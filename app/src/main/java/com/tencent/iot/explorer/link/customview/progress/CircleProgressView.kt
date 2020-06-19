package com.view.progress

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import com.view.R

class CircleProgressView : View {

    private var innerColor: Int = Color.RED
    private var centerColor: Int = Color.GRAY
    private var lineColor: Int = Color.BLACK
    private var outerColor: Int = Color.BLUE
    private var outerColor1: Int = Color.GRAY
    private var textColor: Int = Color.BLUE

    private var innerPaint: Paint? = null
    private var centerPaint: Paint? = null
    private var centerLinePaint: Paint? = null
    private var outerPaint: Paint? = null
    private var progressPaint: Paint? = null
    private var textPaint: Paint? = null

    private var innerWidth = 9f
    private var centerWidth = 9f
    //虚线宽度
    private var lineWidth = 1f
    private var outerWidth = 10f
    private var gapWidth = 2f
    //虚线长度
    private var dashWidth = 2f


    private var textSize = 20f
    private var progressTextSize = 40f
    private var cx = 0f
    private var cy = 0f

    //进度
    private var progress = 50
    private var rectF: RectF? = null
    private var pathEffect: DashPathEffect? = null
    private var listener: OnIncreaseListener? = null
    private var increase = false

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(attrs)
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        init(attrs)
    }

    private fun init(attrs: AttributeSet) {
        val typedArray = context.obtainStyledAttributes(attrs,
            R.styleable.CircleProgressView
        )
        for (i in 0 until typedArray.indexCount) {
            val resId = typedArray.getIndex(i)
            when (resId) {
                R.styleable.CircleProgressView_progress_text_size -> {
                    progressTextSize = typedArray.getDimension(resId, sp2px(40))
                }
                R.styleable.CircleProgressView_unit_text_size -> {
                    textSize = typedArray.getDimension(resId, sp2px(20))
                }
                R.styleable.CircleProgressView_inner_width -> {
                    innerWidth = typedArray.getDimension(resId, dp2px(10f))
                }
                R.styleable.CircleProgressView_inner_color -> {
                    innerColor = typedArray.getColor(resId, Color.RED)
                }
                R.styleable.CircleProgressView_center_width -> {
                    centerWidth = typedArray.getDimension(resId, dp2px(10f))
                }
                R.styleable.CircleProgressView_center_color -> {
                    centerColor = typedArray.getColor(resId, Color.GRAY)
                }
                R.styleable.CircleProgressView_outer_width -> {
                    outerWidth = typedArray.getDimension(resId, dp2px(10f))
                }
                R.styleable.CircleProgressView_outer_top_color -> {
                    outerColor = typedArray.getColor(resId, Color.BLUE)
                }
                R.styleable.CircleProgressView_outer_bottom_color -> {
                    outerColor1 = typedArray.getColor(resId, Color.BLUE)
                }
                R.styleable.CircleProgressView_outer_dash_width -> {
                    dashWidth = typedArray.getDimension(resId, dp2px(10f))
                }
                R.styleable.CircleProgressView_center_out_line_color -> {
                    lineColor = typedArray.getColor(resId, Color.BLACK)
                }
                R.styleable.CircleProgressView_center_out_line_width -> {
                    lineWidth = typedArray.getDimension(resId, 1f)
                }
                R.styleable.CircleProgressView_outer_and_center_gap -> {
                    gapWidth = typedArray.getDimension(resId, dp2px(2f))
                }
                R.styleable.CircleProgressView_progress -> {
                    progress = typedArray.getInteger(resId, 0)
                }
            }
        }
        typedArray.recycle()
    }

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

    fun setProgress(p: Int) {
        setProgress(p, false)
    }

    fun setProgress(p: Int, increase: Boolean) {
        this.increase = increase
        if (this.increase) {
            handler?.postDelayed(IncreaseRunnable(p), 0)
        } else {
            progress = if (p > 100) {
                100
            } else {
                p
            }
            invalidate()
            listener?.finish(this, progress)
        }
    }

    fun setOnIncreaseListener(listener: OnIncreaseListener) {
        this.listener = listener
    }

    /**
     * @param textColor
     *
     */
    fun setTextColor(textColor: Int) {
        if (resources.getColor(textColor) != this.textColor) {
            this.textColor = resources.getColor(textColor)
            invalidate()
        }
    }

    fun setInnerColor(innerColor: Int) {
        if (this.innerColor != resources.getColor(innerColor)) {
            this.innerColor = resources.getColor(innerColor)
            invalidate()
        }
    }

    override fun onDraw(canvas: Canvas?) {
        canvas?.let {
            cx = (width / 2).toFloat()
            cy = (height / 2).toFloat()
            drawInnerCircle(it)
            drawCenterCircle(it)
            drawOuterCircle(it)
            drawProgress(it)
        }
    }

    private fun drawInnerCircle(canvas: Canvas) {
        val radius =
            (width / 2).toFloat() - (gapWidth + centerWidth + outerWidth + lineWidth) - (innerWidth) / 2
        canvas.drawCircle(cx, cy, radius, getInnerPaint())
    }

    private fun drawCenterCircle(canvas: Canvas) {
        val radius =
            (width / 2).toFloat() - (lineWidth + gapWidth + outerWidth) - centerWidth / 2
        canvas.drawCircle(cx, cy, radius, getCenterPaint())
        val radius2 = (width / 2).toFloat() - (gapWidth + outerWidth) - (lineWidth) / 2
        canvas.drawCircle(cx, cy, radius2, getCenterLinePaint())
    }

    private fun drawOuterCircle(canvas: Canvas) {
        val startAngle = 270f
        if (progress < 100)
            canvas.drawArc(getRectF(), startAngle, 360 - 2f, false, getOuterPaint(false))
        val sweepAngle = progress * 360f / 100
        canvas.drawArc(getRectF(), 270f, sweepAngle, false, getOuterPaint(true))
    }

    private fun drawProgress(canvas: Canvas) {
        val rect1 = Rect()
        getProgressPaint().getTextBounds(progress.toString(), 0, progress.toString().length, rect1)
        val rect2 = Rect()
        getTextPaint().getTextBounds("%", 0, 1, rect2)
        val textLength = rect1.right + rect2.right
        val x1 = (width - textLength).toFloat() / 2
        val x2 = x1 + rect1.right
        val y = (height - rect1.top / 2).toFloat() / 2
        canvas.drawText(progress.toString(), x1, y, getProgressPaint())
        canvas.drawText("%", x2, y, getTextPaint())
    }

    private fun getInnerPaint(): Paint {
        if (innerPaint == null) {
            innerPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        }
        innerPaint?.let {
            it.style = Paint.Style.STROKE
            it.strokeWidth = innerWidth
            it.color = innerColor
        }
        return innerPaint!!
    }

    private fun getCenterPaint(): Paint {
        if (centerPaint == null) {
            centerPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        }
        centerPaint?.let {
            it.style = Paint.Style.STROKE
            it.color = centerColor
            it.strokeWidth = centerWidth
        }
        return centerPaint!!
    }

    private fun getCenterLinePaint(): Paint {
        if (centerLinePaint == null) {
            centerLinePaint = Paint(Paint.ANTI_ALIAS_FLAG)
        }
        centerLinePaint?.let {
            it.style = Paint.Style.STROKE
            it.color = lineColor
            it.strokeWidth = lineWidth
        }
        return centerLinePaint!!
    }

    private fun getOuterPaint(select: Boolean): Paint {
        if (outerPaint == null) {
            outerPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        }
        outerPaint?.let {
            it.pathEffect = getPathEffect()
            it.style = Paint.Style.STROKE
            it.strokeWidth = outerWidth
            it.color = if (select) {
                outerColor
            } else {
                outerColor1
            }
        }
        return outerPaint!!
    }

    private fun getTextPaint(): Paint {
        if (textPaint == null) {
            textPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        }
        textPaint?.let {
            it.color = textColor
            it.textSize = textSize
            it.typeface = Typeface.DEFAULT_BOLD
        }
        return textPaint!!
    }

    private fun getProgressPaint(): Paint {
        if (progressPaint == null) {
            progressPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        }
        progressPaint?.let {
            it.color = textColor
            it.textSize = progressTextSize
            it.typeface = Typeface.DEFAULT_BOLD
        }
        return progressPaint!!
    }

    private fun getPathEffect(): DashPathEffect {
        val f2 = (3.14592653 * width - dashWidth * 100).toFloat() / 99.8f
        if (pathEffect == null) {
            pathEffect =
                DashPathEffect(floatArrayOf(dashWidth, f2), 0f)
        }
        return pathEffect!!
    }

    private fun getRectF(): RectF {
        if (rectF == null) {
            rectF = RectF()
        }
        rectF?.let {
            it.left = outerWidth / 2
            it.top = it.left
            it.right = width - outerWidth / 2
            it.bottom = it.right
        }
        return rectF!!

    }

    inner class IncreaseRunnable(targetProgress: Int) : Runnable {
        private var targetProgress: Int = 0

        init {
            this.targetProgress = if (targetProgress > 100) {
                100
            } else {
                targetProgress
            }
        }

        override fun run() {
            if (progress < targetProgress && increase) {
                progress++
                invalidate()
                if (targetProgress == 100)
                    handler?.postDelayed(this, 20)
                else
                    handler?.postDelayed(this, getSpeedDelayMillis())
                listener?.finish(this@CircleProgressView, progress)
            } else {
                invalidate()
                if (increase)
                    listener?.finish(this@CircleProgressView, progress)
                handler?.removeCallbacks(this)
            }
        }

        private fun getSpeedDelayMillis(): Long {
            val p = targetProgress - progress
            return (500 - (p / 10) * 20).toLong()
        }
    }

    interface OnIncreaseListener {
        fun finish(view: CircleProgressView, progress: Int)
    }

    private fun dp2px(dp: Int): Int {
        return (context.resources.displayMetrics.density * dp + 0.5).toInt()
    }

    private fun dp2px(dp: Float): Float {
        return context.resources.displayMetrics.density * dp
    }

    private fun sp2px(sp: Int): Float {
        return this.context.resources.displayMetrics.scaledDensity * sp
    }
}