package com.tencent.iot.explorer.link.demo.common.customView;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.tencent.iot.explorer.link.demo.R;
import com.tencent.iot.explorer.link.demo.common.util.DateUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class CalendarView extends View {

    public static final String DATE_FORMAT_PATTERN = "yyyyMMdd"; /** 默认的日期格式化格式 */
    public static final String SECOND_DATE_FORMAT_PATTERN = "yyyy-MM-dd";
    private int mTextColor; //默认文字颜色
    private int mSelectTextColor; //选中后文字颜色
    private int mCurrentDayTextColor; //当天文字的颜色
    private int mTagTextColor; //被标记文字的颜色
    private float mTextSize; // 阳历字体大小
    private float mBottomTextSize; // 农历字体大小
    private Drawable mDayBackground; //默认天的背景
    private Drawable mSelectDayBackground; //选中后天的背景
    private String mDateFormatPattern; //日期格式化格式
    private String mTodayTag;
    private Typeface mTypeface; //字体
    private int mColumnWidth; //每列宽度
    private int mRowHeight; //每行高度
    private List<String> mSelectDate; //已标记的日期数据
    private List<String> mCheckedDate = new CopyOnWriteArrayList<>(); //已标记的日期数据
    private int[][] mDays = new int[6][7]; //存储对应列行处的天

    private OnDataClickListener  mOnDataClickListener;
    private OnDateChangeListener mChangeListener;
    private SimpleDateFormat mDateFormat;
    private Calendar mSelectCalendar;
    private Calendar mCalendar;
    private Paint mPaint;
    private int mSlop;
    private int mDownX = 0, mDownY = 0;

    public interface OnDataClickListener{

        /**
         * 日期点击监听.
         * @param view     与次监听器相关联的 View.
         * @param year     对应的年.
         * @param month    对应的月.
         * @param day      对应的日.
         */
        boolean onDataClick(@NonNull CalendarView view, int year, int month, int day);
    }

    public interface OnDateChangeListener {

        /**
         * 选中的天发生了改变监听回调, 改变有 2 种, 分别是选中和取消选中.
         * @param view     与次监听器相关联的 View.
         * @param select   true 表示是选中改变, false 是取消改变.
         * @param year     对应的年.
         * @param month    对应的月.
         * @param day      对应的日.
         */
        void onSelectedDayChange(@NonNull CalendarView view, boolean select, int year, int month, int day);
    }

    public CalendarView(Context context) {
        this(context, null);
    }

    public CalendarView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CalendarView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        mSelectCalendar = Calendar.getInstance();
        mCalendar = Calendar.getInstance();

        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mSelectDate = new ArrayList<>();
        setClickable(true);
        initAttrs(attrs);
    }

    private void initAttrs(AttributeSet attrs) {
        TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.CalendarView);

        setTextColor(a.getColor(R.styleable.CalendarView_cv_textColor, Color.BLACK));
        setCurrentDayTextColor(a.getColor(R.styleable.CalendarView_cv_currentDayTextColor, Color.BLACK));
        setSelectTextColor(a.getColor(R.styleable.CalendarView_cv_selectTextColor, Color.BLACK));
        setTagTextColor(a.getColor(R.styleable.CalendarView_cv_tagTextColor, Color.BLACK));
        setTextSize(a.getDimension(R.styleable.CalendarView_cv_textSize, sp2px(14)));
        setBottomTextSize(a.getDimension(R.styleable.CalendarView_cv_bottomTextSize, sp2px(14)));
        setDayBackground(a.getDrawable(R.styleable.CalendarView_cv_dayBackground));
        setSelectDayBackground(a.getDrawable(R.styleable.CalendarView_cv_selectDayBackground));
        setDateFormatPattern(a.getString(R.styleable.CalendarView_cv_dateFormatPattern));
        setTodayTag(a.getString(R.styleable.CalendarView_cv_today2Show));

        a.recycle();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        mColumnWidth = getWidth() / 7;
        mRowHeight = getHeight() / 6;
        String currentDateStr = getCurrentDateStr();

        int year  = mCalendar.get(Calendar.YEAR);
        int month = mCalendar.get(Calendar.MONTH) + 1; // 获取的月份要少一月, 所以这里 + 1
        int days  = DateUtils.getMonthDays(year, month); // 获取当月的天数
        int week  = DateUtils.getFirstDayWeek(year, month); // 获取当月第一天位于周几

        for (int day = 1; day <= days; day++) { // 绘制每天
            // 获取天在行、列的位置
            int column  =  (day + week - 1) % 7;
            int row     =  (day + week - 1) / 7;

            mDays[row][column] = day; // 存储对应天

            String dayStr = String.valueOf(day);
            if (getFormatDate(year, month - 1, day).equals(currentDateStr) && !TextUtils.isEmpty(mTodayTag)) dayStr = mTodayTag;

            mPaint.setTextSize(mTextSize);
            float textWidth = mPaint.measureText(dayStr);
            float topTextHeight = mPaint.descent() - mPaint.ascent();
            int topX = (int) (mColumnWidth * column + (mColumnWidth - textWidth) / 2);

            int[] lunarDayInfo = LunarCalendar.solarToLunar(year, month, day);
            String lunarDay = LunarCalendar.getChinaDayString(lunarDayInfo[2]);
            mPaint.setTextSize(mBottomTextSize);
            float bottomTextWidth = mPaint.measureText(lunarDay);
            float bottomTextHeight = mPaint.descent() - mPaint.ascent();
            int bottomX = (int) (mColumnWidth * column + (mColumnWidth - bottomTextWidth) / 2);
            int topY = (int) (mRowHeight * row + (mRowHeight - topTextHeight - bottomTextHeight) / 2 + topTextHeight);
            int bottomY = topY + (int)bottomTextHeight;

            // 判断 day 是否在选择日期内
            if (mSelectDate == null || mSelectDate.size() == 0 ||
                    !mSelectDate.contains(getFormatDate(year, month - 1, day))){ // 没有则绘制默认背景和文字颜色
                drawBackground(canvas, mDayBackground, column, row);
                drawText(canvas, dayStr, mTextColor, mTextSize, topX, topY);
                drawText(canvas, lunarDay, mTextColor, mBottomTextSize, bottomX, bottomY);
            } else { // 绘制标记后的背景和文字颜色
                drawBackground(canvas, mDayBackground, column, row);
                drawText(canvas, dayStr, mTagTextColor, mTextSize, topX, topY);
                drawText(canvas, lunarDay, mTagTextColor, mBottomTextSize, bottomX, bottomY);
            }

            if (getFormatDate(year, month - 1, day).equals(currentDateStr)) {
                drawBackground(canvas, mDayBackground, column, row);
                drawText(canvas, dayStr, mCurrentDayTextColor, mTextSize, topX, topY);
                drawText(canvas, lunarDay, mCurrentDayTextColor, mBottomTextSize, bottomX, bottomY);
            }

            if (mCheckedDate.contains(getFormatDate(year, month - 1, day))) {
                drawBackground(canvas, mSelectDayBackground, column, row);
                drawText(canvas, dayStr, mSelectTextColor, mTextSize, topX, topY);
                drawText(canvas, lunarDay, mSelectTextColor, mBottomTextSize, bottomX, bottomY);
            }
        }
    }

    private void drawBackground(Canvas canvas, Drawable background, int column, int row) {
        if (background == null) return;

        canvas.save();
        int dx = (mColumnWidth * column) + mColumnWidth;
        int dy = (mRowHeight * row) + mRowHeight;
        background.setBounds(mColumnWidth * column, mRowHeight * row, dx, dy);
        background.draw(canvas);
        canvas.restore();
    }

    private void drawText(Canvas canvas, String text, @ColorInt int color, float size, int x, int y) {
        mPaint.setColor(color);
        mPaint.setTextSize(size);
        if (mTypeface != null) mPaint.setTypeface(mTypeface);
        canvas.drawText(text, x, y, mPaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!isClickable()) return false;

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mDownX = (int) event.getX();
                mDownY = (int) event.getY();
                return true;
            case MotionEvent.ACTION_MOVE:
                break;
            case MotionEvent.ACTION_UP:
                int upX = (int) event.getX();
                int upY = (int) event.getY();
                int diffX = Math.abs(upX - mDownX);
                int diffY = Math.abs(upY - mDownY);
                if(diffX < mSlop && diffY < mSlop){
                    int column = upX / mColumnWidth;
                    int row    = upY / mRowHeight;
                    onClick(mDays[row][column]);
                }
                break;
            default:
        }
        return super.onTouchEvent(event);
    }

    private void onClick(int day) {
        if (day < 1) return;

        int year = mCalendar.get(Calendar.YEAR);
        int month = mCalendar.get(Calendar.MONTH);
        if (mOnDataClickListener != null && !mOnDataClickListener.onDataClick(this, year, month, day)) return;

        mCheckedDate.clear();
        mCheckedDate.add(getFormatDate(year, month, day));
        invalidate();
    }

    /**
     * 设置选中的日期数据.
     *
     * @param days 日期数据, 日期格式为 {@link #setDateFormatPattern(String)} 方法所指定,
     * 如果没有设置则以默认的格式 {@link #DATE_FORMAT_PATTERN} 进行格式化.
     */
    public void setSelectDate(List<String> days) {
        this.mSelectDate = days;
        invalidate();
    }

    /**
     * 获取选中的日期数据.
     *
     * @return 日期数据.
     */
    public List<String> getSelectDate() {
        return mSelectDate;
    }

    public List<String> getCheckedDate() {
        return mCheckedDate;
    }

    /**
     * 切换到下一个月.
     */
    public void nextMonth() {
        mCalendar.add(Calendar.MONTH, 1);
        invalidate();
    }

    /**
     * 切换到上一个月.
     */
    public void lastMonth() {
        mCalendar.add(Calendar.MONTH, -1);
        invalidate();
    }

    /**
     * 获取当前年份.
     *
     * @return year.
     */
    public int getYear(){
        return mCalendar.get(Calendar.YEAR);
    }

    /**
     * 获取当前月份.
     *
     * @return month. (思考后, 决定这里直接按 Calendar 的 API 进行返回, 不进行 +1 处理)
     */
    public int getMonth(){
        return mCalendar.get(Calendar.MONTH);
    }

    /**
     * 设置当前显示的 Calendar 对象.
     *
     * @param calendar 对象.
     */
    public void setCalendar(Calendar calendar){
        this.mCalendar = calendar;
        invalidate();
    }

    /**
     * 获取当前显示的 Calendar 对象.
     *
     * @return Calendar 对象.
     */
    public Calendar getCalendar(){
        return mCalendar;
    }

    /**
     * 设置文字颜色.
     *
     * @param textColor 文字颜色 {@link ColorInt}.
     */
    public void setTextColor(@ColorInt int textColor){
        this.mTextColor = textColor;
    }

    public void setCurrentDayTextColor(@ColorInt int textColor) {
        this.mCurrentDayTextColor = textColor;
    }

    public void setSelectTextColor(@ColorInt int textColor){
        this.mSelectTextColor = textColor;
    }

    public void setTagTextColor(@ColorInt int textColor){
        this.mTagTextColor = textColor;
    }

    public void setTextSize(float textSize){
        this.mTextSize = textSize;
    }

    public void setBottomTextSize(float textSize){
        this.mBottomTextSize = textSize;
    }

    /**
     * 设置天的背景.
     *
     * @param background 背景 drawable.
     */
    public void setDayBackground(Drawable background) {
        if (background != null && mDayBackground != background) {
            this.mDayBackground = background;
            setCompoundDrawablesWithIntrinsicBounds(mDayBackground);
        }
    }

    /**
     * 设置选择后天的背景.
     *
     * @param background 背景 drawable.
     */
    public void setSelectDayBackground(Drawable background) {
        if (background != null && mSelectDayBackground != background) {
            this.mSelectDayBackground = background;
            setCompoundDrawablesWithIntrinsicBounds(mSelectDayBackground);
        }
    }

    /**
     * 设置日期格式化格式.
     *
     * @param pattern 格式化格式, 如: yyyy-MM-dd.
     */
    public void setDateFormatPattern(String pattern) {
        if (!TextUtils.isEmpty(pattern)) {
            this.mDateFormatPattern = pattern;
        } else {
            this.mDateFormatPattern = DATE_FORMAT_PATTERN;
        }
        this.mDateFormat = new SimpleDateFormat(mDateFormatPattern);
    }

    public void setTodayTag(String today) {
        this.mTodayTag = today;
    }

    /**
     * 获取日期格式化格式.
     *
     * @return 格式化格式.
     */
    public String getDateFormatPattern(){
        return mDateFormatPattern;
    }

    /**
     * 设置字体.
     *
     * @param typeface {@link Typeface}.
     */
    public void setTypeface(Typeface typeface) {
        this.mTypeface = typeface;
        invalidate();
    }

    /**
     * 获取 {@link Paint} 对象.
     * @return {@link Paint}.
     */
    public Paint getPaint(){
        return mPaint;
    }

    /**
     * 设置日期点击监听.
     *
     * @param listener 被通知的监听器.
     */
    public void setOnDataClickListener(OnDataClickListener listener) {
        this.mOnDataClickListener = listener;
    }

    /**
     * 设置选中日期改变监听器.
     *
     * @param listener 被通知的监听器.
     */
    public void setOnDateChangeListener(OnDateChangeListener listener) {
        this.mChangeListener = listener;
    }

    /**
     * 根据指定的年月日按当前日历的格式格式化后返回.
     *
     * @param year  年.
     * @param month 月.
     * @param day   日.
     * @return 格式化后的日期.
     */
    public String getFormatDate(int year, int month, int day) {
        mSelectCalendar.set(year, month, day);
        return mDateFormat.format(mSelectCalendar.getTime());
    }

    public String getCurrentDateStr() {
        Calendar currentCalendar = Calendar.getInstance();
        Date date = new Date();
        currentCalendar.setTime(date);
        int year  = currentCalendar.get(Calendar.YEAR);
        int month = currentCalendar.get(Calendar.MONTH);
        int day = currentCalendar.get(Calendar.DAY_OF_MONTH);
        return getFormatDate(year, month, day);
    }

    private void setCompoundDrawablesWithIntrinsicBounds(Drawable drawable) {
        if (drawable != null) {
            drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
        }
    }

    private int sp2px(float spVal) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, spVal, getContext().getResources().getDisplayMetrics());
    }
}
