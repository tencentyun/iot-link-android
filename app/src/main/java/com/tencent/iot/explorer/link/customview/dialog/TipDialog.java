package com.tencent.iot.explorer.link.customview.dialog;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

import com.tencent.iot.explorer.link.R;

public class TipDialog extends IosCenterStyleDialog implements View.OnClickListener {
    TextView okBtn;

    public TipDialog(Context context) {
        super(context, R.layout.popup_tip_layout);
    }

    @Override
    public void initView() {
        okBtn = view.findViewById(R.id.tv_confirm);
        okBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_confirm:
                if (onDismisListener != null) {
                    onDismisListener.onDismised();
                }
                break;
            default:
                break;
        }
        dismiss();
    }

    private volatile OnDismisListener onDismisListener;

    public interface OnDismisListener {
        void onDismised();
    }

    public void setOnDismisListener(OnDismisListener onDismisListener) {
        this.onDismisListener = onDismisListener;
    }

}
