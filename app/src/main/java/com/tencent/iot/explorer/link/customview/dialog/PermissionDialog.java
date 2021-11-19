package com.tencent.iot.explorer.link.customview.dialog;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.tencent.iot.explorer.link.R;
import com.tencent.iot.explorer.link.customview.dialog.entity.UpgradeInfo;

public class PermissionDialog extends IosCenterStyleDialog {

    private TextView detail;
    private TextView title;
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
        title = view.findViewById(R.id.tv_title);

        detail.setText(detailString);
        title.setText(detailLipsString);
        outLayout.setBackgroundColor(getContext().getResources().getColor(R.color.dialog_background));
    }
}
