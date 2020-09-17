package com.tencent.iot.explorer.link.customview.dialog;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.tencent.iot.explorer.link.R;

public class WifiHelperDialog extends IosCenterStyleDialog {

    private String tipStr;
    private TextView tipContent;
    private TextView cancel;
    private TextView ok;
    private ConstraintLayout outsideLayout;

    public WifiHelperDialog(Context context, String tipStr) {
        super(context, R.layout.popup_cancel_ok_layout);
        this.tipStr = tipStr;
    }

    @Override
    public void initView() {
        tipContent = view.findViewById(R.id.tv_tip_content);
        cancel = view.findViewById(R.id.tv_cancel);
        ok = view.findViewById(R.id.tv_to_set);
        outsideLayout = view.findViewById(R.id.outside_dialog_layout);

        tipContent.setText(tipStr);
        cancel.setOnClickListener(onClickListener);
        ok.setOnClickListener(onClickListener);
        outsideLayout.setOnClickListener(onClickListener);
    }

    private View.OnClickListener onClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            switch (v.getId())
            {
                case R.id.tv_cancel:
                    if (onDismisListener != null) {
                        onDismisListener.onCancelClicked();
                    }
                    break;
                case R.id.tv_to_set:
                    if (onDismisListener != null) {
                        onDismisListener.onOkClicked();
                    }
                    break;
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
