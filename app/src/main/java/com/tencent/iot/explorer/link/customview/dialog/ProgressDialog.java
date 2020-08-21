package com.tencent.iot.explorer.link.customview.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
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
import com.tencent.iot.explorer.link.retrofit.DownloadRequest;

import java.io.File;
import java.io.IOException;

public class ProgressDialog extends Dialog implements View.OnClickListener {

    public final static int MSG_REFRESH = 0;
    public final static int MSG_DOWNLOAD_SUCCESS = 1;
    public final static int MSG_DOWNLOAD_FAILED = 2;

    private DisplayMetrics displayMetrics;
    private View view;
    private Context mContext;
    private String url;     // 下载文件使用的 url
    private ConstraintLayout outLayout;
    private ProgressView progressBar;
    private volatile int count = 0;    // 进度值
    private String dir2StoreApk;    // 下载文件时本地的存储路径

    public ProgressDialog(Context context, String url) {
        super(context, R.style.iOSDialog);
        mContext = context;
        displayMetrics = context.getResources().getDisplayMetrics();
        this.url = url;
        if (TextUtils.isEmpty(dir2StoreApk)) {
            dir2StoreApk = mContext.getCacheDir().getAbsolutePath() + "/temp.apk";
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

                if (count < 100 && onDismisListener != null) {
                    onDismisListener.onDownloadProgress(count, (int) progressBar.getMax());
                }
            } else if (msg.what == MSG_DOWNLOAD_SUCCESS) {
                progressBar.setProgress(100);
                progressBar.setLabelText(mContext.getResources().getString(R.string.download_success));
                onDismisListener.onDownloadSuccess(dir2StoreApk);
                dismiss();
            } else if (msg.what == MSG_DOWNLOAD_FAILED) {
                onDismisListener.onDownloadFailed();
                dismiss();
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

        File file = new File(dir2StoreApk);
        if (file.exists()) {
            file.delete();
        }
        try {
            file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }

        DownloadRequest.get().download(url, dir2StoreApk, downloadlistener);
    }

    DownloadRequest.OnDownloadListener downloadlistener = new DownloadRequest.OnDownloadListener() {

        @Override
        public void onDownloadSuccess(String requestId) {
            handler.sendEmptyMessage(MSG_DOWNLOAD_SUCCESS);
        }

        @Override
        public void onDownloading(String requestId, int progress) {
            count = progress;
            refreshProgress();
        }

        @Override
        public void onDownloadFailed(String requestId) {
            handler.sendEmptyMessage(MSG_DOWNLOAD_FAILED);
        }
    };

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
        void onDownloadSuccess(String path);
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
