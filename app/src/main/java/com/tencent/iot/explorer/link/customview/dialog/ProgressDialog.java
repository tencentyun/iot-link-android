package com.tencent.iot.explorer.link.customview.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.TranslateAnimation;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.skydoves.progressview.ProgressView;
import com.tencent.iot.explorer.link.R;
import com.tencent.iot.explorer.link.kitlink.util.FileUtils;

public class ProgressDialog extends Dialog implements View.OnClickListener {

    public final static int MSG_REFRESH = 0;

    private DisplayMetrics displayMetrics;
    private View view;
    private Context mContext;
    private String url;
    private ConstraintLayout outLayout;
    private ProgressView progressBar;
    private volatile int count = 50;
    private String dir2StoreApk;

    public ProgressDialog(Context context, String url) {
        super(context, R.style.iOSDialog);
        mContext = context;
        displayMetrics = context.getResources().getDisplayMetrics();
        this.url = url;
        if (TextUtils.isEmpty(dir2StoreApk)) {
            dir2StoreApk = FileUtils.INSTANCE.getSdCardTempDirectory(mContext);
        }
    }

    public void setDir2StoreApk(String dir2StoreApk) {
        this.dir2StoreApk = dir2StoreApk;
    }

    public String getDir2StoreApk() {
        return this.dir2StoreApk;
    }

    Handler handler = new Handler() {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            if (msg.what == MSG_REFRESH) {
                if (progressBar != null) {
                    progressBar.setProgress(count);
                    progressBar.setLabelText(count + "%");
                }

                if (count >= 100 && onDismisListener != null) {
                    onDismisListener.onDownloadSuccess();
                } else if (count < 100 && onDismisListener != null) {
                    onDismisListener.onDownloadProgress(count, (int) progressBar.getMax());
                }
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

        view = View.inflate(mContext, R.layout.popup_download_progress_layout, null);
        view.setAnimation(animation);//设置动画
        initView();
    }

    private void initView() {
        progressBar = view.findViewById(R.id.progress_download);
        outLayout = view.findViewById(R.id.download_progress_dialog_layout);
        outLayout.setBackgroundColor(mContext.getResources().getColor(R.color.dialog_background));
        progressBar.setAnimating(true);
        refreshProgress();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            default:
                break;
        }
    }

    private void refreshProgress() {

        // 进度在 100 以内允许刷新
        if (count <= 100) {
            handler.sendEmptyMessage(MSG_REFRESH);
        }
    }

    private volatile OnDismisListener onDismisListener;

    //对外的接口回调
    public interface OnDismisListener {
        void onDownloadSuccess();
        void onDownloadFailed();
        void onDownloadProgress(int currentProgress, int size);
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
