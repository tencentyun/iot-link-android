package com.tencent.iot.explorer.link.demo.video.dialog;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.tencent.iot.explorer.link.demo.R;

public class VideoQualityDialog extends IosCenterStyleDialog {

    private ConstraintLayout outsideLayout;
    private ConstraintLayout insideLayout;
    private Context context;
    private TextView vQL;
    private TextView vQM;
    private TextView vQH;
    private int pos = 0;
    private OnDismisListener onDismisListener;

    public VideoQualityDialog(Context context, int pos) {
        super(context, R.layout.popup_video_quality_options_layout);
        this.context = context;
        this.pos = pos;
    }

    @Override
    public void initView() {
        outsideLayout = view.findViewById(R.id.outside_dialog_layout);
        insideLayout = view.findViewById(R.id.inside_layout);
        vQL = view.findViewById(R.id.tv_video_quality_low);
        vQM = view.findViewById(R.id.tv_video_quality_medium);
        vQH = view.findViewById(R.id.tv_video_quality_heigh);
        vQL.setText(context.getString(R.string.video_quality_low) + "\n" + context.getString(R.string.video_quality_low_str));
        vQM.setText(context.getString(R.string.video_quality_medium) + "\n" + context.getString(R.string.video_quality_medium_str));
        vQH.setText(context.getString(R.string.video_quality_high) + "\n" + context.getString(R.string.video_quality_high_str));

        if (pos == 0) {
            vQL.setBackgroundColor(context.getResources().getColor(R.color.blue_0052D9));
        } else if (pos == 1) {
            vQM.setBackgroundColor(context.getResources().getColor(R.color.blue_0052D9));
        } else if (pos == 2) {
            vQH.setBackgroundColor(context.getResources().getColor(R.color.blue_0052D9));
        }

        outsideLayout.setOnClickListener(onClickListener);
        vQM.setOnClickListener(onClickListener);
        vQH.setOnClickListener(onClickListener);
        vQL.setOnClickListener(onClickListener);
        insideLayout.setOnClickListener(onClickListener);
    }

    private View.OnClickListener onClickListener = v -> {
        switch (v.getId()) {
            case R.id.tv_video_quality_low:
                if (onDismisListener != null) {
                    onDismisListener.onItemClicked(2);
                }
                break;
            case R.id.tv_video_quality_medium:
                if (onDismisListener != null) {
                    onDismisListener.onItemClicked(1);
                }
                break;
            case R.id.tv_video_quality_heigh:
                if (onDismisListener != null) {
                    onDismisListener.onItemClicked(0);
                }
                break;
            case R.id.inside_layout:
                return;
        }
        dismiss();
        if (onDismisListener != null) {
            onDismisListener.onDismiss();
        }
    };

    public interface OnDismisListener {
        void onItemClicked(int pos);
        void onDismiss();
    }

    public void setOnDismisListener(OnDismisListener onDismisListener) {
        this.onDismisListener = onDismisListener;
    }

    public void show() {
        super.show();
    }

}
