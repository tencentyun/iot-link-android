package com.tencent.iot.explorer.link.customview.dialog;

import android.content.Context;
import android.view.View;
import android.widget.TextView;
import androidx.constraintlayout.widget.ConstraintLayout;
import com.tencent.iot.explorer.link.R;
import com.tencent.iot.explorer.link.customview.dialog.entity.UpgradeInfo;

public class UpgradeDialog extends IosCenterStyleDialog implements View.OnClickListener {

    private TextView title;
    private TextView log;
    private TextView version;
    private TextView packgeSize;
    private TextView publishTime;
    private TextView btnNextTime;
    private TextView btnUpgrade;
    private View centreView;
    private ConstraintLayout outLayout;
    private UpgradeInfo info;
    private boolean mIsForceUpgrade;
    private static boolean dialogShowing = false;
    private static boolean dialogShowed = false;

    public UpgradeDialog(Context context, UpgradeInfo info) {
        super(context, R.layout.popup_upgrade_layout);
        this.info = info;
    }

    @Override
    public void initView() {
        outLayout = view.findViewById(R.id.upgrade_dialog_layout);
        title = view.findViewById(R.id.tv_upgrade_tip);
        log = view.findViewById(R.id.tv_upgrade_log);
        version = view.findViewById(R.id.tv_version);
        packgeSize = view.findViewById(R.id.tv_package_size);
        publishTime = view.findViewById(R.id.tv_publish_time);
        btnNextTime = view.findViewById(R.id.tv_next);
        btnUpgrade = view.findViewById(R.id.tv_upgrade_now);
        centreView = view.findViewById(R.id.divid_line);

        btnNextTime.setOnClickListener(this);
        btnUpgrade.setOnClickListener(this);
        outLayout.setOnClickListener(this);

        title.setText(info.getTitle());
        log.setText(info.getLog());
        version.setText(getContext().getString(R.string.version_code) +
                getContext().getString(R.string.splite_from_name) + info.getVersion());
        packgeSize.setText(getContext().getString(R.string.package_size) +
                getContext().getString(R.string.splite_from_name) + info.getPackageSzie());
        publishTime.setText(getContext().getString(R.string.publish_time) +
                getContext().getString(R.string.splite_from_name) + info.getPublishTime());
        outLayout.setBackgroundColor(getContext().getResources().getColor(R.color.dialog_background));
        if (info.getUpgradeType() == 1) { // 2:静默更新不提示 1:强制升级 0:用户确认
            btnNextTime.setVisibility(View.GONE);
            centreView.setVisibility(View.GONE);
            // 拦截物理返回按键
            setCancelable(false);
            setCanceledOnTouchOutside(false);
            mIsForceUpgrade = true;
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.upgrade_dialog_layout:
            case R.id.tv_next:
                if (!mIsForceUpgrade) {
                    dismiss();
                    dialogShowing = false;
                }
                break;
            case R.id.tv_upgrade_now:
                if (onDismisListener != null) {
                    onDismisListener.OnClickUpgrade(info.getUrl());
                    dismiss();
                    dialogShowing = false;
                }
                break;
            default:
                break;
        }
    }

    public static boolean dialogShowing() {
        return dialogShowing;
    }

    public static boolean dialogShowed() {
        return dialogShowed;
    }

    private volatile OnDismisListener onDismisListener;

    public interface OnDismisListener {
        void OnClickUpgrade(String url);
    }

    public void setOnDismisListener(OnDismisListener onDismisListener) {
        this.onDismisListener = onDismisListener;
    }
}
