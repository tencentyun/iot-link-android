package com.tencent.iot.explorer.link.customview.dialog;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.tencent.iot.explorer.link.R;
import com.tencent.iot.explorer.link.customview.dialog.entity.UpgradeInfo;

public class PermissionDialog extends IosCenterStyleDialog implements View.OnClickListener {

    private TextView detail;
    private TextView detailLips;
    private TextView btnRefuse;
    private TextView btnOK;
    private ConstraintLayout outLayout;
    private String detailString;
    private String detailLipsString;

    public PermissionDialog(Context context, String detail, String detailLips) {
        super(context, R.layout.popup_permission_layout);
        this.detailString = detail;
        this.detailLipsString = detailLips;
    }

    @Override
    public void initView() {
        outLayout = view.findViewById(R.id.permission_dialog_layout);
        detail = view.findViewById(R.id.tv_detail);
        detailLips = view.findViewById(R.id.tv_detail_lips);
        btnRefuse = view.findViewById(R.id.tv_refuse);
        btnOK = view.findViewById(R.id.tv_ok);

        btnRefuse.setOnClickListener(this);
        btnOK.setOnClickListener(this);

        detail.setText(detailString);
        detailLips.setText(detailLipsString);
        outLayout.setBackgroundColor(getContext().getResources().getColor(R.color.dialog_background));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_refuse:
                dismiss();
                if (onDismisListener != null) {
                    onDismisListener.OnClickRefuse();
                }
                break;
            case R.id.tv_ok:
                dismiss();
                if (onDismisListener != null) {
                    onDismisListener.OnClickOK();
                }
                break;
            default:
                break;
        }
    }

    private volatile OnDismisListener onDismisListener;

    public interface OnDismisListener {
        void OnClickRefuse();
        void OnClickOK();
    }

    public void setOnDismisListener(OnDismisListener onDismisListener) {
        this.onDismisListener = onDismisListener;
    }
}
