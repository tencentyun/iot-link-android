package com.tencent.iot.explorer.link.demo.video.playback;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.tencent.iot.explorer.link.demo.R;
import com.tencent.iot.explorer.link.demo.common.customView.CalendarView;
import com.tencent.iot.explorer.link.demo.databinding.PopupCalendarLayoutBinding;
import com.tencent.iot.explorer.link.demo.video.utils.IosCenterStyleDialog;

import java.util.List;

public class CalendarDialog extends IosCenterStyleDialog<PopupCalendarLayoutBinding> implements View.OnClickListener{

    private OnClickedListener onClickedListener;
    private OnMonthChanged onMonthChanged;
    private List<String> allDate2Tag;

    public List<String> getAllDate2Tag() {
        return this.allDate2Tag;
    }

    public void addTagDates(List<String> tagDates) {
        allDate2Tag.addAll(tagDates);
        binding.calendarView.invalidate();
    }

    public void setOnClickedListener(OnClickedListener onClickedListener) {
        this.onClickedListener = onClickedListener;
    }

    public void setOnMonthChanged(OnMonthChanged onMonthChanged) {
        this.onMonthChanged = onMonthChanged;
    }

    public CalendarDialog(Context context, List<String> allDate2Tag) {
        super(context, PopupCalendarLayoutBinding.inflate(LayoutInflater.from(context)));
        this.allDate2Tag = allDate2Tag;
    }

    public CalendarDialog(Context context, List<String> allDate2Tag, OnMonthChanged onMonthChanged) {
        this(context, allDate2Tag);
        this.onMonthChanged = onMonthChanged;
    }

    @Override
    public void initView() {
        binding.btnLastMonth.setOnClickListener(this);
        binding.btnNextMonth.setOnClickListener(this);
        binding.tvCancel.setOnClickListener(this);
        binding.tvOk.setOnClickListener(this);
        binding.outsideDialogLayout.setOnClickListener(this);
        binding.insideLayout.setOnClickListener(this);

        setCurDate();
        binding.calendarView.setSelectDate(allDate2Tag);
    }

    @Override
    public void show() {
        super.show();
    }

    public void next(){
        binding.calendarView.nextMonth();
        setCurDate();
    }

    public void last(){
        binding.calendarView.lastMonth();
        setCurDate();
    }

    private void setCurDate() {
        binding.tvMonthTip.setText(getContext().getString(R.string.year_and_month_unit,
                String.valueOf(binding.calendarView.getYear()), String.valueOf((binding.calendarView.getMonth() + 1))));
        if (onMonthChanged != null) {
            onMonthChanged.onMonthChanged(this, String.format("%04d-%02d", binding.calendarView.getYear(), binding.calendarView.getMonth() + 1));
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_last_month) {
            last();
        } else if (v.getId() == R.id.btn_next_month) {
            next();
        } else if (v.getId() == R.id.tv_cancel || v.getId() == R.id.outside_dialog_layout) {
            dismiss();
        } else if (v.getId() == R.id.tv_ok) {
            if (onClickedListener != null) {
                if (binding.calendarView.getCheckedDate() == null || binding.calendarView.getCheckedDate().size() <= 0) {
                    onClickedListener.onOkClickedWithoutDateChecked();
                    return;
                }
//                    if (calendar.getCheckedDate() != null && calendar.getCheckedDate().size() > 0 &&
//                            !allDate2Tag.containsAll(calendar.getCheckedDate())) {
//                        onClickedListener.onOkClickedCheckedDateWithoutData();
//                        return;
//                    }
                onClickedListener.onOkClicked(binding.calendarView.getCheckedDate());
            }
            dismiss();
        }
    }

    public interface OnClickedListener {
        void onOkClicked(List<String> checkedDates);
        void onOkClickedWithoutDateChecked();
        void onOkClickedCheckedDateWithoutData();
    }

    public interface OnMonthChanged {
        void onMonthChanged(CalendarDialog dlg, String dateStr);
    }
}
