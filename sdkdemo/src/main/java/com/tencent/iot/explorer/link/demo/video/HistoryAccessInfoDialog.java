package com.tencent.iot.explorer.link.demo.video;

import android.content.Context;
import android.os.Handler;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import com.aigestudio.wheelpicker.WheelPicker;
import com.alibaba.fastjson.JSONArray;
import com.tencent.iot.explorer.link.demo.R;
import com.tencent.iot.explorer.link.core.utils.SharePreferenceUtil;
import com.tencent.iot.explorer.link.demo.databinding.PopupHistoryAccessLayoutBinding;
import com.tencent.iot.explorer.link.demo.video.utils.IosCenterStyleDialog;
import com.tencent.iot.video.link.consts.VideoConst;

import java.util.ArrayList;
import java.util.List;

public class HistoryAccessInfoDialog extends IosCenterStyleDialog<PopupHistoryAccessLayoutBinding> implements View.OnClickListener {
    private List<String> accessIds = new ArrayList<>();
    private List<AccessInfo> accessInfos = new ArrayList<>();
    private Handler handler = new Handler();

    public HistoryAccessInfoDialog(Context context) {
        super(context, PopupHistoryAccessLayoutBinding.inflate(LayoutInflater.from(context)));
    }

    @Override
    public void initView() {
        binding.wheelAccessInfoPicker.setOnItemSelectedListener(selectedListener);
        binding.tvOk.setOnClickListener(this);
        binding.tvCancel.setOnClickListener(this);
        binding.tipLayout.setOnClickListener(this);
        binding.outsideDialogLayout.setOnClickListener(this);

        binding.wheelAccessInfoPicker.setVisibility(View.GONE);
        String jsonArrStr = SharePreferenceUtil.getString(getContext(), VideoConst.VIDEO_CONFIG, VideoConst.VIDEO_ACCESS_INFOS);
        if (TextUtils.isEmpty(jsonArrStr)) return;
        accessInfos = JSONArray.parseArray(jsonArrStr, AccessInfo.class);
        if (accessInfos == null || accessInfos.size() <= 0) return;

        for (AccessInfo accessInfo : accessInfos) {
            accessIds.add(accessInfo.getAccessId());
        }
        binding.wheelAccessInfoPicker.setVisibility(View.VISIBLE);
        binding.wheelAccessInfoPicker.setData(accessIds);
        handler.postDelayed(() -> binding.wheelAccessInfoPicker.setSelectedItemPosition(accessInfos.size() - 1), 10);
    }

    private WheelPicker.OnItemSelectedListener selectedListener = (picker, data, position) -> { };

    @Override
    public void onClick(View v) {
        if (v.getId()==R.id.tv_cancel|| v.getId()==R.id.outside_dialog_layout){
            if (onDismisListener != null) {
                onDismisListener.onCancelClicked();
            }
        }else if (v.getId()==R.id.tv_ok){
            if (onDismisListener != null && accessInfos != null &&
                    binding.wheelAccessInfoPicker.getCurrentItemPosition() < accessInfos.size()) {
                onDismisListener.onOkClicked(accessInfos.get(binding.wheelAccessInfoPicker.getCurrentItemPosition()));
            }
        }else if (v.getId()==R.id.tip_layout){
            return;
        }
        dismiss();
    }

    private volatile OnDismisListener onDismisListener;

    public interface OnDismisListener {
        void onOkClicked(AccessInfo accessInfo);
        void onCancelClicked();
    }

    public void setOnDismissListener(OnDismisListener onDismisListener) {
        this.onDismisListener = onDismisListener;
    }

}
