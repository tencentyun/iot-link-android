package com.tencent.iot.explorer.link.customview.dialog;

import android.content.Context;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.aigestudio.wheelpicker.WheelPicker;
import com.tencent.iot.explorer.link.R;
import com.tencent.iot.explorer.link.customview.dialog.IosCenterStyleDialog;
import com.tencent.iot.explorer.link.customview.dialog.adapter.ListOptionsAdapter;

import java.util.ArrayList;
import java.util.List;

public class SelectWoringTimeDialog extends IosCenterStyleDialog {

    private TextView cancel;
    private TextView save;
    private RecyclerView daysTypeOptions;
    private ConstraintLayout outsideLayout;
    private ConstraintLayout startLayout;
    private ConstraintLayout definationTimeLayout;
    private ConstraintLayout definationDayLayout;
    private ConstraintLayout definationTimeByUser;//layout_defination_by_user;
    private ConstraintLayout definationDefault;//layout_defination_default;
    //wheel_start_time_hour
    private WheelPicker startHourPicker;
    private WheelPicker startMinPicker;
    private WheelPicker centerPicker;
    private WheelPicker endHourPicker;
    private WheelPicker endMinPicker;
    private WorkTimeMode workTimeMode;
    private Context context;
    private ImageView defalutTimeTypeStatus;
    private ImageView definationTimeTypeStatus;
    private List<String> hours = new ArrayList<>();
    private List<String> mins = new ArrayList<>();
    private List<String> centers = new ArrayList<>();
    private int timeTypeIndex = 0;

    public SelectWoringTimeDialog(Context context, WorkTimeMode workTimeMode) {
        super(context, R.layout.popup_select_time_layout);
        this.context = context;
        this.workTimeMode = workTimeMode;

        for (int i = 0; i < 24; i++) {
            hours.add("" + i);
        }

        for (int i = 0; i < 60; i++) {
            mins.add(String.format("%02d", i));
        }

        centers.add("-");
    }

    @Override
    public void initView() {
        outsideLayout = view.findViewById(R.id.outside_dialog_layout);
        cancel = view.findViewById(R.id.tv_cancel);
        save = view.findViewById(R.id.tv_ok);
        definationDefault = view.findViewById(R.id.layout_defination_default);
        definationTimeByUser = view.findViewById(R.id.layout_defination_by_user);
        daysTypeOptions = view.findViewById(R.id.lv_day_options_type);
        startLayout = view.findViewById(R.id.layout_dialog_default_start);
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
        startHourPicker.setVisibleItemCount(5);
        endHourPicker.setVisibleItemCount(5);
        startMinPicker.setVisibleItemCount(5);
        endMinPicker.setVisibleItemCount(5);
        centerPicker.setVisibleItemCount(5);

        // 禁止滑动
        centerPicker.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });

//        adapter.setOnItemClicked(onItemClicked);
//        LinearLayoutManager layoutManager = new LinearLayoutManager(this.context);
//        daysTypeOptions.setLayoutManager(layoutManager);
//        daysTypeOptions.setAdapter(adapter);

        save.setOnClickListener(onClickListener);
        cancel.setOnClickListener(onClickListener);
        outsideLayout.setOnClickListener(onClickListener);
        definationDefault.setOnClickListener(onClickListener);
        definationTimeByUser.setOnClickListener(onClickListener);

        startLayout.setVisibility(View.VISIBLE);
        showLayout();
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

        if (timeTypeIndex == 0) {
            defalutTimeTypeStatus.setImageResource(R.mipmap.dev_mode_sel);
            definationTimeTypeStatus.setImageResource(R.mipmap.dev_mode_unsel);
        } else {
            defalutTimeTypeStatus.setImageResource(R.mipmap.dev_mode_unsel);
            definationTimeTypeStatus.setImageResource(R.mipmap.dev_mode_sel);
        }
    }

    private void clearAllLayout() {
        startLayout.setVisibility(View.GONE);
        definationDayLayout.setVisibility(View.GONE);
        definationTimeLayout.setVisibility(View.GONE);
    }

    private ListOptionsAdapter.OnItemClicked onItemClicked = new ListOptionsAdapter.OnItemClicked() {
        @Override
        public void onItemClicked(int postion, String option) {
//            if (onDismisListener != null) {
//                onDismisListener.onItemClicked(postion);
//            }
            dismiss();
        }
    };

    private View.OnClickListener onClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            switch (v.getId())
            {
                case R.id.layout_defination_default:
                    timeTypeIndex = 0;
                    clearAllLayout();
                    startLayout.setVisibility(View.VISIBLE);
                    showLayout();
                    return;
                case R.id.layout_defination_by_user:
                    timeTypeIndex = 1;
                    clearAllLayout();
                    definationTimeLayout.setVisibility(View.VISIBLE);
                    showLayout();
                    return;
                case R.id.tv_cancel:
                    break;
                case R.id.tv_ok:
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
