package com.tencent.iot.explorer.link.customview.progress

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.View
import com.tencent.iot.explorer.link.R
import kotlin.math.sqrt

/**
 * 波浪进度
 */
class WaveProgress : View {

    private lateinit var cPaint: Paint
    private var cDrawable: Drawable? = null
    private var cBitmap: Bitmap? = null
    private lateinit var cPath: Path
    private var cWidth = 10f

    private lateinit var deepBluePaint: Paint
    private var deepBlueDrawable: Drawable? = null
    private var deepBlueBitmap: Bitmap? = null
    private lateinit var deepBluePath: Path

    private lateinit var bluePaint: Paint
    private var blueDrawable: Drawable? = null
    private var blueBitmap: Bitmap? = null
    private lateinit var bluePath: Path

    private var mWaveWidth = 80f
    private var mWaveWidthOffset = 0f
    private var progress = 0
    private var realProgress = 0f
    private var listener: OnIncreaseListener? = null
    private var increaseRunnable: IncreaseRunnable? = null
    private var increase = false

    private var mWaveHeight = 40f
    private var mWaveHeightOffset = 30f
    private var mCount = 2
    private var mWaveHeightDuration = 10000L


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
            val typedArray = context.obtainStyledAttributes(it, R.styleable.WaveProgress)
            for (i in 0 until typedArray.indexCount) {
                when (typedArray.getIndex(i)) {
                    R.styleable.WaveProgress_wave_progress -> {
                        progress = typedArray.getInteger(R.styleable.WaveProgress_wave_progress, 0)
                    }
                    R.styleable.WaveProgress_wave_background -> {
                        cDrawable =
                            typedArray.getDrawable(R.styleable.WaveProgress_wave_background)
                    }
                    R.styleable.WaveProgress_wave_first_background -> {
                        deepBlueDrawable =
                            typedArray.getDrawable(R.styleable.WaveProgress_wave_first_background)
                    }
                    R.styleable.WaveProgress_wave_second_background -> {
                        blueDrawable =
                            typedArray.getDrawable(R.styleable.WaveProgress_wave_second_background)
                    }
                    R.styleable.WaveProgress_wave_height -> {
                        mWaveHeight =
                            typedArray.getDimension(R.styleable.WaveProgress_wave_height, 30f)
                    }
                    R.styleable.WaveProgress_wave_width -> {
                        //不支持
//                        mWaveWidth = typedArray.getDimension(R.styleable.WaveProgress_wave_width, 130f)
                    }
                    R.styleable.WaveProgress_wave_duration -> {
                        mWaveHeightDuration =
                            typedArray.getInteger(R.styleable.WaveProgress_wave_duration, 10000)
                                .toLong()
                    }
                }
            }
            typedArray.recycle()
        }
        cPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        cPath = Path()
        deepBluePaint = Paint(Paint.ANTI_ALIAS_FLAG)
        deepBluePath = Path()
        bluePaint = Paint(Paint.ANTI_ALIAS_FLAG)
        bluePath = Path()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)
        val mWidth = when (widthMode == MeasureSpec.EXACTLY) {
            true -> widthSize
            false -> 300
        }
        val mHeight = when (heightMode == MeasureSpec.EXACTLY) {
            true -> heightSize
            false -> 300
        }
        setMeasuredDimension(mWidth, mHeight)
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        setCPaint()
        setDeepBluePaint()
        setBluePaint()
        measurePath()
        valueAnimator.start()
    }

    fun setProgress(p: Int) {
        setProgress(p, false)
    }

    fun setProgress(p: Int, increase: Boolean) {
        this.increase = increase
        if (this.increase) {
            if (increaseRunnable == null) {
                increaseRunnable = IncreaseRunnable()
            }
            increaseRunnable?.setTarget(p)
            handler?.postDelayed(increaseRunnable!!, 0)
        } else {
            progress = if (p > 100) {
                100
            } else {
                p
            }
            realProgress = progress * 10f
            measurePath()
            listener?.finish(this, progress)
        }
    }

    fun setOnIncreaseListener(listener: OnIncreaseListener) {
        this.listener = listener
    }

    override fun onDraw(canvas: Canvas?) {
        canvas?.run {
            drawWave(this)
        }
    }

    /**
     * 绘制
     */
    private fun drawWave(canvas: Canvas) {
        canvas.clipPath(cPath)
        //画背景
        canvas.drawPath(cPath, cPaint)
        //画第一层波浪
        canvas.drawPath(deepBluePath, deepBluePaint)
        //画第二层波浪
        canvas.drawPath(bluePath, bluePaint)
    }

    /**
     * 计算路径
     */
    private fun measurePath() {
        //裁圆和背景
        cPath.reset()
        cPath.addCircle(width / 2f, height / 2f, width / 2f - cWidth / 2, Path.Direction.CCW)
        //第一层
        deepBluePath.reset()
        //第二层
        bluePath.reset()
        val r = width / 2
        var h = (height * realProgress) / 1000
        val w = sqrt(width * h - h * h)
        var x = r - w
        val y = height - h
        deepBluePath.moveTo(x, y)
        bluePath.moveTo(x, y)
        if (w * 2 < 200) {
            mWaveWidth = w * 2
            deepBluePath.quadTo(x + w / 2, y - mWaveHeightOffset, x + mWaveWidth, y)
            bluePath.quadTo(x + w / 2, y + mWaveHeightOffset, x + mWaveWidth, y)
        } else {
            mWaveWidth = w * 2 / mCount
            for (i in 0..mCount) {
                x += (mWaveWidth + mWaveWidthOffset)
                if (i % 2 == 0) {
                    deepBluePath.quadTo(x - mWaveWidth / 2, y - mWaveHeightOffset, x, y)
                    bluePath.quadTo(x - mWaveWidth / 2, y + mWaveHeightOffset, x, y)
                } else {
                    deepBluePath.quadTo(x - mWaveWidth / 2, y + mWaveHeightOffset, x, y)
                    bluePath.quadTo(x - mWaveWidth / 2, y - mWaveHeightOffset, x, y)
                }
            }
        }
        deepBluePath.lineTo(width.toFloat(), y)
        deepBluePath.lineTo(width.toFloat(), height.toFloat())
        deepBluePath.lineTo(0f, height.toFloat())
        deepBluePath.lineTo(0f, y)
        deepBluePath.close()
        bluePath.lineTo(width.toFloat(), y)
        bluePath.lineTo(width.toFloat(), height.toFloat())
        bluePath.lineTo(0f, height.toFloat())
        bluePath.lineTo(0f, y)
        bluePath.close()
        invalidate()
    }

    /**
     * 设置背景
     */
    private fun setCPaint() {
        if (cDrawable == null) {
            cDrawable = ColorDrawable(Color.rgb(204, 204, 204))
        }
        if (cBitmap == null) {
            cBitmap = drawableToBitmap(cDrawable!!, width, height)
        }
        cPaint.shader =
            BitmapShader(cBitmap!!, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)
    }

    /**
     * 设置第一层曲线背景（底层）
     */
    private fun setDeepBluePaint() {
        if (deepBlueDrawable == null) {
            deepBlueDrawable = ColorDrawable(Color.rgb(8, 223, 194))
        }
        if (deepBlueBitmap == null) {
            deepBlueBitmap = drawableToBitmap(deepBlueDrawable!!, width, height)
        }
        deepBluePaint.shader =
            BitmapShader(deepBlueBitmap!!, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)
    }

    /**
     * 设置第二层曲线背景(上层)
     */
    private fun setBluePaint() {
        if (blueDrawable == null) {
            blueDrawable = ColorDrawable(Color.rgb(65, 169, 253))
        }
        if (blueBitmap == null) {
            blueBitmap = drawableToBitmap(blueDrawable!!, width, height)
        }
        bluePaint.shader = BitmapShader(blueBitmap!!, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)
    }

    /**
     * 浪高动画
     */
    private val valueAnimator = object : ValueAnimator() {
        init {
            setFloatValues(0f, mWaveHeight * 8)
            duration = mWaveHeightDuration
            repeatCount = INFINITE
            addUpdateListener {
                val offset = animatedValue.toString().toFloat()
                val dis = offset % mWaveHeight
                val count = (offset - dis) / mWaveHeight
                mWaveHeightOffset = when (count.toInt() % 4) {
                    0 -> -dis
                    1 -> dis - mWaveHeight
                    2 -> dis
                    else -> mWaveHeight - dis
                }
                measurePath()
            }
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

    inner class IncreaseRunnable : Runnable {

        private var targetProgress = 0f

        fun setTarget(p: Int) {
            targetProgress = p * 10f
        }

        override fun run() {
            if (realProgress < targetProgress && increase) {
                realProgress++
                measurePath()
                if (targetProgress == 1000f)
                    handler?.postDelayed(this, 20)
                else
                    handler?.postDelayed(this, getSpeedDelayMillis())
                progress = (realProgress / 10).toInt()
                listener?.finish(this@WaveProgress, progress)
            } else {
                measurePath()
                if (increase)
                    listener?.finish(this@WaveProgress, progress)
                handler?.removeCallbacks(this)
            }
        }

        private fun getSpeedDelayMillis(): Long {
            val p = targetProgress - progress
            return (500 - (p / 10) * 20).toLong()
        }
    }

    fun destroy() {
        listener = null
        increaseRunnable?.run {
            handler.removeCallbacks(this)
        }
    }

    interface OnIncreaseListener {
        fun finish(view: WaveProgress, progress: Int)
    }

}