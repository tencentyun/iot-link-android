package com.tencent.iot.explorer.link.customview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.widget.ProgressBar;

import com.tencent.iot.explorer.link.R;

public class HorizontalProgressBarWithNumber extends ProgressBar {

    private int DEFAULT_TEXT_SIZE = 12;
    private int DEFAULT_TEXT_COLOR = 0XFFFFFFFF;
    private int DEFAULT_COLOR_UNREACHED_COLOR = 0xFFE1E4E9;
    private int DEFAULT_HEIGHT_REACHED_PROGRESS_BAR = 4;
    private int DEFAULT_HEIGHT_UNREACHED_PROGRESS_BAR = 4;
    private int DEFAULT_CIRCLE_COLOR = 0XFFFFFFFF;
    private int DEFAULT_COLOR_REACHED_COLOR = 0XFFFFFFFF;

    public void setUnReachedColor(int color) {
        DEFAULT_COLOR_UNREACHED_COLOR = color;
        mUnReachedBarColor = DEFAULT_COLOR_UNREACHED_COLOR;
    }

    public void setCircleTextColor(int color) {
        DEFAULT_TEXT_COLOR = color;
        mTextColor = DEFAULT_TEXT_COLOR;
    }

    public void setReachedBarColor(int color) {
        mReachedBarColor = color;
    }

    public void setCircleColor(int color) {
        DEFAULT_CIRCLE_COLOR = color;
        mCircleColor = DEFAULT_CIRCLE_COLOR;
    }

    protected Paint mPaint = new Paint();
    // 字体颜色
    protected int mTextColor = DEFAULT_TEXT_COLOR;
    // 字体大小
    protected int mTextSize = sp2px(DEFAULT_TEXT_SIZE);
    // 覆盖进度高度
    protected int mReachedProgressBarHeight = dp2px(DEFAULT_HEIGHT_REACHED_PROGRESS_BAR);
    // 覆盖进度颜色
    protected int mReachedBarColor = DEFAULT_TEXT_COLOR;
    // 未覆盖进度高度
    protected int mUnReachedProgressBarHeight = dp2px(DEFAULT_HEIGHT_UNREACHED_PROGRESS_BAR);
    // 未覆盖进度颜色
    protected int mUnReachedBarColor = DEFAULT_COLOR_UNREACHED_COLOR;
    // 圆的颜色
    protected int mCircleColor = DEFAULT_CIRCLE_COLOR;

    protected int mRealWidth;

    protected boolean mIfDrawText = true;
    protected boolean mIfDrawCircle = true;

    protected static final int VISIBLE = 0;

    private String startTxt = "0";
    private String endTxt = "0";

    public void setStartTxt(String startTxt) {
        this.startTxt = startTxt;
    }

    public HorizontalProgressBarWithNumber(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public HorizontalProgressBarWithNumber(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        obtainStyledAttributes(attrs);
        mPaint.setTextSize(mTextSize);
        mPaint.setColor(mTextColor);
        mPaint.setAntiAlias(true);
    }

    private void obtainStyledAttributes(AttributeSet attrs) {
        // 获取自定义属性
        final TypedArray attributes = getContext().obtainStyledAttributes(attrs, R.styleable.HorizontalProgressBarWithNumber);
        mTextColor = attributes.getColor(R.styleable.HorizontalProgressBarWithNumber_progress_text_color, DEFAULT_TEXT_COLOR);
        mTextSize = (int) attributes.getDimension(R.styleable.HorizontalProgressBarWithNumber_progress_bar_text_size, mTextSize);
        mCircleColor = attributes.getColor(R.styleable.HorizontalProgressBarWithNumber_progress_circle_color, DEFAULT_CIRCLE_COLOR);
        mReachedBarColor = attributes.getColor(R.styleable.HorizontalProgressBarWithNumber_progress_reached_color, mTextColor);
        mUnReachedBarColor = attributes.getColor(R.styleable.HorizontalProgressBarWithNumber_progress_unreached_color, DEFAULT_COLOR_UNREACHED_COLOR);
        mReachedProgressBarHeight = (int) attributes.getDimension(R.styleable.HorizontalProgressBarWithNumber_progress_reached_bar_height, mReachedProgressBarHeight);
        mUnReachedProgressBarHeight = (int) attributes.getDimension(R.styleable.HorizontalProgressBarWithNumber_progress_unreached_bar_height, mUnReachedProgressBarHeight);
        int textVisible = attributes.getInt(R.styleable.HorizontalProgressBarWithNumber_progress_text_visibility, VISIBLE);
        if (textVisible != VISIBLE) {
            mIfDrawText = false;
        }
        attributes.recycle();
        int left = (int) (mReachedProgressBarHeight * 0.8), right = (int) (mReachedProgressBarHeight * 0.8);
        int top = (int) (mReachedProgressBarHeight * 0.3 + dp2px(1)), bottom = (int) (mReachedProgressBarHeight * 0.3 + dp2px(1));
        setPadding(left, top, right, bottom);
    }

    @Override
    protected synchronized void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = measureHeight(heightMeasureSpec);
        setMeasuredDimension(width, height);
        mRealWidth = getMeasuredWidth() - getPaddingRight() - getPaddingLeft();
    }

