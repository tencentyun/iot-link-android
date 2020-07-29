package com.tencent.iot.explorer.link.customview.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;
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
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.tencent.iot.explorer.link.R;

public class ConnectWifiDialog extends Dialog implements View.OnClickListener {

    public final static int CONNECTING_WIFI = 0;
    public final static int CONNECT_WIFI_SUCCESS = 1;
    public final static int CONNECT_WIFI_FAILED = 2;
    public final static int MSG_REFRESH = 0;

    private DisplayMetrics displayMetrics;
    private View view;
    private Context mContext;
    ImageView loadStateImg;
    TextView loadStateTxt;
    TextView okBtn;
    RelativeLayout loadStatelayout;
    RelativeLayout failedlayout;
    ConstraintLayout backgroundLayout;
    private volatile int status;
    RotateAnimation rotate;

    public ConnectWifiDialog(Context context) {
        super(context, R.style.iOSDialog);
        mContext = context;
        displayMetrics = context.getResources().getDisplayMetrics();
        status = CONNECTING_WIFI;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    Handler handler = new Handler() {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            if (msg.what == MSG_REFRESH) {
                refreshViewByProgress();
                autoDismiss();
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

        view = View.inflate(mContext, R.layout.popup_connect_layout, null);
        view.setAnimation(animation);//设置动画

        rotate = new RotateAnimation(0f, 360f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        LinearInterpolator lin = new LinearInterpolator();
        rotate.setInterpolator(lin);
        rotate.setDuration(2000);//设置动画持续周期
        rotate.setRepeatCount(-1);//设置重复次数
        rotate.setFillAfter(true);//动画执行完后是否停留在执行完的状态
        rotate.setStartOffset(10);//执行前的等待时间

        loadStateImg = view.findViewById(R.id.iv_connect_progress);
        loadStateTxt = view.findViewById(R.id.tv_view);
        okBtn = view.findViewById(R.id.i_got_it);
        loadStatelayout = view.findViewById(R.id.connect_loading_layout);
        failedlayout = view.findViewById(R.id.connect_failed_layout);
        backgroundLayout = view.findViewById(R.id.connect_dialog_layout);
        refreshViewByProgress();
        okBtn.setOnClickListener(this);
        backgroundLayout.setOnClickListener(this);
        failedlayout.setOnClickListener(this);
    }

    public void refreshState() {
        handler.sendEmptyMessage(MSG_REFRESH);
    }

    public void autoDismiss() {
        if (status == CONNECT_WIFI_SUCCESS) {
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    dismiss();
                    if (onDismisListener != null) {
                        onDismisListener.OnDismisedBySuccess();
                    }
                }
            }, 1000 * 2);
        }
    }

    public void refreshViewByProgress() {
        switch (status) {
            case CONNECTING_WIFI:
                loadStatelayout.setVisibility(View.VISIBLE);
                failedlayout.setVisibility(View.GONE);
                loadStateImg.setImageResource(R.mipmap.loading);
                loadStateImg.setAnimation(rotate);
                loadStateTxt.setText(R.string.wifi_connecting);
                backgroundLayout.setBackgroundColor(mContext.getResources().getColor(R.color.btn_normal));
                break;
            case CONNECT_WIFI_SUCCESS:
                loadStatelayout.setVisibility(View.VISIBLE);
                failedlayout.setVisibility(View.GONE);
                loadStateImg.setImageResource(R.mipmap.success);
                loadStateImg.setAnimation(null);
                loadStateTxt.setText(R.string.wifi_connect_success);
                backgroundLayout.setBackgroundColor(mContext.getResources().getColor(R.color.btn_normal));
                break;
            case CONNECT_WIFI_FAILED:
                loadStatelayout.setVisibility(View.GONE);
                failedlayout.setVisibility(View.VISIBLE);
                backgroundLayout.setBackgroundColor(mContext.getResources().getColor(R.color.dialog_background));
                break;
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.connect_failed_layout:
                return;
            case R.id.i_got_it:
                break;
            case R.id.connect_dialog_layout:
                break;
            default:
                break;
        }
        dismiss();
    }

    private volatile OnDismisListener onDismisListener;

    //对外的接口回调
    public interface OnDismisListener {
        void OnDismisedBySuccess();
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
