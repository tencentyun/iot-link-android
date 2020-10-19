package com.tencent.iot.explorer.link.customview.dialog;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.tencent.iot.explorer.link.R;
import com.tencent.iot.explorer.link.kitlink.util.WeChatLogin;
import com.tencent.iot.explorer.link.util.AppInfoUtils;
import com.tencent.iot.explorer.link.util.T;

public class ShareOptionDialog extends IosCenterStyleDialog {

    private TextView cancel;
    private ConstraintLayout outsideLayout;
    private ConstraintLayout wechatLayout;
    private ConstraintLayout copyLinkLayout;
    private String linkInfo;

    public ShareOptionDialog(Context context, String linkInfo) {
        super(context, R.layout.popup_share_option_layout);
        this.linkInfo = linkInfo;
    }

    @Override
    public void initView() {
        outsideLayout = view.findViewById(R.id.outside_dialog_layout);
        wechatLayout = view.findViewById(R.id.layout_wechat_share);
        copyLinkLayout = view.findViewById(R.id.layout_copy_link);
        cancel = view.findViewById(R.id.tv_cancel);

        wechatLayout.setOnClickListener(onClickListener);
        copyLinkLayout.setOnClickListener(onClickListener);
        cancel.setOnClickListener(onClickListener);
        outsideLayout.setOnClickListener(onClickListener);
    }

    private View.OnClickListener onClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            switch (v.getId())
            {
                case R.id.layout_copy_link:
                    AppInfoUtils.copy(getContext(), linkInfo);
                    T.show(getContext().getString(R.string.copy));
                    break;
                case R.id.layout_wechat_share:
                    WeChatLogin.Companion.getInstance().shareText(getContext(), "123");
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
