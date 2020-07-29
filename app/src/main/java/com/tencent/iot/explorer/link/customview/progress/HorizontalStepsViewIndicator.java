package com.tencent.iot.explorer.link.customview.progress;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.View;

import androidx.core.content.ContextCompat;

import com.tencent.iot.explorer.link.R;
import com.tencent.iot.explorer.link.customview.progress.bean.StepBean;
import com.tencent.iot.explorer.link.util.picture.utils.BitmapUtils;

import java.util.ArrayList;
import java.util.List;

public class HorizontalStepsViewIndicator extends View {
    //定义默认的高度
    private int defaultStepIndicatorNum = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 30, getResources().getDisplayMetrics());
    private float mCompletedLineHeight; //完成线的高度
    private float mCircleRadius;    //圆的半径

    private Drawable mCompleteIcon; //完成的默认图片
    private Drawable mDefaultIcon;  //默认的背景图
    private float mCenterY; //该view的Y轴中间位置
    private float mLeftY;   //左上方的Y位置
    private float mRightY;  //右下方的位置
    private int mCircleTextSize = 0;    // 节点中字体大小

    private List<StepBean> mStepBeanList;  //当前有几部流程
    private int mStepNum = 0;
    private float mLinePadding; //两个节点之间的距离

    private List<Float> mCircleCenterPointPositionList; //定义所有圆的圆心点位置的集合
    private Paint mUnCompletedPaint;    //未完成Paint
    private Paint mCompletedPaint;  //完成paint
    private int mUnCompletedLineColor = getResources().getColor(R.color.uncomplete_progress); //定义默认未完成线的颜色
    private int mCompletedLineColor = getResources().getColor(R.color.complete_progress);  //定义默认完成线的颜色
    private int mComplectingPosition; //正在进行的位置

    private OnDrawIndicatorListener mOnDrawListener;
    private int screenWidth; //屏幕宽度

    public int getCurrentStep() {
        return mComplectingPosition;
    }

    public void setCurrentStep(int currentStep) {
        mComplectingPosition = currentStep;
    }

    public void setOnDrawListener(OnDrawIndicatorListener onDrawListener) {
        mOnDrawListener = onDrawListener;
    }

    // 圆的半径
    public float getCircleRadius() {
        return mCircleRadius;
    }

    public HorizontalStepsViewIndicator(Context context) {
        this(context, null);
    }

    public HorizontalStepsViewIndicator(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public HorizontalStepsViewIndicator(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private Paint createPaint(int color) {
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setColor(color);
        paint.setStrokeWidth(2);
        paint.setStyle(Paint.Style.FILL_AND_STROKE);
        return paint;
    }

    private void init() {
        mStepBeanList = new ArrayList<>();
        mCircleCenterPointPositionList = new ArrayList<>();//初始化

        mUnCompletedPaint = createPaint(mUnCompletedLineColor);
        mCompletedPaint = createPaint(mCompletedLineColor);

        mCompletedLineHeight = 0.12f * defaultStepIndicatorNum; //已经完成线的宽高
        mCircleRadius = 0.4f * defaultStepIndicatorNum;    //圆的半径
        mCompleteIcon = ContextCompat.getDrawable(getContext(), R.drawable.circle_shape);   //已经完成的icon
        mDefaultIcon = ContextCompat.getDrawable(getContext(), R.drawable.circle_uncomplete_shape); //未完成的icon
    }

    @Override
    protected synchronized void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (MeasureSpec.UNSPECIFIED != MeasureSpec.getMode(widthMeasureSpec)) {
            screenWidth = MeasureSpec.getSize(widthMeasureSpec);
            mLinePadding = (screenWidth - ((mStepNum + 3) * mCircleRadius * 2)) / (mStepNum - 1);
        }
        int height = defaultStepIndicatorNum;
        if (MeasureSpec.UNSPECIFIED != MeasureSpec.getMode(heightMeasureSpec)) {
            height = Math.min(height, MeasureSpec.getSize(heightMeasureSpec));
        }
        int width = (int) (mStepNum * mCircleRadius * 2 - (mStepNum - 1) * mLinePadding);
        setMeasuredDimension(width, height);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        //获取中间的高度,目的是为了让该view绘制的线和圆在该view垂直居中
        mCenterY = 0.5f * getHeight();

        mLeftY = mCenterY - mCompletedLineHeight / 2; //获取左上方Y的位置，获取该点的意义是为了方便画矩形左上的Y位置
        mRightY = mCenterY + mCompletedLineHeight / 2;  //获取右下方Y的位置，获取该点的意义是为了方便画矩形右下的Y位置
        mCircleCenterPointPositionList.clear();
        for (int i = 0; i < mStepNum; i++) {
            //先计算全部最左边的padding值（getWidth()-（圆形直径+两圆之间距离）*2）
            float paddingLeft = (screenWidth - mStepNum * mCircleRadius * 2 - (mStepNum - 1) * mLinePadding) / 2;
            mCircleCenterPointPositionList.add(paddingLeft + mCircleRadius + i * mCircleRadius * 2 + i * mLinePadding);
        }

        if (mOnDrawListener != null) {
            mOnDrawListener.ondrawIndicator();
        }
    }

    @Override
    protected synchronized void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mOnDrawListener != null) {
            mOnDrawListener.ondrawIndicator();
        }

        // 绘制进度条的实线部分
        for (int i = 0; i < mCircleCenterPointPositionList.size() - 1; i++) {
            final float preComplectedXPosition = mCircleCenterPointPositionList.get(i); //前一个ComplectedXPosition
            final float afterComplectedXPosition = mCircleCenterPointPositionList.get(i + 1);   //后一个ComplectedXPosition

            RectF line = new RectF(preComplectedXPosition + mCircleRadius - 10, mLeftY,
                    afterComplectedXPosition - mCircleRadius + 10, mRightY);
            //判断在完成之前的所有点
            if (i < mComplectingPosition - 1) {
                canvas.drawRect(line, mCompletedPaint);
            } else {
                canvas.drawRect(line, mUnCompletedPaint);
            }
        }

        // 绘制节点
        for (int i = 0; i < mCircleCenterPointPositionList.size(); i++) {
            final float currentComplectedXPosition = mCircleCenterPointPositionList.get(i);
            Rect rect = new Rect((int) (currentComplectedXPosition - mCircleRadius), (int) (mCenterY - mCircleRadius),
                    (int) (currentComplectedXPosition + mCircleRadius), (int) (mCenterY + mCircleRadius));
            if (i == 0) {
                mCircleTextSize = getMostFitTextSize(String.valueOf(i), (int)mCircleRadius * 2);
            }

            Bitmap bitmap;
            if (i >= mComplectingPosition) {
                bitmap = BitmapUtils.createWaterMark(mDefaultIcon, String.valueOf(i + 1), (int)mCircleRadius * 2, mCircleTextSize);
            }  else {
                bitmap = BitmapUtils.createWaterMark(mCompleteIcon, String.valueOf(i + 1), (int)mCircleRadius * 2, mCircleTextSize);
            }
            Drawable showDrawable = new BitmapDrawable(bitmap);
            showDrawable.setBounds(rect);
            showDrawable.draw(canvas);
        }
    }

    private int getMostFitTextSize(String txt, int targetWidth) {
        int textSize = 5;   // 初始估计值
        int step = 5;       // 估计值测试使用的步长
        int checkTargetWidth = targetWidth - 20;    // 修正临界宽度，避免刚好填充整个圈
        return getTextSize(txt, checkTargetWidth, textSize, step);
    }

    private int getTextSize(String txt, int targetWidth, int currentSize, int step) {
        Paint p = new Paint();
        p.setTextSize(currentSize);
        float len = p.measureText(txt);
        float hei = Math.abs(p.ascent() + p.descent());
        if (len < targetWidth && hei < targetWidth) {
            currentSize += step;
            return getTextSize(txt, targetWidth, currentSize, step);
        } else {
            return currentSize - step;
        }
    }

    // 得到所有圆点所在的位置
    public List<Float> getCircleCenterPointPositionList() {
        return mCircleCenterPointPositionList;
    }

    // 设置流程步数
    public void setStepNum(List<StepBean> stepsBeanList) {
        this.mStepBeanList = stepsBeanList;
        mStepNum = mStepBeanList.size();
        requestLayout();
    }

    // 设置未完成线的颜色
    public void setUnCompletedLineColor(int unCompletedLineColor) {
        this.mUnCompletedLineColor = unCompletedLineColor;
    }

    // 设置已完成线的颜色
    public void setCompletedLineColor(int completedLineColor) {
        this.mCompletedLineColor = completedLineColor;
    }

    // 设置默认图片
    public void setDefaultIcon(Drawable defaultIcon) {
        this.mDefaultIcon = defaultIcon;
    }

    // 设置已完成图片
    public void setCompleteIcon(Drawable completeIcon) {
        this.mCompleteIcon = completeIcon;
    }

    // 设置对view监听
    public interface OnDrawIndicatorListener {
        void ondrawIndicator();
    }
}