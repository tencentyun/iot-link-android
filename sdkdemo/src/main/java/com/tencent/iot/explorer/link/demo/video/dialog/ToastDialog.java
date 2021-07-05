package com.tencent.iot.explorer.link.demo.video.dialog;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.widget.ImageView;
import android.widget.TextView;

import com.tencent.iot.explorer.link.demo.R;


public class ToastDialog extends IosCenterStyleDialog {

    public enum Type {
        SUCCESS, WARNING
    }

    private Type type;
    private String content;
    private long duration;
    private Handler handler = new Handler(Looper.getMainLooper());
    private int[] icons = {
            R.mipmap.icon_toast_dialog_success,
            R.mipmap.icon_toast_dialog_warning
    };

    /**
     * @param context 上下文
     * @param type 对话框类型，SUCCESS/WARNING
     * @param content 对话框内容
     * @param duration 对话框展示时长，单位ms
     */
    public ToastDialog(Context context, Type type, String content, long duration) {
        super(context, R.layout.popup_toast_layout);
        this.type = type;
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

        ImageView iv = view.findViewById(R.id.iv_status);
        TextView tv = view.findViewById(R.id.tv_content);
        tv.setText(content);
        iv.setImageResource(icons[type.ordinal()]);
    }

    @Override
    public void show() {
        super.show();
    }
}
