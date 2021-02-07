package com.tencent.iot.explorer.link.customview.dialog;

import android.content.Context;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.tencent.iot.explorer.link.R;

public class TipSwitchDialog extends IosCenterStyleDialog implements View.OnClickListener {
    private TextView okBtn;
    private TextView cancelBtn;
    private RelativeLayout dialogLayout;
    private ConstraintLayout outsideLayout;

    public TipSwitchDialog(Context context) {
        super(context, R.layout.popup_tip_switch_layout);
    }

    @Override
    public void initView() {
        okBtn = view.findViewById(R.id.tv_confirm);
        dialogLayout = view.findViewById(R.id.tip_layout);
        cancelBtn = view.findViewById(R.id.tv_cancel);
        outsideLayout = view.findViewById(R.id.dialog_layout);

        okBtn.setOnClickListener(this);
        dialogLayout.setOnClickListener(this);
        cancelBtn.setOnClickListener(this);
        outsideLayout.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_confirm:
                if (onDismisListener != null) {
                    onDismisListener.onOkClicked();
                }
                break;
            case R.id.tip_layout:
                return;
            case R.id.tv_cancel:
            case R.id.dialog_layout:
            default:
                if (onDismisListener != null) {
                    onDismisListener.onCancelClicked();
                }
                break;
        }
        dismiss();
    }

    private volatile OnDismisListener onDismisListener;

    public interface OnDismisListener {
        void onOkClicked();
        void onCancelClicked();
    }

    public void setOnDismisListener(OnDismisListener onDismisListener) {
        this.onDismisListener = onDismisListener;
    }

}
