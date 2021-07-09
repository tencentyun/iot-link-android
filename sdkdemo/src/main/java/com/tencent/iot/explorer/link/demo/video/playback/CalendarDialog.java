package com.tencent.iot.explorer.link.demo.video.playback;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.tencent.iot.explorer.link.demo.R;
import com.tencent.iot.explorer.link.demo.common.customView.CalendarView;
import com.tencent.iot.explorer.link.demo.video.utils.IosCenterStyleDialog;

import java.util.List;

public class CalendarDialog extends IosCenterStyleDialog implements View.OnClickListener{

    private CalendarView calendar;
    private TextView month2Show;
    private View lastMonth;
    private View nextMonth;
    private TextView cancel;
    private TextView ok;
    private ConstraintLayout outsideLayout;
    private ConstraintLayout insideLayout;
    private OnClickedListener onClickedListener;
    private List<String> allDate2Tag;

    public List<String> getAllDate2Tag() {
        return this.allDate2Tag;
    }

    public void setOnClickedListener(OnClickedListener onClickedListener) {
        this.onClickedListener = onClickedListener;
    }

    public CalendarDialog(Context context, List<String> allDate2Tag) {
        super(context, R.layout.popup_calendar_layout);
        this.allDate2Tag = allDate2Tag;
    }

    @Override
    public void initView() {
        calendar = view.findViewById(R.id.calendar_view);
        month2Show = view.findViewById(R.id.tv_month_tip);
        lastMonth = view.findViewById(R.id.btn_last_month);
        nextMonth = view.findViewById(R.id.btn_next_month);
        cancel = view.findViewById(R.id.tv_cancel);
        ok = view.findViewById(R.id.tv_ok);
        outsideLayout = view.findViewById(R.id.outside_dialog_layout);
        insideLayout = view.findViewById(R.id.inside_layout);

        lastMonth.setOnClickListener(this);
        nextMonth.setOnClickListener(this);
        cancel.setOnClickListener(this);
        ok.setOnClickListener(this);
        outsideLayout.setOnClickListener(this);
        insideLayout.setOnClickListener(this);

        setCurDate();
        calendar.setSelectDate(allDate2Tag);
    }

    @Override
    public void show() {
        super.show();
    }

    public void next(){
        calendar.nextMonth();
        setCurDate();
    }

    public void last(){
        calendar.lastMonth();
        setCurDate();
    }

    private void setCurDate() {
        month2Show.setText(getContext().getString(R.string.year_and_month_unit,
                String.valueOf(calendar.getYear()), String.valueOf((calendar.getMonth() + 1))));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId())
        {
            case R.id.btn_last_month:
                last();
                break;
            case R.id.btn_next_month:
                next();
                break;
            case R.id.inside_layout:
                return;
            case R.id.tv_cancel:
            case R.id.outside_dialog_layout:
                dismiss();
                break;
            case R.id.tv_ok:
                if (onClickedListener != null) {
                    if (calendar.getCheckedDate() == null || calendar.getCheckedDate().size() <= 0) {
                        onClickedListener.onOkClickedWithoutDateChecked();
                        return;
                    }
                    if (calendar.getCheckedDate() != null && calendar.getCheckedDate().size() > 0 &&
                            !allDate2Tag.containsAll(calendar.getCheckedDate())) {
                        onClickedListener.onOkClickedCheckedDateWithoutData();
                        return;
                    }
                    onClickedListener.onOkClicked(calendar.getCheckedDate());
                }
                dismiss();
                break;
        }
    }

    public interface OnClickedListener {
        void onOkClicked(List<String> checkedDates);
        void onOkClickedWithoutDateChecked();
        void onOkClickedCheckedDateWithoutData();
    }
}
