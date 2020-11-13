package com.tencent.iot.explorer.link.customview.dialog;

import android.content.Context;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.aigestudio.wheelpicker.WheelPicker;
import com.tencent.iot.explorer.link.R;
import com.tencent.iot.explorer.link.T;
import com.tencent.iot.explorer.link.customview.dialog.adapter.DayTypeOptionsAdapter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SelectWoringTimeDialog extends IosCenterStyleDialog {

    private TextView cancel;
    private TextView save;
    private TextView definationByUser;
    private RecyclerView daysTypeOptions;
    private RecyclerView dayOptions;
    private ConstraintLayout outsideLayout;
    private ConstraintLayout insideLayout;
    private ConstraintLayout startLayout;
    private ConstraintLayout definationTimeLayout;
    private ConstraintLayout definationDayLayout;
    private ConstraintLayout definationTimeByUser;
    private ConstraintLayout definationDefault;
    private WheelPicker startHourPicker;
    private WheelPicker startMinPicker;
    private WheelPicker centerPicker;
    private WheelPicker endHourPicker;
    private WheelPicker endMinPicker;
    private WorkTimeMode workTimeMode;
    private Context context;
    private ImageView defalutTimeTypeStatus;
    private ImageView definationTimeTypeStatus;
    private ImageView definationTimeBack;
    private ImageView definationDayBack;
    private List<String> hours = new ArrayList<>();
    private List<String> mins = new ArrayList<>();
    private List<String> centers = new ArrayList<>();
    private Handler handler = new Handler();
    private List<String> dayType = new ArrayList<>();
    private List<String> days = new ArrayList<>();
    private Set<Integer> tmpStatusSaved = new HashSet<>();
    private DayTypeOptionsAdapter adapter = new DayTypeOptionsAdapter(dayType);
    private DayTypeOptionsAdapter dayAdapter = null;

    public SelectWoringTimeDialog(Context context, WorkTimeMode workTimeMode) {
        super(context, R.layout.popup_select_time_layout);
        this.context = context;
        this.workTimeMode = workTimeMode;

        for (int i = 0; i < 24; i++) {
            hours.add(i + context.getResources().getString(R.string.unit_h_single));
        }

        for (int i = 0; i < 60; i++) {
            mins.add(i + context.getResources().getString(R.string.unit_m_single));
        }

        centers.add("-");
    }

    @Override
    public void initView() {
        definationByUser = view.findViewById(R.id.tv_defination_by_user);
        outsideLayout = view.findViewById(R.id.outside_dialog_layout);
        insideLayout = view.findViewById(R.id.layout_all);
        cancel = view.findViewById(R.id.tv_cancel);
        save = view.findViewById(R.id.tv_ok);
        dayOptions = view.findViewById(R.id.gv_time_options);
        definationDefault = view.findViewById(R.id.layout_defination_default);
        definationTimeByUser = view.findViewById(R.id.layout_defination_by_user);
        daysTypeOptions = view.findViewById(R.id.lv_day_options_type);
        startLayout = view.findViewById(R.id.layout_dialog_default_start);
        definationTimeBack = view.findViewById(R.id.iv_back_dialog_defination_time_by_user);
        definationDayBack = view.findViewById(R.id.iv_back_dialog_defination_day_by_user);
        definationTimeLayout = view.findViewById(R.id.layout_dialog_defination_time_by_user);
        definationDayLayout = view.findViewById(R.id.layout_dialog_defination_day_by_user);
        defalutTimeTypeStatus = view.findViewById(R.id.iv_status_defination_default);
        definationTimeTypeStatus = view.findViewById(R.id.iv_status_defination_by_user);
        startHourPicker = view.findViewById(R.id.wheel_start_time_hour);
        startMinPicker = view.findViewById(R.id.wheel_start_time_min);
        centerPicker = view.findViewById(R.id.wheel_center);
        endHourPicker = view.findViewById(R.id.wheel_end_time_hour);
        endMinPicker = view.findViewById(R.id.wheel_end_time_min);
        startHourPicker.setData(hours);
        startMinPicker.setData(mins);
        endHourPicker.setData(hours);
        endMinPicker.setData(mins);
        centerPicker.setData(centers);
        startHourPicker.setSelected(true);
        startMinPicker.setSelected(true);
        endHourPicker.setSelected(true);
        endMinPicker.setSelected(true);
        centerPicker.setSelected(true);
        centerPicker.setIndicator(true);
        endMinPicker.setIndicator(true);
        startMinPicker.setIndicator(true);
        endHourPicker.setIndicator(true);
        startHourPicker.setIndicator(true);
        centerPicker.setAtmospheric(true);
        endMinPicker.setAtmospheric(true);
        startMinPicker.setAtmospheric(true);
        endHourPicker.setAtmospheric(true);
        startHourPicker.setAtmospheric(true);

        // 禁止滑动
        centerPicker.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });

        dayType.add(context.getString(R.string.everyday));
        dayType.add(context.getString(R.string.work_day));
        dayType.add(context.getString(R.string.weekend));
        dayType.add(context.getString(R.string.defination_by_user));

        days.add(context.getString(R.string.sunday));
        days.add(context.getString(R.string.monday));
        days.add(context.getString(R.string.tuesday));
        days.add(context.getString(R.string.wednesday));
        days.add(context.getString(R.string.thursday));
        days.add(context.getString(R.string.friday));
        days.add(context.getString(R.string.saturday));
        dayAdapter = new DayTypeOptionsAdapter(days, false);

        adapter.setOnItemClicked(onItemClicked);
        GridLayoutManager layoutManager = new GridLayoutManager(this.context, 3);
        daysTypeOptions.setLayoutManager(layoutManager);
        daysTypeOptions.setAdapter(adapter);

        GridLayoutManager dayLayoutManager = new GridLayoutManager(this.context, 3);
        dayOptions.setLayoutManager(dayLayoutManager);
        dayOptions.setAdapter(dayAdapter);
        setDaysItemStatus(0);

        save.setOnClickListener(onClickListener);
        cancel.setOnClickListener(onClickListener);
        outsideLayout.setOnClickListener(onClickListener);
        definationDefault.setOnClickListener(onClickListener);
        definationTimeByUser.setOnClickListener(onClickListener);
        definationTimeBack.setOnClickListener(onClickListener);
        definationDayBack.setOnClickListener(onClickListener);

        if (workTimeMode.getTimeType() == 0) {
            resetModeTime();
        } else {
            setModeTime(workTimeMode.getStartTimeHour(), workTimeMode.getStartTimerMin(), workTimeMode.getEndTimeHour(), workTimeMode.getEndTimeMin());
        }

        adapter.setIndex(new HashSet<Integer>(){{add(workTimeMode.getWorkDayType());}});
        if (workTimeMode.getWorkDayType() == 0) {
            workTimeMode.setWorkDays("1111111");
        } else if (workTimeMode.getWorkDayType() == 1) {
            workTimeMode.setWorkDays("0111110");
        } else if (workTimeMode.getWorkDayType() == 2) {
            workTimeMode.setWorkDays("1000001");
        } else if (workTimeMode.getWorkDayType() == 3) {
//            dayAdapter.setIndex(WorkTimeMode.Companion.convertDays2DaySet(workTimeMode.getWorkDays()));
        }
        dayAdapter.setIndex(WorkTimeMode.Companion.convertDays2DaySet(workTimeMode.getWorkDays()));

        showView(startLayout);
    }

    private void showLayout() {
        if (startLayout.getVisibility() == View.VISIBLE) {
            definationTimeLayout.setVisibility(View.GONE);
            definationDayLayout.setVisibility(View.GONE);

        } else if (definationTimeLayout.getVisibility() == View.VISIBLE) {
            startLayout.setVisibility(View.GONE);
            definationDayLayout.setVisibility(View.GONE);

        } else if (definationDayLayout.getVisibility() == View.VISIBLE) {
            startLayout.setVisibility(View.GONE);
            definationTimeLayout.setVisibility(View.GONE);
        }

        if (workTimeMode.getTimeType() == 0) {
            defalutTimeTypeStatus.setImageResource(R.mipmap.dev_mode_sel);
            definationTimeTypeStatus.setImageResource(R.mipmap.dev_mode_unsel);
            definationByUser.setText(R.string.defination_by_user);
        } else {
            defalutTimeTypeStatus.setImageResource(R.mipmap.dev_mode_unsel);
            definationTimeTypeStatus.setImageResource(R.mipmap.dev_mode_sel);
            definationByUser.setText(getContext().getString(R.string.defination_by_user_with_content,
                    WorkTimeMode.Companion.getCurrentTimeLongStr(workTimeMode)));
        }

        setViewTime();
    }

    private void clearAllLayout() {
        startLayout.setVisibility(View.GONE);
        definationDayLayout.setVisibility(View.GONE);
        definationTimeLayout.setVisibility(View.GONE);
    }

    private void resetModeTime() {
        setModeTime(0, 0, hours.size() - 1, mins.size() - 1);
    }

    private void setModeTime(int startHour, int startMin, int endHour, int endMin) {
        workTimeMode.setStartTimeHour(startHour);
        workTimeMode.setStartTimerMin(startMin);
        workTimeMode.setEndTimeHour(endHour);
        workTimeMode.setEndTimeMin(endMin);
    }

    private void setViewTime() {
        handler.postDelayed( new Runnable () {
                                 @Override
                                 public void run() {
                                     endMinPicker.setSelectedItemPosition(workTimeMode.getEndTimeMin(), false);
                                     endHourPicker.setSelectedItemPosition(workTimeMode.getEndTimeHour(), false);
                                     startMinPicker.setSelectedItemPosition(workTimeMode.getStartTimerMin(), false);
                                     startHourPicker.setSelectedItemPosition(workTimeMode.getStartTimeHour(), false);
                                 }
                             },
                10);
    }

    private DayTypeOptionsAdapter.OnItemClicked onItemClicked = new DayTypeOptionsAdapter.OnItemClicked() {
        @Override
        public void onItemClicked(int postion, String option) {
            if (postion == dayType.size() - 1) {
                // 启动下一个窗口前，先缓存当前窗口被选择项的记录
                tmpStatusSaved.clear();
                tmpStatusSaved.addAll(dayAdapter.getIndex());
                showView(definationDayLayout);
            } else {
                workTimeMode.setWorkDayType(postion);
                setDaysItemStatus(postion);
            }
        }
    };

    private void setDaysItemStatus(int postion) {
        switch (postion)
        {
            case 0:
                dayAdapter.setSelectOption(new HashSet<Integer>(){{
                    add(0);
                    add(1);
                    add(2);
                    add(3);
                    add(4);
                    add(5);
                    add(6);
                }});
                break;
            case 1:
                dayAdapter.setSelectOption(new HashSet<Integer>(){{
                    add(1);
                    add(2);
                    add(3);
                    add(4);
                    add(5);
                }});
                break;
            case 2:
                dayAdapter.setSelectOption(new HashSet<Integer>(){{
                    add(0);
                    add(6);
                }});
                break;
            case 3:
                break;
        }
    }

    private void showView(View view) {
        clearAllLayout();
        view.setVisibility(View.VISIBLE);
        showLayout();
    }

    private View.OnClickListener onClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            switch (v.getId())
            {
                case R.id.layout_defination_default:
                    workTimeMode.setTimeType(0);
                    resetModeTime();
                    showView(startLayout);
                    return;
                case R.id.iv_back_dialog_defination_time_by_user:
                    // 点击返回按钮，不会改变界面状态，也不会改变之前的显示状况
                    showView(startLayout);
                    return;
                case R.id.layout_defination_by_user:
                    // 点击自定义不一定使用自定义，当点击自定义的时候不切换状态
                    showView(definationTimeLayout);
                    return;
                case R.id.iv_back_dialog_defination_day_by_user:
                    showView(startLayout);
                    return;
                case R.id.tv_cancel:
                    if (definationTimeLayout.getVisibility() == View.VISIBLE) {
                        showView(startLayout);
                    } else if (definationDayLayout.getVisibility() == View.VISIBLE) {
                        showView(startLayout);
                        dayAdapter.setSelectOption(tmpStatusSaved);
                        tmpStatusSaved.clear();
                    } else if (startLayout.getVisibility() == View.VISIBLE) {
                        dismiss();
                    }
                    break;
                case R.id.tv_ok:
                    if (definationTimeLayout.getVisibility() == View.VISIBLE) {
                        WorkTimeMode tag = new WorkTimeMode();
                        tag.setStartTimeHour(startHourPicker.getCurrentItemPosition());
                        tag.setStartTimerMin(startMinPicker.getCurrentItemPosition());
                        tag.setEndTimeHour(endHourPicker.getCurrentItemPosition());
                        tag.setEndTimeMin(endMinPicker.getCurrentItemPosition());
                        if (!WorkTimeMode.Companion.ifTimeLegal(tag)) {
                            T.show(getContext().getString(R.string.end_time_earlier_start_time));
                            return;
                        }

                        if (WorkTimeMode.Companion.isAllDay(tag)) {
                            workTimeMode.setTimeType(0);
                            resetModeTime();
                            showView(startLayout);
                            return;
                        }
                        // 点击保存，界面处于自定义选择框时，保存当前选择的内容
                        workTimeMode.setTimeType(1);
                        setModeTime(startHourPicker.getCurrentItemPosition(), startMinPicker.getCurrentItemPosition(),
                                endHourPicker.getCurrentItemPosition(), endMinPicker.getCurrentItemPosition());
                        showView(startLayout);
                    } else if (definationDayLayout.getVisibility() == View.VISIBLE) {
                        String days = WorkTimeMode.Companion.convetDaySet2Days(dayAdapter.getIndex());
                        int dayType = WorkTimeMode.Companion.getDayType(days);
                        if (dayType < 0) {
                            T.show(context.getString(R.string.at_least_select_one_day));
                            return;
                        } else {
                            workTimeMode.setWorkDayType(dayType);
                            workTimeMode.setWorkDays(days);
                            adapter.setIndex(new HashSet<Integer>() {{
                                add(workTimeMode.getWorkDayType());
                            }});
                            adapter.notifyDataSetChanged();
                        }
                        showView(startLayout);
                    } else if (onDismisListener != null && startLayout.getVisibility() == View.VISIBLE) {
                        onDismisListener.onSaveClicked(workTimeMode);
                        dismiss();
                    }
                    break;
                case R.id.outside_dialog_layout:
                    dismiss();
                    break;
                case R.id.layout_all:
                    break;
            }
        }
    };

    private OnDismisListener onDismisListener;

    public interface OnDismisListener {
        void onSaveClicked(WorkTimeMode workTimeMode);
        void onCancelClicked();
    }

    public void setOnDismisListener(OnDismisListener onDismisListener) {
        this.onDismisListener = onDismisListener;
    }

    public void show() {
        if (!isShowing()) {
            super.show();
        }
    }

}
