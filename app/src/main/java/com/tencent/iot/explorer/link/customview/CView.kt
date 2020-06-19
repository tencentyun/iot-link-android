package com.tencent.iot.explorer.link.customview

import android.content.Context
import android.content.res.TypedArray
import android.graphics.*
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.View
import com.tencent.iot.explorer.link.R

abstract class CView : View {

    private var textPaint: Paint? = null

    internal var text = ""
    protected var textSize = 0f
    protected var textColor = Color.BLACK
    protected var textDrawable: Drawable? = null
    protected var textGravity = TextGravity.CENTER
    protected var textStyle = Typeface.DEFAULT

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
        if (attrs == null) return
        //关闭硬件加速
        this.setLayerType(LAYER_TYPE_SOFTWARE, null)
        val cTypedArray = context.obtainStyledAttributes(attrs, R.styleable.CView)
        for (i in 0 until cTypedArray.indexCount) {
            when (cTypedArray.getIndex(i)) {
                R.styleable.CView_c_text -> {
                    cTypedArray.getString(R.styleable.CView_c_text)?.let {
                        text = it
                    }
                }
                R.styleable.CView_c_textSize -> {
                    textSize = cTypedArray.getDimension(R.styleable.CView_c_textSize, sp2px(12))
                }
                R.styleable.CView_c_textColor -> {
                    textColor = cTypedArray.getColor(R.styleable.CView_c_textColor, Color.BLACK)
                }
                R.styleable.CView_c_textDrawable -> {
                    textDrawable = cTypedArray.getDrawable(R.styleable.CView_c_textDrawable)
                }
                R.styleable.CView_c_textStyle -> {
                    textStyle = if (cTypedArray.getInteger(R.styleable.CView_c_textStyle, 0) == 0) {
                        Typeface.DEFAULT
                    } else {
                        Typeface.DEFAULT_BOLD
                    }
                }
                R.styleable.CView_c_gravity -> {
                    textGravity = when (cTypedArray.getInteger(R.styleable.CView_c_gravity, 0)) {
                        0 -> TextGravity.CENTER
                        1 -> TextGravity.TOP
                        2 -> TextGravity.BOTTOM
                        3 -> TextGravity.LEFT
                        else -> TextGravity.RIGHT
                    }
                }
            }
        }
        if (textSize == 0f) textSize = sp2px(12)
        cTypedArray.recycle()
        getStyleable()?.let {
            val typedArray = context.obtainStyledAttributes(attrs, it)
            for (i in 0 until typedArray.indexCount) {
                init(typedArray, typedArray.getIndex(i))
            }
            typedArray.recycle()
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
            dp2px(defaultWidth())
        }
        val height = if (heightMode == MeasureSpec.EXACTLY) {
            heightSize
        } else {
            dp2px(defaultHeight())
        }
        setMeasuredDimension(width, height)
    }

    abstract fun getStyleable(): IntArray?

    abstract fun init(typedArray: TypedArray, resId: Int)

    abstract fun defaultWidth(): Int

    abstract fun defaultHeight(): Int

    fun getTextPaint(): Paint {
        if (textPaint == null) {
            textPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        }
        textPaint?.let {
            it.textSize = textSize
            if (textDrawable == null)
                it.color = textColor
            else it.shader = BitmapShader(
                drawableToBitmap(textDrawable!!, width, height),
                Shader.TileMode.CLAMP,
                Shader.TileMode.CLAMP
            )
            it.typeface = textStyle
        }
        return textPaint!!
    }

    fun drawableToBitmap(drawable: Drawable, w: Int, h: Int): Bitmap {
        // 取 drawable 的颜色格式
        val config = Bitmap.Config.ARGB_8888
        // 建立对应 bitmap
        val bitmap = Bitmap.createBitmap(w, h, config)
        // 建立对应 bitmap 的画布
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, w, h)
        // 把 drawable 内容画到画布中
        drawable.draw(canvas)
        return bitmap
    }

    fun dp2px(dp: Int): Int {
        return (context.resources.displayMetrics.density * dp + 0.5).toInt()
    }

    fun sp2px(sp: Int): Float {
        return context.resources.displayMetrics.scaledDensity * sp
    }

    enum class TextGravity {
        CENTER,
        TOP,
        BOTTOM,
        LEFT,
        RIGHT
    }

}