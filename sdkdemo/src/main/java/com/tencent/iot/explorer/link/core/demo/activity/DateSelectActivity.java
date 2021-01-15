package com.tencent.iot.explorer.link.core.demo.activity;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.alibaba.fastjson.JSONArray;
import com.tencent.iot.explorer.link.core.demo.R;
import com.tencent.iot.explorer.link.core.demo.view.CalendarView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;


public class DateSelectActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    private CalendarView mCalendarView;
    private TextView mTxtDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_date);

        String dateArrStr = getIntent().getStringExtra("dataArr");
        List<String> all = new ArrayList<>();
        if (!TextUtils.isEmpty(dateArrStr)) {
            JSONArray jsonArray = JSONArray.parseArray(dateArrStr);
            for (int i = 0; i < jsonArray.size(); i++) {
                String tmp = jsonArray.getString(i);
                tmp = tmp.replace("-", "");
                all.add(tmp);
            }
        }

        mTxtDate = (TextView) findViewById(R.id.txt_date);
        mCalendarView = (CalendarView) findViewById(R.id.calendarView);
        // 设置已选的日期
        mCalendarView.setSelectDate(initData());

        // 指定显示的日期, 如当前月的下个月
        Calendar calendar = mCalendarView.getCalendar();
        calendar.add(Calendar.MONTH, 0);
        mCalendarView.setCalendar(calendar);

        // 设置字体
        mCalendarView.setTypeface(Typeface.SERIF);

        // 设置日期状态改变监听
        mCalendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(@NonNull CalendarView view, boolean select, int year, int month, int day) { }
        });
        // 设置是否能够改变日期状态
        mCalendarView.setChangeDateStatus(true);
        mCalendarView.setSelectDate(all);

        // 设置日期点击监听
        mCalendarView.setOnDataClickListener(new CalendarView.OnDataClickListener() {
            @Override
            public boolean onDataClick(@NonNull CalendarView view, int year, int month, int day) {

                String date = view.getFormatDate(year, month, day);
                if ((view.getSelectDate() != null && !view.getSelectDate().contains(date)) ||
                        view.getSelectDate() == null || view.getSelectDate().size() == 0){
                    Toast.makeText(DateSelectActivity.this, "当前日期不可选择", Toast.LENGTH_SHORT).show();
                    return false;
                }

                Intent dataIntent = new Intent();
                dataIntent.putExtra("year", year);
                dataIntent.putExtra("month", month);
                dataIntent.putExtra("day", day);
                setResult(RESULT_OK, dataIntent);
                finish();
                return false;
            }
        });
        // 设置是否能够点击
        mCalendarView.setClickable(true);

        setCurDate();
    }

    private List<String> initData() {
        List<String> dates = new ArrayList<>();
        Calendar calendar = Calendar.getInstance(Locale.CHINA);
        SimpleDateFormat sdf = new SimpleDateFormat(mCalendarView.getDateFormatPattern(), Locale.CHINA);
        sdf.format(calendar.getTime());
        dates.add(sdf.format(calendar.getTime()));
        return dates;
    }

    public void next(View v){
        mCalendarView.nextMonth();
        setCurDate();
    }

    public void last(View v){
        mCalendarView.lastMonth();
        setCurDate();
    }

    private void setCurDate(){
        mTxtDate.setText(mCalendarView.getYear() + "年" + (mCalendarView.getMonth() + 1) + "月");
    }
}
