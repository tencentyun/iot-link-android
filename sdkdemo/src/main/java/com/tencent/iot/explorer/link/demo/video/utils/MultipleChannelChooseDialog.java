package com.tencent.iot.explorer.link.demo.video.utils;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.Toast;

import com.tencent.iot.explorer.link.demo.R;
import com.tencent.iot.explorer.link.demo.databinding.PopupMultipleChannelLayoutBinding;

import java.util.ArrayList;

public class MultipleChannelChooseDialog extends IosCenterStyleDialog<PopupMultipleChannelLayoutBinding> implements View.OnClickListener {
    public MultipleChannelChooseDialog(Context context) {
        super(context, PopupMultipleChannelLayoutBinding.inflate(LayoutInflater.from(context)));
    }

    @Override
    public void initView() {
        super.initView();
        binding.tvMultipleCancel.setOnClickListener(this);
        binding.tvMultipleConfirm.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.tv_multiple_cancel) {
            dismiss();
        } else if (view.getId() == R.id.tv_multiple_confirm) {
            Log.d("MultipleChannelChoose", "checkbox selected: channel0: " + binding.cbChannel1.isChecked()
                    + ",  channel1: " + binding.cbChannel2.isChecked() + ",  channel2: " + binding.cbChannel3.isChecked() + ",  channel3: " + binding.cbChannel4.isChecked());
            ArrayList<Integer> selectChannels = new ArrayList<>();
            if (binding.cbChannel1.isChecked()) selectChannels.add(0);
            if (binding.cbChannel2.isChecked()) selectChannels.add(1);
            if (binding.cbChannel3.isChecked()) selectChannels.add(2);
            if (binding.cbChannel4.isChecked()) selectChannels.add(3);
            if (selectChannels.size() != 0) {
                if (onDismisListener != null) {
                    onDismisListener.onDismissed(selectChannels);
                }
                dismiss();
            } else {
                Toast.makeText(getContext(), "请选择要观看的channels", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private OnDismisListener onDismisListener;

    public interface OnDismisListener {
        void onDismissed(ArrayList<Integer> selectChannels);
    }

    public void setOnDismisListener(OnDismisListener onDismisListener) {
        this.onDismisListener = onDismisListener;
    }
}
