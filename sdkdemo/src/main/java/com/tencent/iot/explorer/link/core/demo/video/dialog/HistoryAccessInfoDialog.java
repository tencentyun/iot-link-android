package com.tencent.iot.explorer.link.core.demo.video.dialog;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.aigestudio.wheelpicker.WheelPicker;
import com.tencent.iot.explorer.link.core.demo.R;

public class HistoryAccessInfoDialog extends IosCenterStyleDialog implements View.OnClickListener {
    private TextView okBtn;
    private TextView cancelBtn;
    private WheelPicker accessHistoryPicker;
    private ConstraintLayout outsideLayout;
    private ConstraintLayout dialogLayout;

    public HistoryAccessInfoDialog(Context context) {
        super(context, R.layout.popup_history_access_layout);
    }

    @Override
    public void initView() {
        accessHistoryPicker = view.findViewById(R.id.wheel_access_info_picker);
        okBtn = view.findViewById(R.id.tv_ok);
        cancelBtn = view.findViewById(R.id.tv_cancel);
        dialogLayout = view.findViewById(R.id.tip_layout);
        outsideLayout = view.findViewById(R.id.outside_dialog_layout);

        accessHistoryPicker.setOnItemSelectedListener(selectedListener);
        okBtn.setOnClickListener(this);
        cancelBtn.setOnClickListener(this);
        dialogLayout.setOnClickListener(this);
        outsideLayout.setOnClickListener(this);
    }

    private WheelPicker.OnItemSelectedListener selectedListener = new WheelPicker.OnItemSelectedListener() {
        @Override
        public void onItemSelected(WheelPicker picker, Object data, int position) {
        }
    };

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_cancel:
            case R.id.outside_dialog_layout:
                if (onDismisListener != null) {
                    onDismisListener.onCancelClicked();
                }
                break;
            case R.id.tv_ok:
                if (onDismisListener != null) {
                    onDismisListener.onOkClicked();
                }
                break;
            case R.id.tip_layout:
                return;
        }
        dismiss();
    }

    private volatile OnDismisListener onDismisListener;

    public interface OnDismisListener {
        void onOkClicked();
        void onCancelClicked();
    }

    public void setOnDismissListener(OnDismisListener onDismisListener) {
        this.onDismisListener = onDismisListener;
    }

}
