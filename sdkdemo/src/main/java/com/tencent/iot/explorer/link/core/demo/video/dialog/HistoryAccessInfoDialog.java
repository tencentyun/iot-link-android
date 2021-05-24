package com.tencent.iot.explorer.link.core.demo.video.dialog;

import android.content.Context;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.aigestudio.wheelpicker.WheelPicker;
import com.alibaba.fastjson.JSONArray;
import com.tencent.iot.explorer.link.core.demo.R;
import com.tencent.iot.explorer.link.core.demo.video.entity.AccessInfo;
import com.tencent.iot.explorer.link.core.utils.SharePreferenceUtil;
import com.tencent.iot.video.link.consts.VideoConst;

import java.util.ArrayList;
import java.util.List;

public class HistoryAccessInfoDialog extends IosCenterStyleDialog implements View.OnClickListener {
    private TextView okBtn;
    private TextView cancelBtn;
    private WheelPicker accessHistoryPicker;
    private ConstraintLayout outsideLayout;
    private ConstraintLayout dialogLayout;
    private List<String> accessIds = new ArrayList<>();
    private List<AccessInfo> accessInfos = new ArrayList<>();
    private Handler handler = new Handler();

    public HistoryAccessInfoDialog(Context context) {
        super(context, R.layout.popup_history_access_layout);
    }

    @Override
    public void initView() {
        accessHistoryPicker = view.findViewById(R.id.wheel_access_info_picker);
        okBtn = view.findViewById(R.id.tv_ok);
        cancelBtn = view.findViewById(R.id.tv_cancel);
        dialogLayout = view.findViewById(R.id.tip_layout);
        outsideLayout = view.findViewById(R.id.outside_dialog_layout);

        accessHistoryPicker.setOnItemSelectedListener(selectedListener);
        okBtn.setOnClickListener(this);
        cancelBtn.setOnClickListener(this);
        dialogLayout.setOnClickListener(this);
        outsideLayout.setOnClickListener(this);

        accessHistoryPicker.setVisibility(View.GONE);
        String jsonArrStr = SharePreferenceUtil.getString(getContext(), VideoConst.VIDEO_CONFIG, VideoConst.VIDEO_ACCESS_INFOS);
        if (TextUtils.isEmpty(jsonArrStr)) return;
        accessInfos = JSONArray.parseArray(jsonArrStr, AccessInfo.class);
        if (accessInfos == null || accessInfos.size() <= 0) return;

        for (AccessInfo accessInfo : accessInfos) {
            accessIds.add(accessInfo.getAccessId());
        }
        accessHistoryPicker.setVisibility(View.VISIBLE);
        accessHistoryPicker.setData(accessIds);
        handler.postDelayed(() -> accessHistoryPicker.setSelectedItemPosition(accessInfos.size() - 1), 10);
    }

    private WheelPicker.OnItemSelectedListener selectedListener = (picker, data, position) -> { };

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_cancel:
            case R.id.outside_dialog_layout:
                if (onDismisListener != null) {
                    onDismisListener.onCancelClicked();
                }
                break;
            case R.id.tv_ok:
                if (onDismisListener != null && accessInfos != null &&
                        accessHistoryPicker.getCurrentItemPosition() < accessInfos.size()) {
                    onDismisListener.onOkClicked(accessInfos.get(accessHistoryPicker.getCurrentItemPosition()));
                }
                break;
            case R.id.tip_layout:
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
