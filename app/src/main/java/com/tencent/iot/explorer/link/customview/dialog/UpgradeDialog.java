package com.tencent.iot.explorer.link.customview.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.tencent.iot.explorer.link.R;

public class UpgradeDialog extends Dialog implements View.OnClickListener {

    public final static int MSG_REFRESH = 0;

    private DisplayMetrics displayMetrics;
    private View view;
    private Context mContext;
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

    public UpgradeDialog(Context context, UpgradeInfo info) {
        super(context, R.style.iOSDialog);
        mContext = context;
        displayMetrics = context.getResources().getDisplayMetrics();
        this.info = info;
    }

    Handler handler = new Handler() {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            if (msg.what == MSG_REFRESH) {

            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //设置view 弹出的平移动画，从底部-100% 平移到自身位置
        TranslateAnimation animation = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0f, Animation.RELATIVE_TO_SELF,
                0f, Animation.RELATIVE_TO_SELF, 1f, Animation.RELATIVE_TO_SELF, 0);
        animation.setInterpolator(new DecelerateInterpolator());
        animation.setDuration(350);
        animation.setStartOffset(150);

        view = View.inflate(mContext, R.layout.popup_upgrade_layout, null);
        view.setAnimation(animation);//设置动画
        initView();
        btnNextTime.setOnClickListener(this);
        btnUpgrade.setOnClickListener(this);
        outLayout.setOnClickListener(this);
    }

    private void initView() {
        outLayout = view.findViewById(R.id.upgrade_dialog_layout);
        title = view.findViewById(R.id.tv_upgrade_tip);
        log = view.findViewById(R.id.tv_upgrade_log);
        version = view.findViewById(R.id.tv_version);
        packgeSize = view.findViewById(R.id.tv_package_size);
        publishTime = view.findViewById(R.id.tv_publish_time);
        btnNextTime = view.findViewById(R.id.tv_next);
        btnUpgrade = view.findViewById(R.id.tv_upgrade_now);
        centreView = view.findViewById(R.id.divid_line);

        title.setText(info.getTitle());
        log.setText(info.getLog());
        version.setText(mContext.getResources().getString(R.string.version_code) +
                mContext.getResources().getString(R.string.splite_from_name) + info.getVersion());
        packgeSize.setText(mContext.getResources().getString(R.string.package_size) +
                mContext.getResources().getString(R.string.splite_from_name) + info.getPackageSzie());
        publishTime.setText(mContext.getResources().getString(R.string.publish_time) +
                mContext.getResources().getString(R.string.splite_from_name) + info.getPublishTime());
        outLayout.setBackgroundColor(mContext.getResources().getColor(R.color.dialog_background));
        if (info.getUpgradeType() == 1) {
            btnNextTime.setVisibility(View.GONE);
            centreView.setVisibility(View.GONE);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.upgrade_dialog_layout:
            case R.id.tv_next:
                break;
            case R.id.tv_upgrade_now:
                if (onDismisListener != null) {
                    onDismisListener.OnClickUpgrade(info.getUrl());
                }
                break;
            default:
                break;
        }
        dismiss();
    }

    private volatile OnDismisListener onDismisListener;

    //对外的接口回调
    public interface OnDismisListener {
        void OnClickUpgrade(String url);
    }

    public void setOnDismisListener(OnDismisListener onDismisListener) {
        this.onDismisListener = onDismisListener;
    }

    @Override
    public void show() {
        super.show();
        // 设置dialog的宽高是全屏，注意：一定要放在show的后面，否则不是全屏显示
        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.width = displayMetrics.widthPixels;
        params.height = ViewGroup.LayoutParams.MATCH_PARENT;
        params.gravity = Gravity.CENTER;
        getWindow().setAttributes(params);
        getWindow().setContentView(view);
    }

}
