package com.tencent.iot.explorer.link.core.demo.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.ColorInt;
import androidx.annotation.Nullable;

import com.tencent.iot.explorer.link.core.demo.R;

public class WeekView extends View {

    private String[] mWeeks = null;

    private int mTextSize;
    private int mTextColor;
    private int mWeekendTextColor;
    private Typeface mTypeface;
    private final Paint mPaint;
    private float mMeasureTextWidth;

    public WeekView(Context context) {
        this(context, null);
    }

    public WeekView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public WeekView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mWeeks = new String[] {context.getString(R.string.sunday),
                context.getString(R.string.monday), context.getString(R.string.tuesday),
                context.getString(R.string.wednesday), context.getString(R.string.thursday),
                context.getString(R.string.friday), context.getString(R.string.saturday)};

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.WeekView);
        setTextColor(a.getColor(R.styleable.WeekView_wv_textColor, Color.BLACK));
        setWeekendTextColorTextColor(a.getColor(R.styleable.WeekView_wv_weekend_textColor, Color.BLACK));
        int textSize = a.getDimensionPixelSize(R.styleable.WeekView_wv_textSize, -1);
        setTextSize(textSize);
        a.recycle();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        if(widthMode == MeasureSpec.AT_MOST){
            widthSize = (int) (mMeasureTextWidth * mWeeks.length) + getPaddingLeft() + getPaddingRight();
        }
        if(heightMode == MeasureSpec.AT_MOST){
            heightSize = (int) mMeasureTextWidth + getPaddingTop() + getPaddingBottom();
        }
        setMeasuredDimension(widthSize, heightSize);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if(mTextSize != -1){
            mPaint.setTextSize(mTextSize);
        }
        if(mTypeface != null){
            mPaint.setTypeface(mTypeface);
        }
        mPaint.setStyle(Paint.Style.FILL);

        int columnWidth = (getWidth() - getPaddingLeft() - getPaddingRight()) / 7;
        for (int i = 0; i < mWeeks.length; i++) {
            if (i != 0 && i != mWeeks.length - 1) {
                mPaint.setColor(mTextColor);
            } else {
                mPaint.setColor(mWeekendTextColor);
            }

            String text = mWeeks[i];
            int fontWidth = (int) mPaint.measureText(text);
            int startX = columnWidth * i + (columnWidth - fontWidth) / 2 + getPaddingLeft();
            int startY = (int) ((getHeight()) / 2 - (mPaint.ascent() + mPaint.descent()) / 2) + getPaddingTop();
            canvas.drawText(text, startX, startY, mPaint);
        }
    }

    public void setTextSize(int size){
        this.mTextSize = size;
        mPaint.setTextSize(mTextSize);
        mMeasureTextWidth = mPaint.measureText(mWeeks[0]);
    }

    public void setTextColor(@ColorInt int color){
        this.mTextColor = color;
    }

    public void setWeekendTextColorTextColor(@ColorInt int color){
        this.mWeekendTextColor = color;
    }

    public void setTypeface(Typeface typeface){
        this.mTypeface = typeface;
        invalidate();
    }

    public Paint getPaint(){
        return mPaint;
    }
}