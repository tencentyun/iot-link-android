package com.view.progress

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.View
import com.view.R

/**
 * 圆环进度条
 */
class RingProgressView : View {

    private var backgroundPaint: Paint? = null
    private var seekPaint: Paint? = null

    private var mBackgroundDrawable: Drawable? = null
    private var mBackgroundBitmap: Bitmap? = null
    private var mSeekDrawable: Drawable? = null
    private var mSeekBitmap: Bitmap? = null
    private var seekWidth = 0f
    private var progress = 0
    private var targetProgress = 0
    private var animation: RingProgressAnimation? = null

    private var seekPath: Path? = null
    private var seekRectF: RectF? = null

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

    private fun init(attrs: AttributeSet?) {
        if (attrs == null) return
        //关闭硬件加速
        this.setLayerType(LAYER_TYPE_SOFTWARE, null)
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.RingProgressView)
        for (i in 0 until typedArray.indexCount) {
            val resId = typedArray.getIndex(i)
            when (resId) {
                R.styleable.RingProgressView_ring_background -> {
                    mBackgroundDrawable = typedArray.getDrawable(resId)
                }
                R.styleable.RingProgressView_ring_seek_background -> {
                    mSeekDrawable = typedArray.getDrawable(resId)
                }
                R.styleable.RingProgressView_ring_progress -> {
                    progress = typedArray.getInt(resId, 0)
                }
                R.styleable.RingProgressView_ring_seek_width -> {
                    seekWidth = typedArray.getDimension(resId, 10f)
                }
            }
        }
        typedArray.recycle()
        if (mBackgroundDrawable == null) {
            mBackgroundDrawable = ColorDrawable(Color.GRAY)
        }
        if (mSeekDrawable == null) {
            mSeekDrawable = ColorDrawable(Color.BLUE)
        }
        if (seekWidth == 0f) {
            seekWidth = dp2px(5).toFloat()
        }
    }

    fun setProgress(progress: Int) {
        this.progress = progress
        invalidate()
    }

    fun setProgressWithAnim(progress: Int) {
        targetProgress = progress
        startAnimator()
    }

    private fun startAnimator() {
        if (animation == null) {
            animation = RingProgressAnimation()
        }
        animation?.setIntValues(0, targetProgress)
        animation?.start()
    }

    override fun onDraw(canvas: Canvas?) {
        canvas?.let {
            it.drawCircle(width / 2f, height / 2f, width / 2f - seekWidth / 2, getBackgroundPaint())
            it.drawPath(measurePath(), getSeekPaint())
        }
    }

    private fun getBackgroundPaint(): Paint {
        if (backgroundPaint == null) {
            backgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG)
            backgroundPaint!!.style = Paint.Style.STROKE
        }
        if (mBackgroundBitmap == null) {
            mBackgroundBitmap = drawableToBitmap(mBackgroundDrawable ?: ColorDrawable(Color.GRAY))
        }
        backgroundPaint?.shader =
            BitmapShader(mBackgroundBitmap!!, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)
        backgroundPaint?.strokeWidth = seekWidth
        return backgroundPaint!!
    }

    private fun getSeekPaint(): Paint {
        if (seekPaint == null) {
            seekPaint = Paint(Paint.ANTI_ALIAS_FLAG)
            seekPaint?.style = Paint.Style.STROKE
        }
        if (mSeekBitmap == null) {
            mSeekBitmap = drawableToBitmap(mSeekDrawable ?: ColorDrawable(Color.BLUE))
        }
        seekPaint?.strokeWidth = seekWidth
        seekPaint?.strokeCap = Paint.Cap.ROUND
        seekPaint?.shader =
            BitmapShader(mSeekBitmap!!, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)
        return seekPaint!!
    }

    private fun measurePath(): Path {
        if (seekPath == null) {
            seekPath = Path()
        }
        seekPath?.rewind()
        if (seekRectF == null) {
            seekRectF =
                RectF(seekWidth / 2, seekWidth / 2, width - seekWidth / 2, height - seekWidth / 2)
        }
        //Paint.Cap.ROUND是在前后加上半圆，所以要计算出来
        val area = (seekWidth * 180 / (Math.PI * (height - seekWidth))).toFloat()
        val startAngle = 270f + area
        var sweepAngle = (360 * progress / 100f) - area * 2
        if (progress == 0) sweepAngle = 0f
        seekPath?.addArc(seekRectF!!, startAngle, sweepAngle)
        return seekPath!!
    }

    private fun drawableToBitmap(drawable: Drawable): Bitmap {
        // 取 drawable 的长宽
        var w = width - paddingStart - paddingEnd
        val h = seekWidth.toInt()
        // 取 drawable 的颜色格式
        val config = if (drawable!!.opacity != PixelFormat.OPAQUE)
            Bitmap.Config.ARGB_8888
        else
            Bitmap.Config.RGB_565
        // 建立对应 bitmap
        val bitmap = Bitmap.createBitmap(w, h, config)
        // 建立对应 bitmap 的画布
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, w, h)
        // 把 drawable 内容画到画布中
        drawable.draw(canvas)
        return bitmap
    }

    private fun dp2px(dp: Int): Int {
        return (context.resources.displayMetrics.density * dp + 0.5).toInt()
    }

    private inner class RingProgressAnimation : ValueAnimator(),
        ValueAnimator.AnimatorUpdateListener {

        init {
            this.setIntValues(0, targetProgress)
            this.duration = 2000
            this.addUpdateListener(this)
        }

        override fun onAnimationUpdate(animation: ValueAnimator?) {
            progress = this.animatedValue.toString().toInt()
            postInvalidate()
        }

    }
}