package com.tencent.iot.explorer.link.demo.common.customView.timeline;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;

import com.tencent.iot.explorer.link.demo.R;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class TimeLineView extends View {
    private static final String TIME_SUFFIX = ":00";
    private static final long ONE_MINUTE = 60 * 1000;
    private int mCenterlineColor; //中间线颜色
    private float mCenterlineWidth; //中间线宽度
    private float mBottomTextMaginBottom; // 底部时间距离底部的距离
    private int mLineColor; //刻度线颜色
    private float mLineWidth; //刻度线宽度
    private int mTextColor; //文字颜色
    private int mTagTimeBlockColor; //标记时间段的背景颜色
    private int mBgColor; //视频轴的背景颜色
    private int mTextSize; //文字大小
    private Date mTime = new Date(); //当前刻度时间
    private Date mCurrentDayTime = new Date(); //记录当前的日期，用于限制滑动越界的判断，只初始化一次，后续不在初始化
    private int mHeight;
    private int mWidth;
    private float timeUnitWidth;  // 每个刻度将要在屏幕的宽度，即每十分钟界面上的展示宽度
    private int intervalMinute = 10; //每个刻度表示10分钟
    private Paint paint = new Paint();
    private List<TimeBlockInfo> timeBlockInfo = new ArrayList();
    private float mStartX = 0;
    private TimeLineViewChangeListener timeLineViewChangeListener;
    private int step = 10; //下一段/上一段的步长 单位：分钟

    public void setStep(int step) {
        this.step = step;
    }

    public int getStep() {
        return step;
    }

    public Date getCurrentDayTime() {
        return mCurrentDayTime;
    }

    public void setCurrentDayTime(Date mCurrentDayTime) {
        this.mCurrentDayTime = mCurrentDayTime;
    }

    public boolean next() {
        Date date = new Date(mTime.getTime() + step * ONE_MINUTE);
        return updateDate(date);
    }

    public boolean last() {
        Date date = new Date(mTime.getTime() - step * ONE_MINUTE);
        return updateDate(date);
    }

    public boolean updateDate(Date date) {
        boolean setFlag = setTime(date);
        if (setFlag && timeLineViewChangeListener != null) {
            timeLineViewChangeListener.onChange(date, this);
        }
        return setFlag;
    }

    public TimeLineView(Context context) {
        this(context,null);
    }

    public TimeLineView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs,-1);
    }

    public TimeLineView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.TimeLineView);
        mCenterlineColor = typedArray.getColor(R.styleable.TimeLineView_centerlineColor, Color.RED);
        mLineColor = typedArray.getColor(R.styleable.TimeLineView_lineColor, Color.BLACK);
        mTextColor = typedArray.getColor(R.styleable.TimeLineView_textColor, Color.BLACK);
        mTagTimeBlockColor = typedArray.getColor(R.styleable.TimeLineView_timeBlockColor, Color.WHITE);//默认无色
        mBgColor = typedArray.getColor(R.styleable.TimeLineView_bgColor, Color.WHITE);
        mTextSize = typedArray.getDimensionPixelSize(R.styleable.TimeLineView_textSize, 12);
        timeUnitWidth = typedArray.getDimension(R.styleable.TimeLineView_timeUnitSize, 20);
        mCenterlineWidth = typedArray.getDimension(R.styleable.TimeLineView_centerlineWidth, 1);
        mLineWidth = typedArray.getDimension(R.styleable.TimeLineView_lineWidth, 1);
        mBottomTextMaginBottom = typedArray.getDimension(R.styleable.TimeLineView_bottomTextMaginBottom, 1);
    }

    public Date getTime() {
        return mTime;
    }

    public void setTimeLineTimeDay(Date time) {
        mTime = time;
    }

    private boolean setTime(Date time) {
        if (time == null || !judgeTimeSameDay(time.getTime())) return false;

        mTime.setTime(time.getTime());
        invalidate();
        return true;
    }

    public List<TimeBlockInfo> getTimeBlockInfos() {
        return timeBlockInfo;
    }

    public void setTimeBlockInfos(List<TimeBlockInfo> timeBlockInfo) {
        this.timeBlockInfo = timeBlockInfo;
    }

    public TimeLineViewChangeListener getTimeLineViewChangeListener() {
        return timeLineViewChangeListener;
    }

    public void setTimelineChangeListener(TimeLineViewChangeListener timeLineViewChangeListener) {
        this.timeLineViewChangeListener = timeLineViewChangeListener;
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        mHeight = getHeight();
        mWidth = getWidth();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        mHeight = measureVideoLine(heightMeasureSpec);
        mWidth = measureVideoLine(widthMeasureSpec);
        setMeasuredDimension(mWidth, mHeight);
    }

    private int measureVideoLine(int measureSpec) {
        int result = 0;
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);
        switch (specMode) {
            case MeasureSpec.AT_MOST:
                result = 160;
                break;
            case MeasureSpec.EXACTLY:
                result = Math.max(specSize, 160);
                break;
        }
        return result;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                mStartX = event.getX();
                break;
            case MotionEvent.ACTION_MOVE:
            case MotionEvent.ACTION_UP:
                float distance = event.getX() - mStartX;
                long zeng = getTimeByDistance(distance);
                mStartX = event.getX();
                long time2Show = mTime.getTime() - zeng;
                if (time2Show >= getCurrentDayMaxTime()) {
                    time2Show = getCurrentDayMaxTime();
                } else if (time2Show <= getCurrentDayMinTime()) {
                    time2Show = getCurrentDayMinTime();
                }
                mTime.setTime(time2Show);
                invalidate();
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (timeLineViewChangeListener != null) {
                        timeLineViewChangeListener.onChange(mTime, this);
                    }
                }
                break;
        }
        return true;
    }

    private long getCurrentDayMinTime() {
        Date currentTimeDate = new Date(mCurrentDayTime.getTime());
        currentTimeDate.setMinutes(0);
        currentTimeDate.setHours(0);
        currentTimeDate.setSeconds(0);
        return currentTimeDate.getTime();
    }

    private long getCurrentDayMaxTime() {
        Date currentTimeDate = new Date(mCurrentDayTime.getTime());
        currentTimeDate.setMinutes(59);
        currentTimeDate.setHours(23);
        currentTimeDate.setSeconds(59);
        return currentTimeDate.getTime();
    }

    private boolean judgeTimeSameDay(long day) {
        Date nextTimeDate = new Date(day);
        if (mCurrentDayTime.getYear() != nextTimeDate.getYear() ||
                mCurrentDayTime.getMonth() != nextTimeDate.getMonth() ||
                mCurrentDayTime.getDay() != nextTimeDate.getDay()) {
            return false;
        }
        return true;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        paint.setColor(mBgColor);//首先绘制背景颜色
        canvas.drawRect(0,0, mWidth, mHeight, paint);
        paint.setColor(mTextColor);
        paint.setTextSize(mTextSize);

        paint.measureText(TIME_SUFFIX);
        int centerLineHeight = mHeight - (int)((paint.descent() - paint.ascent()) * 3 / 2);
        drawTimeBlock(canvas, centerLineHeight);
        drawLine(canvas, mTime.getTime(), centerLineHeight, true); //往左绘制
        drawLine(canvas, mTime.getTime(), centerLineHeight, false); //往右绘制

        //中间线最后绘制
        paint.setColor(mCenterlineColor);
        paint.setStrokeWidth(mCenterlineWidth);
        canvas.drawLine(mWidth / 2, 0, mWidth / 2, centerLineHeight, paint);
    }

    //绘制含视频区域
    private void drawTimeBlock(Canvas canvas, float tagHeight) {
        paint.setColor(mTagTimeBlockColor);
        for (TimeBlockInfo recordInfo : timeBlockInfo) {
            if (recordInfo.getStartTime() != null && recordInfo.getEndTime() != null) {
                // 结束时间不能早于开始时间
                if (recordInfo.getEndTime().getTime() <= recordInfo.getStartTime().getTime()) continue;
                // 如果开始时间和结束时间都不在当天，该段时间区域不绘制
                if (!judgeTimeSameDay(recordInfo.getEndTime().getTime()) &&
                        !judgeTimeSameDay(recordInfo.getStartTime().getTime())) {
                    continue;
                }

                float end, start;
                start = getXByTime(recordInfo.getStartTime());
                end = getXByTime(recordInfo.getEndTime());
                if (recordInfo.getStartTime().getTime() <= getCurrentDayMinTime()) {
                    start = getXByTime(new Date(getCurrentDayMinTime()));
                }
                if (recordInfo.getEndTime().getTime() >= getCurrentDayMaxTime()) {
                    end = getXByTime(new Date(getCurrentDayMaxTime()));
                }

                canvas.drawRect(start < 0 ? 0 : start, tagHeight / 4,
                        end > mWidth ? mWidth : end, tagHeight * 3/4, paint);
            }
        }
    }

    private void drawLine(Canvas canvas, long time, float tagHeight, boolean left){
        paint.setStrokeWidth(mLineWidth);
        paint.setColor(mLineColor);
        long residue = time % (ONE_MINUTE * intervalMinute);
        long nextTime;
        if (residue == 0) {
            nextTime = left ? time - ONE_MINUTE * intervalMinute : time + ONE_MINUTE * intervalMinute;
        } else {
            nextTime = left ? time - residue : time - residue + ONE_MINUTE * intervalMinute;
        }
        Date nextTimeDate = new Date(nextTime);
        float startX = getXByTime(nextTimeDate);
        if (startX < 0 || startX > mWidth) return;

        if (nextTime % (60 * ONE_MINUTE) == 0) {
            String toShow = String.format("%02d", nextTimeDate.getHours()) + TIME_SUFFIX;
            float textWidth = paint.measureText(toShow);
            paint.setColor(mTextColor);
            canvas.drawText(toShow,startX - textWidth / 2, mHeight - mBottomTextMaginBottom, paint);

            float timeLineHeight = tagHeight * 3/4;
            float timeStartY = (tagHeight - timeLineHeight) / 2;
            canvas.drawLine(startX, timeStartY, startX, timeStartY + timeLineHeight, paint);
        }

        if (!judgeTimeSameDay(nextTime)) return;  // 和当天不是一天的时间，界面不绘制
        canvas.drawLine(startX,tagHeight / 4, startX, tagHeight * 3/4, paint);

        drawLine(canvas, nextTime, tagHeight, left);
    }


    // 根据时间获取该时间在当前数轴的X坐标
    private float getXByTime(Date date){
        float secondsWidth = timeUnitWidth / (60 * intervalMinute);
        long seconds = (date.getTime() - mTime.getTime()) / 1000;
        float x = mWidth / 2.0f + seconds * secondsWidth;
        return x;
    }

    private int getTimeByDistance(float distance){
        float secondsWidth = timeUnitWidth / (60 * intervalMinute);
        return Math.round(distance / secondsWidth) * 1000;
    }

}
