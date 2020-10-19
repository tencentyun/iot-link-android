package com.tencent.iot.explorer.link.customview.dialog;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.tencent.iot.explorer.link.R;

public class ShareOptionDialog extends IosCenterStyleDialog {

    private String tipStr;
    private TextView tipContent;
    private TextView cancel;
    private TextView ok;
    private ConstraintLayout outsideLayout;

    public ShareOptionDialog(Context context) {
        super(context, R.layout.popup_share_option_layout);
    }

    @Override
    public void initView() {

    }

    private View.OnClickListener onClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            switch (v.getId())
            {

            }
            dismiss();
        }
    };

    private OnDismisListener onDismisListener;

    public interface OnDismisListener {
        void onCancelClicked();
        void onOkClicked();
    }

    public void setOnDismisListener(OnDismisListener onDismisListener) {
        this.onDismisListener = onDismisListener;
    }

    public void show() {
        if (!isShowing()) {
            super.show();
        }
    }

}
