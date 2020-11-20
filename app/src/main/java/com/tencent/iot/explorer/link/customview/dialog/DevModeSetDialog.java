package com.tencent.iot.explorer.link.customview.dialog;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.jaygoo.widget.OnRangeChangedListener;
import com.jaygoo.widget.RangeSeekBar;
import com.tencent.iot.explorer.link.R;
import com.tencent.iot.explorer.link.customview.HorizontalProgressBarWithNumber;
import com.tencent.iot.explorer.link.customview.dialog.adapter.DevModeOptionsAdapter;
import com.tencent.iot.explorer.link.customview.dialog.adapter.ListOptionsAdapter;
import com.tencent.iot.explorer.link.kitlink.entity.DevModeInfo;
import com.tencent.iot.explorer.link.kitlink.entity.ModeInt;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class DevModeSetDialog extends IosCenterStyleDialog {

    private TextView cancel;
    private TextView save;
    private RecyclerView options;
    private ConstraintLayout outsideLayout;
    private ConstraintLayout insideLayout;
    private ConstraintLayout barLayout;
    private DevModeOptionsAdapter adapter;
    private RangeSeekBar bar;
    private Context context;
    private List<KeyBooleanValue> content = new ArrayList<>();
    private TextView title;
    private TextView currentProgress;
    private String titleStr;
    private ImageView increase;
    private ImageView decrease;
    private ModeInt modeInt;
    private int progress;
    private int type = 0; // 0 显示选项列表   1 显示拖动控制条

    public int getProgress() {
        return progress;
    }

    public int getCurrentIndex() {
        return adapter.getCurrentIndex();
    }

    public DevModeSetDialog(Context context, List<KeyBooleanValue> content, String titleStr, int index) {
        super(context, R.layout.popup_dev_mode_set_layout);
        this.context = context;
        this.content = content;
        this.titleStr = titleStr;
        adapter = new DevModeOptionsAdapter(this.content);
        adapter.setIndex(index);
        type = 0;
    }

    public DevModeSetDialog(Context context, String title, ModeInt modeInt) {
        super(context, R.layout.popup_dev_mode_set_layout);
        this.context = context;
        this.titleStr = title;
        adapter = new DevModeOptionsAdapter(this.content);
        type = 1;
        this.modeInt = modeInt;
        progress = modeInt.getStart();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void initView() {
        outsideLayout = view.findViewById(R.id.outside_dialog_layout);
        insideLayout = view.findViewById(R.id.layout_inside);
        barLayout = view.findViewById(R.id.layout_bar);
        cancel = view.findViewById(R.id.tv_cancel);
        options = view.findViewById(R.id.lv_options);
        save = view.findViewById(R.id.tv_ok);
        title = view.findViewById(R.id.tv_dialog_title);
        bar = view.findViewById(R.id.bar_score_progrss);
        decrease = view.findViewById(R.id.iv_decrease);
        increase = view.findViewById(R.id.iv_increase);
        currentProgress = view.findViewById(R.id.progress);


        title.setText(titleStr);
        adapter.setOnItemClicked(onItemClicked);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this.context);
        options.setLayoutManager(layoutManager);
        options.setAdapter(adapter);

        cancel.setOnClickListener(onClickListener);
        outsideLayout.setOnClickListener(onClickListener);
        save.setOnClickListener(onClickListener);
        increase.setOnClickListener(onClickListener);
        decrease.setOnClickListener(onClickListener);
        insideLayout.setOnClickListener(onClickListener);

        if (type == 1) {
            options.setVisibility(View.GONE);
            barLayout.setVisibility(View.VISIBLE);
            bar.setRange(modeInt.getMin(), modeInt.getMax());
            bar.setProgress(progress);
            bar.setIndicatorText(String.valueOf(progress));
            bar.setOnRangeChangedListener(onRangeChangedListener);
        } else {
            options.setVisibility(View.VISIBLE);
            barLayout.setVisibility(View.GONE);
        }
    }

    private OnRangeChangedListener onRangeChangedListener = new OnRangeChangedListener() {

        @Override
        public void onRangeChanged(RangeSeekBar view, float leftValue, float rightValue, boolean isFromUser) {
            progress = (int)leftValue;
            view.getLeftSeekBar().setIndicatorText(String.valueOf((int)leftValue));
        }

        @Override
        public void onStartTrackingTouch(RangeSeekBar view, boolean isLeft) { }

        @Override
        public void onStopTrackingTouch(RangeSeekBar view, boolean isLeft) { }
    };

    private DevModeOptionsAdapter.OnItemClicked onItemClicked = new DevModeOptionsAdapter.OnItemClicked() {
        @Override
        public void onItemClicked(int postion, KeyBooleanValue option) {
            adapter.setIndex(postion);
            adapter.notifyDataSetChanged();
        }
    };

    private View.OnClickListener onClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            switch (v.getId())
            {
                case R.id.tv_ok:
                    if (onDismisListener != null && adapter != null) {
                        onDismisListener.onSaveClicked();
                    }
                    break;
                case R.id.tv_cancel:
                    if (onDismisListener != null) {
                        onDismisListener.onCancelClicked();
                    }
                    break;
                case R.id.iv_increase:
                    if (progress >= bar.getMaxProgress()) {
                        return;
                    }

                    progress += modeInt.getStep();
                    bar.setProgress(progress);
                    return;
                case R.id.iv_decrease:
                    if (progress <= bar.getMinProgress()) {
                        return;
                    }

                    progress -= modeInt.getStep();
                    bar.setProgress(progress);
                    return;
                case R.id.layout_inside:
                    return;
            }
            dismiss();
        }
    };

    private OnDismisListener onDismisListener;

    public interface OnDismisListener {
        void onSaveClicked();
        void onCancelClicked();
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
