package com.tencent.iot.explorer.link.customview.dialog;

import android.content.Context;
import android.os.Build;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
    private HorizontalProgressBarWithNumber bar;
    private Context context;
    private List<KeyBooleanValue> content = new ArrayList<>();
    private TextView title;
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

        bar.setCircleColor(context.getResources().getColor(R.color.white));
        bar.setCircleTextColor(context.getResources().getColor(R.color.blue_006EFF));
        bar.setUnReachedColor(context.getResources().getColor(R.color.grey_E1E4E9));
        bar.setReachedBarColor(context.getResources().getColor(R.color.blue_006EFF));

        if (type == 1) {
            options.setVisibility(View.GONE);
            barLayout.setVisibility(View.VISIBLE);
            bar.setMax(modeInt.getMax());
            bar.setMin(modeInt.getMin());
            bar.setProgress(progress);

        } else {
            options.setVisibility(View.VISIBLE);
            barLayout.setVisibility(View.GONE);
        }
    }

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
                    if (progress >= bar.getMax()) {
                        return;
                    }
                    progress += modeInt.getStep();
                    bar.setProgress(progress);
                    return;
                case R.id.iv_decrease:
                    if (progress <= 0) {
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
