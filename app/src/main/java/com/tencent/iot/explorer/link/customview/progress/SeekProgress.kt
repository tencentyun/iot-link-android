package com.tencent.iot.explorer.link.customview.progress

import android.content.Context
import android.graphics.*
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import com.tencent.iot.explorer.link.R
import kotlin.math.absoluteValue


class SeekProgress : View {

    private lateinit var seekPaint: Paint
    private var firstLinePaint: Paint? = null
    private lateinit var barPaint: Paint
    private lateinit var barShapePaint: Paint
    private lateinit var textPaint: Paint

    private var seekDrawable: Drawable? = null
    private var seekBitmap: Bitmap? = null
    private val defaultColor = -0x744201
    private var firstLineDrawable: Drawable? = null
    private var barDrawable: Drawable? = null
    private var barBitmap: Bitmap? = null
    private var barShapeColor = 0
    private var textColor = Color.BLACK
    private var showText = true

    private var lineHeight = 0
    private var shapeWidth = 0.5f
    private var barRadius = 0
    private var textSize = 0f
    private var textPadding = 0

    private var progress = 0
    private var step = 1
    private var min = 0
    private var max = 100
    private var unit = "%"
    private var text = ""

    private var barPointX = 0f
    private var barPointY = 0f
    private val bgLinePath = Path()
    private val firstLinePath = Path()

