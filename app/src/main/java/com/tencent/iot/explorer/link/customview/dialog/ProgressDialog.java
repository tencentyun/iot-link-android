package com.tencent.iot.explorer.link.customview.dialog;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import com.skydoves.progressview.ProgressView;
import com.tencent.iot.explorer.link.R;
import com.tencent.iot.explorer.link.T;
import com.tencent.iot.explorer.link.retrofit.DownloadRequest;
import java.io.File;
import java.io.IOException;

public class ProgressDialog extends IosCenterStyleDialog {

    public final static int MSG_REFRESH = 0;
    public final static int MSG_DOWNLOAD_SUCCESS = 1;
    public final static int MSG_DOWNLOAD_FAILED = 2;

    private String url;     // 下载文件使用的 url
    private ConstraintLayout outLayout;
    private ProgressView progressBar;
    private volatile int count = 0;    // 进度值
    private String dir2StoreApk;    // 下载文件时本地的存储路径

    public ProgressDialog(Context context, String url) {
        super(context, R.layout.popup_download_progress_layout);
        this.url = url;
        if (TextUtils.isEmpty(dir2StoreApk)) {
            dir2StoreApk = getContext().getCacheDir().getAbsolutePath() + "/temp.apk";
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
                progressBar.setLabelText(getContext().getString(R.string.download_success));
                onDismisListener.onDownloadSuccess(dir2StoreApk);
                dismiss();
            } else if (msg.what == MSG_DOWNLOAD_FAILED) {
                onDismisListener.onDownloadFailed();
                dismiss();
            }
        }
    };

    @Override
    public void initView() {
        progressBar = view.findViewById(R.id.progress_download);
        outLayout = view.findViewById(R.id.download_progress_dialog_layout);
        outLayout.setBackgroundColor(getContext().getResources().getColor(R.color.dialog_background));
        progressBar.setAnimating(true);
        refreshProgress();

        File file = new File(dir2StoreApk);
        if (file.exists())  file.delete();
        try {
            file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (TextUtils.isEmpty(url)) {
            T.show(getContext().getResources().getString(R.string.url_empty));
            handler.sendEmptyMessage(MSG_DOWNLOAD_FAILED);
            return;
        }

        DownloadRequest.get().download(url, dir2StoreApk, downloadlistener);
        // 拦截物理返回按键
        setCancelable(false);
        setCanceledOnTouchOutside(false);
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

    private void refreshProgress() {
        // 进度在 100 以内允许刷新
        if (count <= 100) {
            handler.sendEmptyMessage(MSG_REFRESH);
        }
    }

    private volatile OnDismisListener onDismisListener;

    public interface OnDismisListener {
        void onDownloadSuccess(String path);
        void onDownloadFailed();
        void onDownloadProgress(int currentProgress, int size);
    }

    public void setOnDismisListener(OnDismisListener onDismisListener) {
        this.onDismisListener = onDismisListener;
    }

}
