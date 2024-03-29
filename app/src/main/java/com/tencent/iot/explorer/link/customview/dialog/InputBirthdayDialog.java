package com.tencent.iot.explorer.link.customview.dialog;

import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.aigestudio.wheelpicker.WheelPicker;
import com.aigestudio.wheelpicker.widgets.WheelDatePicker;
import com.tencent.iot.explorer.link.R;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class InputBirthdayDialog extends IosCenterStyleDialog implements View.OnClickListener {
    private TextView okBtn;
    private TextView cancelBtn;
    private TextView detailTv;
    private WheelPicker yearPicker;
    private WheelPicker monthPicker;
    private WheelPicker dayPicker;
    private ConstraintLayout outsideLayout;
    private ConstraintLayout dialogLayout;
    private Handler handler = new Handler();
    private List<String> days = new ArrayList<>();
    private List<String> years = new ArrayList<>();
    private List<String> months = new ArrayList<>();
    private int MAX_YEAR_NUM;
    private final int MIN_YEAR = 1900;
    private final int MAX_MONTH_NUM = 12;
    private String detailString;

    public InputBirthdayDialog(Context context, String countryCode) {
        super(context, R.layout.popup_birthday_layout);
        if (countryCode.equals("1")) {
            detailString = context.getString(R.string.birthday_detail_usa);
        } else {
            detailString = context.getString(R.string.birthday_detail_china);
        }

        Date currentDate = new Date();
        final int year = currentDate.getYear() + MIN_YEAR;
        MAX_YEAR_NUM = year - MIN_YEAR + 1; // 出生日期最大只能是当年
        for (int i = MIN_YEAR; i < MIN_YEAR + MAX_YEAR_NUM; i++) {
            years.add(i + context.getResources().getString(R.string.unit_year));
        }

        for (int i = 0; i < MAX_MONTH_NUM; i++) {
            months.add((i + 1) + context.getResources().getString(R.string.unit_mouth));
        }
    }

    @Override
    public void initView() {
        okBtn = view.findViewById(R.id.tv_ok);
        cancelBtn = view.findViewById(R.id.tv_cancel);
        detailTv = view.findViewById(R.id.tv_detail);
        yearPicker = view.findViewById(R.id.wheel_timer_year_picker);
        monthPicker = view.findViewById(R.id.wheel_timer_month_picker);
        dayPicker = view.findViewById(R.id.wheel_timer_day_picker);
        outsideLayout = view.findViewById(R.id.outside_dialog_layout);
        dialogLayout = view.findViewById(R.id.tip_layout);
        yearPicker.setData(years);
        monthPicker.setData(months);

        okBtn.setOnClickListener(this);
        cancelBtn.setOnClickListener(this);
        outsideLayout.setOnClickListener(this);
        dialogLayout.setOnClickListener(this);
        monthPicker.setOnItemSelectedListener(yearMonthSelectedListener);
        yearPicker.setOnItemSelectedListener(yearMonthSelectedListener);

        detailTv.setText(detailString);
        initDateView();
    }

    private void initDateView() {
        Date currentDate = new Date();
        final int year = currentDate.getYear() + MIN_YEAR;
        final int month = currentDate.getMonth() + 1;
        final int day = currentDate.getDay();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                yearPicker.setSelectedItemPosition(year - MIN_YEAR);
                monthPicker.setSelectedItemPosition(month - 1);
                initDayData(year, month);
                dayPicker.setSelectedItemPosition(day - 1);
            }
        }, 100);
    }

    private void initDayData(int year, int month) {
        days.clear();
        Date tagDate = new Date();
        tagDate.setYear(year - MIN_YEAR);
        tagDate.setMonth(month - 1);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(tagDate);
        int maxDay = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
        for (int i = 0; i < maxDay; i++) {
            days.add((i + 1) + getContext().getResources().getString(R.string.unit_day));
        }
        dayPicker.setData(days);
    }

    private WheelPicker.OnItemSelectedListener yearMonthSelectedListener = new WheelPicker.OnItemSelectedListener() {

        @Override
        public void onItemSelected(WheelPicker picker, Object data, int position) {
            if (picker == monthPicker) {
                initDayData(yearPicker.getCurrentItemPosition() + MIN_YEAR, monthPicker.getCurrentItemPosition() + 1);
            } else if (picker == yearPicker) {
                initDayData(yearPicker.getCurrentItemPosition() + MIN_YEAR, monthPicker.getCurrentItemPosition() + 1);
            }
        }
    };

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_ok:
                if (onDismisListener != null) {
                    onDismisListener.onOkClicked(yearPicker.getCurrentItemPosition() + MIN_YEAR,
                            monthPicker.getCurrentItemPosition() + 1, dayPicker.getCurrentItemPosition() + 1);
                }
                break;
            case R.id.tip_layout:
                return;
            case R.id.tv_cancel:
            case R.id.outside_dialog_layout:
                if (onDismisListener != null) {
                    onDismisListener.onCancelClicked();
                }
            default:
                break;
        }
        dismiss();
    }

    private volatile OnDismisListener onDismisListener;

    public interface OnDismisListener {
        void onOkClicked(int year, int month, int day);
        void onCancelClicked();
    }

    public void setOnDismissListener(OnDismisListener onDismisListener) {
        this.onDismisListener = onDismisListener;
    }

}
