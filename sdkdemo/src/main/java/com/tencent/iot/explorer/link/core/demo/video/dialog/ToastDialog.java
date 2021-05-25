package com.tencent.iot.explorer.link.core.demo.video.dialog;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.widget.ImageView;
import android.widget.TextView;

import com.tencent.iot.explorer.link.core.demo.R;


public class ToastDialog extends IosCenterStyleDialog {

    public static final int SUCCESS = 0;
    public static final int WARNING = 1;
    public static final int FAIL = 2;

    private int type;
    private String content;
    private Context context;
    private Handler handler = new Handler(Looper.getMainLooper());

    public ToastDialog(Context context, int type, String content) {
        super(context, R.layout.popup_toast_layout);
        this.type = type;
        this.content = content;
        this.context = context;
    }

    @Override
    public void initView() {
        handler.postDelayed(() -> {
            if (isShowing()) {
                super.dismiss();
            }
        }, 1000);

        ImageView iv = view.findViewById(R.id.iv_status);
        TextView tv = view.findViewById(R.id.tv_content);
        tv.setText(content);
        switch (type) {
            case SUCCESS:
                iv.setImageResource(R.mipmap.icon_toast_dialog_success);
                break;
            case WARNING:
                iv.setImageResource(R.mipmap.icon_toast_dialog_warning);
                break;
            case FAIL:
            default:
                break;
        }
    }

    @Override
    public void show() {
        if (!isShowing()) {
            super.show();
        }
    }
}
