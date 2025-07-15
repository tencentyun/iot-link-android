package com.tencent.iot.explorer.link.demo.video.preview;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.tencent.iot.explorer.link.demo.R;
import com.tencent.iot.explorer.link.demo.databinding.PopupVideoQualityOptionsLayoutBinding;
import com.tencent.iot.explorer.link.demo.video.utils.IosCenterStyleDialog;

public class VideoQualityDialog extends IosCenterStyleDialog<PopupVideoQualityOptionsLayoutBinding> {

    private Context context;
    private int pos = 0;
    private OnDismisListener onDismisListener;

    public VideoQualityDialog(Context context, int pos) {
        super(context, PopupVideoQualityOptionsLayoutBinding.inflate(LayoutInflater.from(context)));
        this.context = context;
        this.pos = pos;
    }

    @Override
    public void initView() {
        binding.tvVideoQualityLow.setText(context.getString(R.string.video_quality_low) + "\n" + context.getString(R.string.video_quality_low_str));
        binding.tvVideoQualityMedium.setText(context.getString(R.string.video_quality_medium) + "\n" + context.getString(R.string.video_quality_medium_str));
        binding.tvVideoQualityHeigh.setText(context.getString(R.string.video_quality_high) + "\n" + context.getString(R.string.video_quality_high_str));

        if (pos == 0) {
            binding.tvVideoQualityLow.setBackgroundColor(context.getResources().getColor(R.color.blue_0052D9));
        } else if (pos == 1) {
            binding.tvVideoQualityMedium.setBackgroundColor(context.getResources().getColor(R.color.blue_0052D9));
        } else if (pos == 2) {
            binding.tvVideoQualityHeigh.setBackgroundColor(context.getResources().getColor(R.color.blue_0052D9));
        }

        binding.outsideDialogLayout.setOnClickListener(onClickListener);
        binding.tvVideoQualityMedium.setOnClickListener(onClickListener);
        binding.tvVideoQualityHeigh.setOnClickListener(onClickListener);
        binding.tvVideoQualityLow.setOnClickListener(onClickListener);
        binding.insideLayout.setOnClickListener(onClickListener);
    }

    private View.OnClickListener onClickListener = v -> {
        if (v.getId() == R.id.tv_video_quality_low){
            if (onDismisListener != null) {
                onDismisListener.onItemClicked(2);
            }
        }else if (v.getId() == R.id.tv_video_quality_medium){
            if (onDismisListener != null) {
                onDismisListener.onItemClicked(1);
            }
        }else if (v.getId() == R.id.tv_video_quality_heigh){
            if (onDismisListener != null) {
                onDismisListener.onItemClicked(0);
            }
        }else if (v.getId() == R.id.inside_layout){
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