    var onProgressListener: OnProgressListener? = null

    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        init(attrs!!)
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        init(attrs!!)
    }

    /**
     * 初始化:获取xml配置数据
     */
    private fun init(attrs: AttributeSet) {
        //关闭硬件加速
        this.setLayerType(LAYER_TYPE_SOFTWARE, null)
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.SeekProgress)
        for (i in 0 until typedArray.indexCount) {
            val resId = typedArray.getIndex(i)
            when (resId) {
                R.styleable.SeekProgress_seek_progress_background -> {
                    seekDrawable = typedArray.getDrawable(resId)
                }
                R.styleable.SeekProgress_seek_first_color -> {
                    firstLineDrawable = typedArray.getDrawable(resId)
                }
                R.styleable.SeekProgress_seek_bar_color -> {
                    barDrawable = typedArray.getDrawable(resId)
                }
                R.styleable.SeekProgress_seek_bar_shape_color -> {
                    barShapeColor = typedArray.getColor(resId, Color.BLUE)
                }
                R.styleable.SeekProgress_seek_text_color -> {
                    textColor = typedArray.getColor(resId, Color.BLUE)
                }
                R.styleable.SeekProgress_seek_height -> {
                    lineHeight = typedArray.getDimension(resId, 10f).toInt()
                }
                R.styleable.SeekProgress_seek_bar_radius -> {
                    barRadius = typedArray.getDimension(resId, 10f).toInt()
                }
                R.styleable.SeekProgress_seek_bar_shape_width -> {
                    shapeWidth = typedArray.getDimension(resId, 1f)
                }
                R.styleable.SeekProgress_seek_text_padding -> {
                    textPadding = typedArray.getDimension(resId, 0f).toInt()
                }
                R.styleable.SeekProgress_seek_progress -> {
                    progress = typedArray.getInt(resId, 50)
                }
                R.styleable.SeekProgress_seek_progress_max -> {
                    max = typedArray.getInt(resId, 100)
                }
                R.styleable.SeekProgress_seek_progress_min -> {
                    min = typedArray.getInt(resId, 0)
                }
                R.styleable.SeekProgress_seek_progress_step -> {
                    step = typedArray.getInt(resId, 1)
                }
                R.styleable.SeekProgress_seek_text_size -> {
                    textSize = typedArray.getDimension(resId, sp2px(10f))
                }
                R.styleable.SeekProgress_seek_text_unit -> {
                    typedArray.getString(resId)?.let {
                        unit = it
                    }
                }
                R.styleable.SeekProgress_seek_show_text -> {
                    showText = typedArray.getBoolean(resId, true)
                }
            }
        }
        typedArray.recycle()
        if (textSize == 0f) {
            textSize = sp2px(12f)
        }
        if (lineHeight == 0) {
            lineHeight = dp2px(2)
        }
        if (barRadius == 0) {
            barRadius = lineHeight
        }
        if (progress > max) {
            progress = max
        }
        if (progress < min) {
            progress = min
        }
        if (step <= 0) {
            step = 1
        }
    }

    /**
     * 计算控件显示数据并创建绘制对象
     */
    private fun initData() {
        measurePath()
        setSeekBackgroundPaint()
        setBarPaint()
        setBarShapePaint()
        setTextPaint()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)
        val width = if (widthMode == MeasureSpec.EXACTLY) {
            widthSize
        } else {
            dp2px(260)
        }
        val height = if (heightMode == MeasureSpec.EXACTLY) {
            heightSize
        } else {
            dp2px(80)
        }
        setMeasuredDimension(width, height)
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        initData()
    }

    /**
     * 获取当前进度
     */
    fun getProgress(): Int {
        return progress
    }

    /**
     * 设置进度
     */
    fun setProgress(progress: Int) {
        when {
            progress < min -> {
                this.progress = min
            }
            progress > max -> {
                this.progress = max
            }
            (progress - this.progress).absoluteValue > step -> {
                this.progress = measureStepProgress(progress - min)
            }
            else -> {
                return
            }
        }
        measurePath()
        invalidate()
    }

    /**
     * 设置步数
     */
    fun setStepValue(step: Int) {
        this.step = step
        if (this.step <= 0) {
            this.step = 1
        }
        if (this.step > max) {
            this.step = max
        }
    }

    /**
     * 设置显示文本单位
     */
    fun setUnit(unit: String) {
        this.unit = unit
    }

    /**
     * 设置进度条显示范围
     */
    fun setRange(min: Int, max: Int) {
        this.min = min
        this.max = max
    }

    private var downX = 0f
    private var downY = 0f
    private var isBreak = false

    override fun dispatchTouchEvent(event: MotionEvent?): Boolean {
        event?.let {
            when (it.action) {
                MotionEvent.ACTION_DOWN -> {
                    downX = it.x
                    downY = it.y
                    return isValuePoint()
                }
                else -> {
                }
            }
        }
        return super.dispatchTouchEvent(event)
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        event?.let {
            downX = it.x
            downY = it.y
            when (it.action) {
                MotionEvent.ACTION_MOVE -> {
                    if (isValuePoint() && !isBreak) {
                        measureProgress()
                    }
                }
                MotionEvent.ACTION_UP -> {
                    if (isValuePoint() && !isBreak) {
                        measureProgress()
                    }
                    onProgressListener?.onProgress(this.progress, step, true)
                    isBreak = false
                }
            }
        }
        return super.onTouchEvent(event)
    }

    /**
     * 计算绘制区域和数据
     */
    private fun measurePath() {
        text = "$progress$unit"
        barPointX =
            barRadius + (width - barRadius * 2) * (progress - min) / (max - min).toFloat()
        if (barPointX > (width - barRadius)) {
            barPointX = (width - barRadius).toFloat()
        }
        val radius = lineHeight / 2f
        val startY = height / 2f
        val bLeft = paddingStart.toFloat()
        val bTop = startY - lineHeight / 2f
        val bRight = width - paddingEnd.toFloat()
        val bBottom = startY + lineHeight / 2f

        bgLinePath.reset()
        bgLinePath.addArc(
            RectF(bLeft, bTop, bLeft + radius * 2, bBottom),
            90f, 180f
        )
        bgLinePath.addRect(bLeft + radius, bTop, bRight - radius, bBottom, Path.Direction.CCW)
        bgLinePath.addArc(
            RectF(bRight - radius * 2, bTop, bRight, bBottom),
            270f, 180f
        )

        firstLinePath.reset()
        firstLinePath.addArc(
            RectF(bLeft, bTop, bLeft + radius * 2, bBottom),
            90f, 180f
        )
        firstLinePath.addRect(bLeft + radius, bTop, barPointX, bBottom, Path.Direction.CCW)

    }

    /**
     * 绘制进度条
     */
    private fun measureProgress() {
        val minDis = width / (max - min)
        val p = ((downX * (max - min)) / width).toInt()
        when {
            downX < minDis -> {
                progress = min
            }
            width - downX < minDis -> {
                progress = max
            }
            (p - progress).absoluteValue >= step -> {
                progress = measureStepProgress(p)
            }
            else -> {
                return
            }
        }
        onProgressListener?.onProgress(this.progress, step, false)
        measurePath()
        invalidate()
    }

    /**
     * 格式化当前进度为有效进度（和设置的步数有关）
     */
    private fun measureStepProgress(progress: Int): Int {
        when (step == 1) {
            true -> {
                return progress + min
            }
            false -> {
                val offsetP = progress % step
                var count = (progress - offsetP) / step
                if (offsetP >= step / 2) {
                    count++
                }
                return count * step + min
            }
        }
    }

    /**
     * 判断是否是有效触点
     */
    private fun isValuePoint(): Boolean {
        barPointY = height / 2f
        if (downY < barPointY - barRadius || downY > barPointY + barRadius) {
            isBreak = true
            return false
        }
        return true
    }

    override fun onDraw(canvas: Canvas?) {
        canvas?.let {
            barPointX =
                barRadius + (width - barRadius * 2) * (progress - min) / (max - min).toFloat()
            drawLine(it)
            drawBar(canvas)
        }
    }

    /**
     * 绘制进度条
     */
    private fun drawLine(canvas: Canvas) {
        //绘制进度条
        canvas.drawPath(bgLinePath, seekPaint)
        //绘制progress进度前半部分
        if (firstLineDrawable != null) {
            setFirstLinePaint()
            canvas.drawPath(firstLinePath, firstLinePaint!!)
        }
    }

    private fun drawBar(canvas: Canvas) {
        val ry = height / 2f
        canvas.translate(barPointX - barRadius, ry - barRadius)
        canvas.drawCircle(barRadius.toFloat(), barRadius.toFloat(), barRadius.toFloat(), barPaint)
        canvas.translate(barRadius - barPointX, barRadius - ry)
        canvas.drawCircle(barPointX, ry, barRadius.toFloat(), barShapePaint)
        if (showText) {
            val rect = Rect()
            textPaint.getTextBounds(text, 0, text.length, rect)
            var x = barPointX - rect.right / 2
            if (x < 0) x = 0f
            if (x > width - rect.right) x = width - rect.right.toFloat()
            val y = ry + barRadius + textSize + textPadding
            canvas.drawText(text, x, y, textPaint)
        }
    }

    /**
     * 转化为bitmap
     */
    private fun drawableToBitmap(drawable: Drawable, w: Int, h: Int): Bitmap {
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

    private fun setSeekBackgroundPaint() {
        seekPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        seekPaint.style = Paint.Style.FILL
        seekPaint.strokeCap = Paint.Cap.ROUND
        if (seekDrawable == null)
            seekDrawable = ColorDrawable(defaultColor)
        if (seekBitmap == null) {
            val w = measuredWidth - paddingStart - paddingEnd
            val h = lineHeight
            seekBitmap = drawableToBitmap(seekDrawable!!, w, h)
        }
        seekPaint.shader = BitmapShader(
            seekBitmap!!,
            Shader.TileMode.CLAMP,
            Shader.TileMode.CLAMP
        )
    }

    private fun setFirstLinePaint() {
        if (firstLinePaint == null)
            firstLinePaint = Paint(Paint.ANTI_ALIAS_FLAG)
        firstLineDrawable?.let {
            firstLinePaint!!.shader = BitmapShader(
                drawableToBitmap(it, barPointX.toInt() - paddingStart, lineHeight),
                Shader.TileMode.CLAMP,
                Shader.TileMode.CLAMP
            )
        }
    }

    private fun setBarPaint() {
        barPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        if (barDrawable == null) {
            barDrawable = ColorDrawable(defaultColor)
        }
        if (barBitmap == null)
            barBitmap = drawableToBitmap(barDrawable!!, barRadius * 2, barRadius * 2)
        barPaint.shader = BitmapShader(
            barBitmap!!,
            Shader.TileMode.CLAMP,
            Shader.TileMode.CLAMP
        )
        barPaint.strokeWidth = 1f
    }

    private fun setBarShapePaint() {
        barShapePaint = Paint(Paint.ANTI_ALIAS_FLAG)
        if (barShapeColor == 0)
            barShapeColor = defaultColor
        barShapePaint.color = barShapeColor
        barShapePaint.strokeWidth = shapeWidth
        barShapePaint.style = Paint.Style.STROKE
    }

    private fun setTextPaint() {
        textPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        textPaint.color = textColor
        textPaint.textSize = textSize
    }

    private fun dp2px(dp: Int): Int {
        return (context.resources.displayMetrics.density * dp + 0.5).toInt()
    }

    private fun sp2px(sp: Float): Float {
        return this.context.resources.displayMetrics.scaledDensity * sp
    }

    interface OnProgressListener {
        fun onProgress(progress: Int, step: Int, keyUp: Boolean)
    }
}