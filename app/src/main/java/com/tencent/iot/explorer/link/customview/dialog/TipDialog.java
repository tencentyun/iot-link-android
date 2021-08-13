package com.tencent.iot.explorer.link.customview.dialog;

import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;

import com.tencent.iot.explorer.link.R;
import com.tencent.iot.explorer.link.customview.dialog.entity.TipInfo;

public class TipDialog extends IosCenterStyleDialog implements View.OnClickListener {
    private TextView okBtn;
    private TextView title;
    private TextView content;
    private TipInfo tipInfo = null;

    public TipDialog(Context context, TipInfo tipInfo) {
        super(context, R.layout.popup_tip_layout);
        this.tipInfo = tipInfo;
    }

    public TipDialog(Context context) {
        super(context, R.layout.popup_tip_layout);
    }

    @Override
    public void initView() {
        okBtn = view.findViewById(R.id.tv_confirm);
        title = view.findViewById(R.id.tv_smart_not_work);
        content = view.findViewById(R.id.tv_reason);
        okBtn.setOnClickListener(this);
        if (tipInfo != null) {
            okBtn.setText(tipInfo.getBtn());
            title.setText(tipInfo.getTitle());
            content.setText(tipInfo.getContent());
            content.setGravity(Gravity.LEFT);
        }
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
