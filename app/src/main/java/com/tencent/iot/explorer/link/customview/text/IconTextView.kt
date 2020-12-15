package com.tencent.iot.explorer.link.customview.text

import android.content.Context
import android.content.res.TypedArray
import android.graphics.*
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.util.Log
import androidx.annotation.ColorInt
import com.tencent.iot.explorer.link.customview.CView
import com.tencent.iot.explorer.link.R
import com.tencent.iot.explorer.link.core.log.L

/**
 * 带图标的TextView
 */
class IconTextView : CView {

    private var iconDrawable: Drawable? = null
    private var iconPadding = 0f
    private var iconWidth = 0f
    private var iconHeight = 0f
    private var iconGravity = 0

    private val iconRect = Rect()
    private var textStartX = 0f
    private var textStartY = 0f

    private var icon = 0
    private var iconPath = Path()
    private var iconPaint: Paint? = null
    private var iconColor = 0
    private var iconStrokeWidth = 1f
    private var iconStyle = 0
    private var iconTriangleDirection = 0

    constructor(context: Context?) : super(context)

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    fun setIconColor(@ColorInt color: Int) {
        this.iconColor = color
        invalidate()
    }

    fun setIcon(icon: Icon) {
        this.icon = icon.ordinal
        invalidate()
    }

    fun setIconDrawable(drawable: Drawable) {
        iconDrawable = drawable
        invalidate()
    }

    fun setIconPadding(padding: Float) {
        this.iconPadding = padding
    }

    fun setTriangleDirection(direction: TriangleDirection) {
        iconTriangleDirection = direction.ordinal
        invalidate()
    }

    fun setText(text: String) {
        this.text = text
        requestLayout()
        invalidate()
    }

    /**
     * @param  color
     */
    fun setTriangleDirection(direction: TriangleDirection, @ColorInt color: Int) {
        this.iconColor = color
        iconTriangleDirection = direction.ordinal
        invalidate()
    }

    override fun getStyleable(): IntArray? {
        return R.styleable.IconTextView
    }

    override fun init(typedArray: TypedArray, resId: Int) {
        when (resId) {
            R.styleable.IconTextView_icon_src -> {
                iconDrawable = typedArray.getDrawable(resId)
            }
            R.styleable.IconTextView_icon_padding -> {
                iconPadding = typedArray.getDimension(resId, 0f)
            }
            R.styleable.IconTextView_icon_gravity -> {
                iconGravity = typedArray.getInteger(resId, 0)
            }
            R.styleable.IconTextView_icon_type -> {
                icon = typedArray.getInteger(resId, 0)
            }
            R.styleable.IconTextView_icon_width -> {
                iconWidth = typedArray.getDimension(resId, dp2px(15).toFloat())
            }
            R.styleable.IconTextView_icon_height -> {
                iconHeight = typedArray.getDimension(resId, dp2px(15).toFloat())
            }
            R.styleable.IconTextView_icon_style -> {
                iconStyle = typedArray.getInteger(resId, 0)
            }
            R.styleable.IconTextView_icon_triangle_direction -> {
                iconTriangleDirection = typedArray.getInteger(resId, 0)
            }
            R.styleable.IconTextView_icon_stroke_width -> {
                iconStrokeWidth = typedArray.getDimension(resId, 1f)
            }
            R.styleable.IconTextView_icon_color -> {
                iconColor = typedArray.getColor(resId, Color.GRAY)
            }
        }
        if (iconWidth == 0f) {
            iconWidth = if (iconDrawable != null && iconDrawable!!.intrinsicWidth > 0) {
                iconDrawable!!.intrinsicWidth.toFloat()
            } else {
                dp2px(15).toFloat()
            }
        }
        if (iconHeight == 0f) {
            iconHeight = if (iconDrawable != null && iconDrawable!!.intrinsicHeight > 0) {
                iconDrawable!!.intrinsicHeight.toFloat()
            } else {
                dp2px(15).toFloat()
            }
        }
        if (iconColor == 0) iconColor = Color.GRAY
    }

    override fun defaultWidth(): Int {
        return 80
    }

    override fun defaultHeight(): Int {
        return 30
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        measureIcon()
    }

