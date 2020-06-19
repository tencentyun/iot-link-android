package com.view.chart

import android.content.Context
import android.graphics.*
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.LayerDrawable
import android.text.TextUtils
import android.util.AttributeSet
import android.util.Log
import android.view.View
import com.view.R


/**
 * 柱形图
 */
class HistogramView : View {

    private var levelPaint: Paint? = null
    private var levelColor = Color.GRAY
    private var levelTextSize = 0f
    private var levelWidth = 0
    private var levelHeight = 0
    private var level = 1
    private var levelCount = 5
    private var valueHeight = 0

    private var chartPaint: Paint? = null
    private var chartWidth = 0
    private var chartHeight = 0
    private val chartRect = RectF()
    private var max = 5
    private var histogramWidth = 0f
    private var histogramGap = 0f
    private var histogramDrawable: Drawable? = null
    private var adapter: Adapter? = null

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

    fun setAdapter(adapter: Adapter) {
        this.adapter = adapter
    }


    fun setMax(max: Int) {
        this.max = max
        if (max <= 0) this.max = 1
    }

    fun setLevel(level: Int, levelCount: Int) {
        this.level = level
        this.levelCount = levelCount
        if (levelCount <= 1) this.levelCount = 2
    }

    fun notifyDataChanged() {
        invalidate()
    }

    private fun measureGap() {
        if (histogramWidth * max > chartWidth) {
            max = ((chartWidth - chartWidth % histogramWidth) / histogramWidth).toInt()
        }
        histogramGap = (chartWidth - histogramWidth * max) / max
    }

