package com.tencent.iot.explorer.link.demo.video.utils;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.tencent.iot.explorer.link.demo.R;


public class TipToastDialog extends IosCenterStyleDialog {

    private String content;
    private long duration;
    private Handler handler = new Handler(Looper.getMainLooper());

    /**
     * 构造函数
     *
     * @param context 上下文
     * @param content 对话框内容
     * @param duration 对话框展示时长，单位ms
     */
    public TipToastDialog(Context context, String content, long duration) {
        super(context, R.layout.popup_tip_toast_layout);
        this.content = content;
        this.duration = duration;
    }

    @Override
    public void initView() {
        handler.postDelayed(() -> {
            if (isShowing()) {
                super.dismiss();
            }
        }, duration);

        TextView tv = view.findViewById(R.id.tv_content);
        tv.setText(content);
        TextView ok = view.findViewById(R.id.tv_ok);
        ok.setOnClickListener(v -> dismiss());
    }

    @Override
    public void show() {
        super.show();
    }
}
