package com.tencent.iot.explorer.link.customview.dialog;

import android.content.Context;
import android.content.Intent;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.View;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.tencent.iot.explorer.link.R;
import com.tencent.iot.explorer.link.T;
import com.tencent.iot.explorer.link.core.utils.Utils;
import com.tencent.iot.explorer.link.kitlink.activity.WebActivity;
import com.tencent.iot.explorer.link.kitlink.consts.CommonField;

public class TipShareDevDialog extends IosCenterStyleDialog implements View.OnClickListener {
    private TextView okBtn;
    private TextView cancelBtn;
    private ConstraintLayout outsideLayout;
    private ConstraintLayout dialogLayout;
    private TextView moreInfo;
    private String ANDROID_ID = Utils.INSTANCE.getAndroidID(T.getContext());

    public TipShareDevDialog(Context context) {
        super(context, R.layout.popup_tip_share_dev_layout);
    }

    @Override
    public void initView() {
        okBtn = view.findViewById(R.id.tv_ok);
        cancelBtn = view.findViewById(R.id.tv_cancel);
        outsideLayout = view.findViewById(R.id.dialog_layout);
        dialogLayout = view.findViewById(R.id.tip_layout);
        moreInfo = view.findViewById(R.id.tv_content);

        String str = getContext().getResources().getString(R.string.share_dev_tip_content,
                getContext().getResources().getString(R.string.register_agree_4));
        String lastPart = getContext().getResources().getString(R.string.register_agree_4);
        SpannableStringBuilder spannable = new SpannableStringBuilder(str);
        spannable.setSpan(new TextClick(), str.length() - lastPart.length() - 1,
                str.length() - 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        moreInfo.setMovementMethod(LinkMovementMethod.getInstance());
        moreInfo.setText(spannable);

        okBtn.setOnClickListener(this);
        cancelBtn.setOnClickListener(this);
        outsideLayout.setOnClickListener(this);
        dialogLayout.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_ok:
                if (onDismisListener != null) {
                    onDismisListener.onOkClicked();
                }
                break;
            case R.id.tip_layout:
            case R.id.tv_more_info_tip:
                return;
            case R.id.tv_cancel:
            case R.id.dialog_layout:
            default:
                break;
        }
        dismiss();
    }

    private volatile OnDismisListener onDismisListener;

    public interface OnDismisListener {
        void onOkClicked();
    }

    public void setOnDismisListener(OnDismisListener onDismisListener) {
        this.onDismisListener = onDismisListener;
    }

    private class TextClick extends ClickableSpan {
        @Override
        public void onClick(View widget) {
            Intent intent = new Intent(getContext(), WebActivity.class);
            intent.putExtra(CommonField.EXTRA_TITLE, getContext().getString(R.string.register_agree_4));
            String url = CommonField.POLICY_PREFIX;
            url += "?uin=" + ANDROID_ID;
            url += CommonField.PRIVACY_POLICY_SUFFIX;
            intent.putExtra(CommonField.EXTRA_TEXT, url);
            getContext().startActivity(intent);
        }

        @Override
        public void updateDrawState(TextPaint ds) {
            ds.setColor(getContext().getResources().getColor(R.color.complete_progress));
        }
    }

}
