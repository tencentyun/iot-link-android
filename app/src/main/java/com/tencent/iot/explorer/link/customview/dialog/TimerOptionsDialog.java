package com.tencent.iot.explorer.link.customview.dialog;

import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.aigestudio.wheelpicker.WheelPicker;
import com.alibaba.fastjson.JSON;
import com.tencent.iot.explorer.link.R;
import com.tencent.iot.explorer.link.T;
import com.tencent.iot.explorer.link.customview.dialog.adapter.DayTypeOptionsAdapter;
import com.tencent.iot.explorer.link.customview.dialog.adapter.ListOptionsAdapter;
import com.tencent.iot.explorer.link.kitlink.entity.TimerExtra;

import java.util.ArrayList;
import java.util.List;


public class TimerOptionsDialog extends IosCenterStyleDialog {

    private TextView cancel;
    private TextView save;
    private WheelPicker timerType;
    private RecyclerView daysOptions;
    private ConstraintLayout outsideLayout;
    private ConstraintLayout timerTypeLayout;
    private ConstraintLayout daysLayout;
    private Context context;
    private List<String> dayType = new ArrayList<>();
    private List<String> days = new ArrayList<>();
    private TimerExtra timerExtra;
    private ImageView back;
    private Handler handler = new Handler();
    private DayTypeOptionsAdapter adapter;

    public TimerOptionsDialog(Context context, TimerExtra timerExtra) {
        super(context, R.layout.popup_timer_layout);
        this.context = context;
        this.timerExtra = timerExtra;
    }

    @Override
    public void initView() {
        outsideLayout = view.findViewById(R.id.outside_dialog_layout);
        cancel = view.findViewById(R.id.tv_cancel);
        daysOptions = view.findViewById(R.id.gv_days_options);
        timerTypeLayout = view.findViewById(R.id.layout_timer_type);
        daysLayout = view.findViewById(R.id.layout_timer_days);
        timerType = view.findViewById(R.id.wheel_timer_type_picker);
        save = view.findViewById(R.id.tv_ok);
        back = view.findViewById(R.id.iv_back_dialog_defination_day_by_user);

        dayType.add(context.getString(R.string.run_one_time));
        dayType.add(context.getString(R.string.everyday));
        dayType.add(context.getString(R.string.work_day));
        dayType.add(context.getString(R.string.weekend));
        dayType.add(context.getString(R.string.defination_by_user));
        timerType.setData(dayType);
        timerType.setIndicator(true);

        days.add(context.getString(R.string.sunday));
        days.add(context.getString(R.string.monday));
        days.add(context.getString(R.string.tuesday));
        days.add(context.getString(R.string.wednesday));
        days.add(context.getString(R.string.thursday));
        days.add(context.getString(R.string.friday));
        days.add(context.getString(R.string.saturday));
        adapter = new DayTypeOptionsAdapter(days, false);
        GridLayoutManager layoutManager = new GridLayoutManager(this.context, 3);
        daysOptions.setLayoutManager(layoutManager);
        daysOptions.setAdapter(adapter);
        adapter.setIndex(TimerExtra.Companion.convertDays2DaySet(timerExtra.getWorkDays()));
        adapter.notifyDataSetChanged();

        cancel.setOnClickListener(onClickListener);
        outsideLayout.setOnClickListener(onClickListener);
        back.setOnClickListener(onClickListener);
        save.setOnClickListener(onClickListener);
        timerType.setOnItemSelectedListener(listener);

        resetTimerRepeatType();
        showView(timerTypeLayout);
    }

    private WheelPicker.OnItemSelectedListener listener = new WheelPicker.OnItemSelectedListener() {

        @Override
        public void onItemSelected(WheelPicker picker, Object data, int position) {
            if (position == dayType.size() - 1) {
                showView(daysLayout);
            }
        }
    };

    private void showView(View view) {
        daysLayout.setVisibility(View.GONE);
        timerTypeLayout.setVisibility(View.GONE);
        view.setVisibility(View.VISIBLE);
        if (timerTypeLayout.getVisibility() == View.VISIBLE) {
            resetTimerRepeatType();
        }

        adapter.setIndex(TimerExtra.Companion.convertDays2DaySet(timerExtra.getWorkDays()));
        adapter.notifyDataSetChanged();
    }

    private void resetTimerRepeatType() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                timerType.setSelectedItemPosition(timerExtra.getRepeatType());
            }
        }, 10);
    }

    private View.OnClickListener onClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            switch (v.getId())
            {
                case R.id.tv_ok:
                    if (daysLayout.getVisibility() == View.VISIBLE) {
                        String tmp = TimerExtra.Companion.convetDaySet2Days(adapter.getIndex());
                        if (TimerExtra.Companion.getDayType(tmp) == 0) {
                            T.show(getContext().getResources().getString(R.string.at_least_select_one_day));
                            return;
                        } else {
                            timerExtra.setRepeatType(TimerExtra.Companion.getDayType(tmp));
                            timerExtra.setWorkDays(TimerExtra.Companion.convetDaySet2Days(adapter.getIndex()));

                        }
                    } else if (timerTypeLayout.getVisibility() == View.VISIBLE) {
                        timerExtra.setRepeatType(timerType.getCurrentItemPosition());
                        if (timerExtra.getRepeatType() == 0) {
                            timerExtra.setWorkDays("0000000");
                        } else if (timerExtra.getRepeatType() == 1) {
                            timerExtra.setWorkDays("1111111");
                        } else if (timerExtra.getRepeatType() == 2) {
                            timerExtra.setWorkDays("0111110");
                        } else if (timerExtra.getRepeatType() == 3) {
                            timerExtra.setWorkDays("1000001");
                        }
                        if (timerType.getCurrentItemPosition() == dayType.size() - 1) {
                            showView(daysLayout);
                            return;
                        }
                    }
                    if (onDismisListener != null) {
                        onDismisListener.onSaved(timerExtra);
                    }
                    dismiss();
                    break;
                case R.id.tv_cancel:
                    if (daysLayout.getVisibility() == View.VISIBLE) {
                        showView(timerTypeLayout);
                    } else if (timerTypeLayout.getVisibility() == View.VISIBLE) {
                        dismiss();
                    }
                    break;
                case R.id.outside_dialog_layout:
                    dismiss();
                    break;
                case R.id.iv_back_dialog_defination_day_by_user:
                    showView(timerTypeLayout);
                    break;
            }
        }
    };

    private OnDismisListener onDismisListener;

    public interface OnDismisListener {
        void onSaved(TimerExtra timerExtra);
        void onCanceled();
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
