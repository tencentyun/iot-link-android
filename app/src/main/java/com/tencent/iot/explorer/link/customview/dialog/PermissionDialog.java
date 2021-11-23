package com.tencent.iot.explorer.link.customview.dialog;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.tencent.iot.explorer.link.R;
import com.tencent.iot.explorer.link.customview.dialog.entity.UpgradeInfo;

public class PermissionDialog extends IosCenterStyleDialog {

    private ImageView logo;
    private TextView detail;
    private TextView title;
    private ConstraintLayout outLayout;
    private String detailString;
    private String titleString;
    private int logoResId;

    public PermissionDialog(Context context, int logoResId, String detail, String title) {
        super(context, R.layout.popup_permission_layout, false);
        this.logoResId = logoResId;
        this.detailString = detail;
        this.titleString = title;
    }

    @Override
    public void initView() {
        outLayout = view.findViewById(R.id.permission_dialog_layout);
        detail = view.findViewById(R.id.tv_detail);
        title = view.findViewById(R.id.tv_title);
        logo = view.findViewById(R.id.iv_logo);

        detail.setText(detailString);
        title.setText(titleString);
        logo.setImageResource(logoResId);
        outLayout.setBackgroundColor(getContext().getResources().getColor(R.color.dialog_background));
    }
}