    private int measureHeight(int measureSpec) {
        int result;
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);
        if (specMode == MeasureSpec.EXACTLY) {
            result = specSize;
        } else {
            float textHeight = (mPaint.descent() - mPaint.ascent()) * 3;
            result = (int) (getPaddingTop() + getPaddingBottom() + Math.max(
                    Math.max(mReachedProgressBarHeight, mUnReachedProgressBarHeight), Math.abs(textHeight)));
            if (specMode == MeasureSpec.AT_MOST) {
                result = Math.min(result, specSize);
            }
        }
        return result;
    }


    @Override
    protected synchronized void onDraw(Canvas canvas) {
        canvas.save();
        canvas.translate(getPaddingLeft(), getHeight() / 2);
        endTxt = String.valueOf(this.getMax());

        boolean noNeedBg = false;
        float radio = getProgress() * 1.0f / getMax();
        float progressPosX = (int) (mRealWidth * radio);
        String text = getProgress() + "";

        float textWidth = mPaint.measureText(text);
        float endTextWidth = mPaint.measureText(endTxt);
        float textHeight = (mPaint.descent() + mPaint.ascent());

        float radius = mReachedProgressBarHeight * 3.5F;

        // 覆盖的进度
        float endX = progressPosX;
        if (endX > -1) {
            mPaint.setColor(mReachedBarColor);
            RectF rectF = new RectF(0, 0 - getPaddingTop() - getPaddingBottom(),
                    endX, mReachedProgressBarHeight - getPaddingBottom());
            canvas.drawRoundRect(rectF, 25, 25, mPaint);
        }

        // 未覆盖的进度
        if (!noNeedBg) {
            float start = progressPosX;
            mPaint.setColor(mUnReachedBarColor);
            RectF rectF = new RectF(start, 0 - getPaddingTop() - getPaddingBottom(),
                    mRealWidth + getPaddingRight() - radius, mReachedProgressBarHeight - getPaddingBottom());
            canvas.drawRoundRect(rectF, 25, 25, mPaint);
        }


        float centerY = ((mReachedProgressBarHeight - getPaddingBottom()) - (0 - getPaddingTop() - getPaddingBottom())) / 2;

        float completeProgressPosX = progressPosX;
        if (completeProgressPosX < radius) {
            completeProgressPosX = radius;
        } else if (completeProgressPosX > mRealWidth + getPaddingRight() - radius) {
            completeProgressPosX = mRealWidth + getPaddingRight() - radius;
        }
        // 圆
        if (mIfDrawCircle) {
            mPaint.setColor(mUnReachedBarColor);
            canvas.drawCircle(completeProgressPosX, -centerY, radius + 3, mPaint);
            mPaint.setColor(mCircleColor);
            canvas.drawCircle(completeProgressPosX, -centerY, radius, mPaint);
        }

        // 文本
        if (mIfDrawText) {
            mPaint.setColor(mTextColor);
            canvas.drawText(text, completeProgressPosX - textWidth / 2, - centerY - textHeight / 2, mPaint);
        }

        canvas.restore();

    }

    /**
     * dp 2 px
     */
    protected int dp2px(int dpVal) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dpVal, getResources().getDisplayMetrics());
    }

    /**
     * sp 2 px
     */
    protected int sp2px(int spVal) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, spVal, getResources().getDisplayMetrics());
    }

}