    private fun measureIcon() {
        val paint = getTextPaint()
        val rect = Rect()
        paint.getTextBounds(text, 0, text.length, rect)
        val textWidth = rect.right - rect.left
        val drawWidth = if (iconDrawable != null || icon != Icon.NONE.ordinal) {
            iconWidth + textWidth + iconPadding
        } else {
            textWidth.toFloat()
        }
        textStartY = height / 2f - (rect.top + rect.bottom) / 2f

        iconRect.top = ((height - iconHeight) / 2).toInt()
        if (iconGravity == IconGravity.LEFT.ordinal) {
            iconRect.left = (width - drawWidth.toInt()) / 2
            textStartX = iconRect.left + iconWidth + iconPadding
        } else {
            textStartX = (width - drawWidth.toInt()) / 2f
            iconRect.left = (textStartX + textWidth + iconPadding).toInt()
        }
        iconRect.right = (iconRect.left + iconWidth).toInt()
        iconRect.bottom = (iconRect.top + iconHeight).toInt()
        //绘制小图形
        if (iconDrawable == null || icon != Icon.NONE.ordinal)
            measureIconPath()
    }

    private fun measureIconPath() {
        when (icon) {
            Icon.TRiANGLE.ordinal -> {
                measureTrianglePath()
            }
            Icon.CIRCLE.ordinal -> {
                val x = (iconRect.left + iconRect.right) / 2f
                val y = (iconRect.bottom + iconRect.top) / 2f
                iconPath.addCircle(x, y, iconHeight / 2, Path.Direction.CCW)
            }
            else -> {

            }
        }
    }

    private fun measureTrianglePath() {
        val x: Float
        val y: Float
        val path = Path()
        L.e("iconTriangleDirection=$iconTriangleDirection")
        when (iconTriangleDirection) {
            TriangleDirection.TOP.ordinal -> {
                x = (iconRect.right + iconRect.left) / 2f
                y = iconRect.top.toFloat()
                path.moveTo(x, y)
                path.lineTo(iconRect.right.toFloat(), iconRect.bottom.toFloat())
                path.lineTo(iconRect.left.toFloat(), iconRect.bottom.toFloat())
            }
            TriangleDirection.BOTTOM.ordinal -> {
                x = (iconRect.right + iconRect.left) / 2f
                y = iconRect.bottom.toFloat()
                path.moveTo(x, y)
                path.lineTo(iconRect.left.toFloat(), iconRect.top.toFloat())
                path.lineTo(iconRect.right.toFloat(), iconRect.top.toFloat())
            }
            TriangleDirection.LEFT.ordinal -> {
                x = (iconRect.bottom + iconRect.top) / 2f
                y = iconRect.left.toFloat()
                path.moveTo(x, y)
                path.lineTo(iconRect.right.toFloat(), iconRect.top.toFloat())
                path.lineTo(iconRect.right.toFloat(), iconRect.bottom.toFloat())
            }
            TriangleDirection.RIGHT.ordinal -> {
                x = (iconRect.bottom + iconRect.top) / 2f
                y = iconRect.right.toFloat()
                path.moveTo(x, y)
                path.lineTo(iconRect.left.toFloat(), iconRect.bottom.toFloat())
                path.lineTo(iconRect.left.toFloat(), iconRect.top.toFloat())
            }
        }
        path.close()
        iconPath = path
    }

    override fun onDraw(canvas: Canvas?) {
        canvas?.let {
            it.drawText(text, textStartX, textStartY, getTextPaint())
            iconDrawable?.run {
                this.bounds = iconRect
                draw(it)
            }
            if (iconDrawable == null && icon != Icon.NONE.ordinal) {
                measureIcon()
                it.drawPath(iconPath, getIconPathPaint())
            }
        }
    }

    private fun getIconPathPaint(): Paint {
        if (iconPaint == null) {
            iconPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        }
        iconPaint?.color = iconColor
        iconPaint?.style = if (iconStyle == 0)
            Paint.Style.STROKE
        else
            Paint.Style.FILL
        iconPaint?.strokeWidth = iconStrokeWidth
        return iconPaint!!
    }

    enum class IconGravity {
        LEFT,
        RIGHT
    }

    enum class Icon {
        NONE,
        TRiANGLE,//三角形
        CIRCLE //圆形
    }

    enum class TriangleDirection {
        TOP,
        BOTTOM,
        LEFT,
        RIGHT
    }
}