    private fun init(attrs: AttributeSet?) {
        if (attrs == null) return
        //关闭硬件加速
        this.setLayerType(LAYER_TYPE_SOFTWARE, null)
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.HistogramView)
        for (i in 0 until typedArray.indexCount) {
            val resId = typedArray.getIndex(i)
            when (resId) {
                R.styleable.HistogramView_histogram_background -> {
                    histogramDrawable = typedArray.getDrawable(resId)
                }
                R.styleable.HistogramView_histogram_width -> {
                    histogramWidth = typedArray.getDimension(resId, dp2px(25).toFloat())
                }
                R.styleable.HistogramView_histogram_gap -> {
                    histogramGap = typedArray.getDimension(resId, dp2px(25).toFloat())
                }
            }
        }
        typedArray.recycle()
        if (levelTextSize == 0f) {
            levelTextSize = sp2px(12)
        }
        if (levelWidth == 0) {
            levelWidth = dp2px(30)
        }
        if (levelHeight == 0) {
            levelHeight = dp2px(40)
        }
        if (histogramGap == 0f) {
            histogramGap = dp2px(10).toFloat()
        }
        if (histogramWidth == 0f) {
            histogramWidth = dp2px(20).toFloat()
        }
        if (histogramDrawable == null) {
            histogramDrawable = ColorDrawable(Color.BLUE)
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)
        val width = if (widthMode == MeasureSpec.EXACTLY) {
            widthSize
        } else {
            dp2px(300)
        }
        val height = if (heightMode == MeasureSpec.EXACTLY) {
            heightSize
        } else {
            dp2px(300)
        }
        setMeasuredDimension(width, height)
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        chartWidth = width - levelWidth - paddingStart - paddingEnd
        chartHeight = height - levelHeight - if (paddingTop == 0) dp2px(20) else paddingTop
        measureGap()
    }

    override fun onDraw(canvas: Canvas?) {
        canvas?.let {
            drawLevel(it)
            drawHistogram(it)
        }
    }

    /**
     *  绘制分级
     */
    private fun drawLevel(canvas: Canvas) {
        valueHeight = (chartHeight - chartHeight % (levelCount - 1)) / (levelCount - 1)
        val startY = height - levelHeight
        for (i in 0 until levelCount) {
            val y = startY.toFloat() - i * valueHeight
            canvas.drawLine(
                levelWidth.toFloat() + paddingStart, y,
                width - paddingEnd.toFloat(), y,
                getLevelPaint()
            )
            val text = if (adapter == null) "${i * level}" else adapter!!.drawY(i)
            if (TextUtils.isEmpty(text)) {
                return
            }
            val textWidth = getLevelPaint().measureText(text)
            val x = if (textWidth >= levelWidth)
                paddingStart.toFloat()
            else
                paddingStart + (levelWidth - textWidth) / 2f
            if (i == 0) {
                canvas.drawText(text, x, y, getLevelPaint())
            } else {
                canvas.drawText(
                    text,
                    x,
                    y + levelTextSize / 2,
                    getLevelPaint()
                )
            }
        }
    }

    /**
     * 不可以滑动
     */
    private fun drawHistogram(canvas: Canvas) {
        for (index in 0 until max) {
            //保存之前的状态
            canvas.save()
            val left =
                paddingStart + levelWidth + histogramGap / 2 + (histogramGap + histogramWidth) * index
            val bottom = height - levelHeight
            val value =
                if (adapter == null) (level * (index + 1)) else adapter!!.getValue(index)
            var h = (value / level.toFloat()) * valueHeight
            if (h > (levelCount - 1) * valueHeight)
                h = (levelCount - 1) * valueHeight.toFloat()
            val top = bottom - h
            if (h <= 0) h = 1f
            chartRect.set(0f, 0f, histogramWidth, h)
            canvas.translate(left, top)
            canvas.drawRect(chartRect, getChartPaint(drawableToBitmapShader(chartRect)))
            drawBottom(chartRect, index, canvas)
            canvas.save()
            canvas.translate(-left, -top)
        }
        canvas.restore()
    }

    private fun drawBottom(rect: RectF, index: Int, canvas: Canvas) {
        if (adapter == null) return
        val text = adapter!!.drawX(index)
        if (!TextUtils.isEmpty(text)) {
            val w = getLevelPaint().measureText(text)
            val x =
                if (w > histogramWidth + histogramGap) -histogramGap / 2 else rect.right / 2 - w / 2
            val y = rect.bottom + (levelHeight - levelTextSize) / 2
            canvas.drawText(text, x, y, getLevelPaint())
        }
    }

    private fun getLevelPaint(): Paint {
        if (levelPaint == null) {
            levelPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        }
        levelPaint?.textSize = levelTextSize
        levelPaint?.color = levelColor
        levelPaint?.strokeWidth = 1f
        return levelPaint!!
    }

    private fun getChartPaint(shader: BitmapShader): Paint {
        if (chartPaint == null) {
            chartPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        }
        chartPaint?.clearShadowLayer()
        chartPaint?.shader = shader
        return chartPaint!!
    }

    private fun drawableToBitmapShader(rect: RectF): BitmapShader {
        if (histogramDrawable == null) throw NullPointerException("histogramDrawable is null")
        // 取 drawable 的长宽
        val w = (rect.right - rect.left).toInt()
        val h = (rect.bottom - rect.top).toInt()
        // 取 drawable 的颜色格式
        val config = if (histogramDrawable!!.opacity != PixelFormat.OPAQUE)
            Bitmap.Config.ARGB_8888
        else
            Bitmap.Config.RGB_565
        // 建立对应 bitmap
        val bitmap = Bitmap.createBitmap(w, h, config)
        // 建立对应 bitmap 的画布
        val canvas = Canvas(bitmap)
        histogramDrawable!!.setBounds(0, 0, w, h)
        // 把 drawable 内容画到画布中
        histogramDrawable!!.draw(canvas)
        //创建BitmapShader
        return BitmapShader(bitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)
    }

    private fun dp2px(dp: Int): Int {
        return (context.resources.displayMetrics.density * dp + 0.5).toInt()
    }

    private fun sp2px(sp: Int): Float {
        return context.resources.displayMetrics.scaledDensity * sp
    }

    interface Adapter {
        fun drawY(position: Int): String
        fun drawX(position: Int): String
        fun getValue(position: Int): Int
    }

}