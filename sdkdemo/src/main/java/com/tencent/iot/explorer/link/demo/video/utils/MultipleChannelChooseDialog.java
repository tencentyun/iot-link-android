package com.tencent.iot.explorer.link.demo.video.utils;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.Toast;

import com.tencent.iot.explorer.link.demo.R;

import java.util.ArrayList;

public class MultipleChannelChooseDialog extends IosCenterStyleDialog implements View.OnClickListener {

    private CheckBox mCbChannel1;
    private CheckBox mCbChannel2;
    private CheckBox mCbChannel3;
    private CheckBox mCbChannel4;

    public MultipleChannelChooseDialog(Context context) {
        super(context, R.layout.popup_multiple_channel_layout);
    }

    @Override
    public void initView() {
        super.initView();

        mCbChannel1 = view.findViewById(R.id.cb_channel1);
        mCbChannel2 = view.findViewById(R.id.cb_channel2);
        mCbChannel3 = view.findViewById(R.id.cb_channel3);
        mCbChannel4 = view.findViewById(R.id.cb_channel4);

        view.findViewById(R.id.tv_multiple_cancel).setOnClickListener(this);
        view.findViewById(R.id.tv_multiple_confirm).setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tv_multiple_cancel:
                dismiss();
                break;
            case R.id.tv_multiple_confirm:
                Log.d("MultipleChannelChoose", "checkbox selected: channel0: " + mCbChannel1.isChecked()
                        + ",  channel1: " + mCbChannel2.isChecked() + ",  channel2: " + mCbChannel3.isChecked() + ",  channel3: " + mCbChannel4.isChecked());
                ArrayList<Integer> selectChannels = new ArrayList<>();
                if (mCbChannel1.isChecked()) selectChannels.add(1);
                if (mCbChannel2.isChecked()) selectChannels.add(2);
                if (mCbChannel3.isChecked()) selectChannels.add(3);
                if (mCbChannel4.isChecked()) selectChannels.add(4);
                if (selectChannels.size() != 0) {
                    if (onDismisListener != null) {
                        onDismisListener.onDismissed(selectChannels);
                    }
                    dismiss();
                } else {
                    Toast.makeText(getContext(), "请选择要观看的channels", Toast.LENGTH_SHORT).show();
                }
                break;
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
