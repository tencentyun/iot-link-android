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

import com.jaygoo.widget.OnRangeChangedListener;
import com.jaygoo.widget.RangeSeekBar;
import com.tencent.iot.explorer.link.R;
import com.tencent.iot.explorer.link.customview.dialog.adapter.DevModeOptionsAdapter;
import com.tencent.iot.explorer.link.kitlink.entity.ModeInt;
import com.tencent.iot.explorer.link.core.auth.entity.OpValue;
import com.tencent.iot.explorer.link.kitlink.util.Utils;

import java.util.ArrayList;
import java.util.List;

public class DevModeSetDialog extends IosCenterStyleDialog {

    private TextView cancel;
    private TextView save;
    private TextView tvGr;
    private TextView tvEq;
    private TextView tvLt;
    private RecyclerView options;
    private ConstraintLayout outsideLayout;
    private ConstraintLayout insideLayout;
    private ConstraintLayout barLayout;
    private ConstraintLayout eqLayout;
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
    private float progress;
    private int type = 0; // 0 显示选项列表   1 显示拖动控制条

    public float getProgress() {
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
        eqLayout = view.findViewById(R.id.layout_op_btn);
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
        tvGr = view.findViewById(R.id.tv_gr);
        tvEq = view.findViewById(R.id.tv_eq);
        tvLt = view.findViewById(R.id.tv_lt);

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
        tvGr.setOnClickListener(onClickListener);
        tvEq.setOnClickListener(onClickListener);
        tvLt.setOnClickListener(onClickListener);

        if (type == 1) {
            options.setVisibility(View.GONE);
            barLayout.setVisibility(View.VISIBLE);
            bar.setRange(modeInt.getMin(), modeInt.getMax());
//            bar.setIndicatorText(progress + modeInt.getUnit());
            bar.setOnRangeChangedListener(onRangeChangedListener);
            if (progress <= modeInt.getMin()) progress = modeInt.getMin();
            if (progress >= modeInt.getMax()) progress = modeInt.getMax();
            bar.setProgress(progress);
            if (modeInt.getShowOp()) {
                eqLayout.setVisibility(View.VISIBLE);
                resetStartEqBtnStatus();
            } else {
                eqLayout.setVisibility(View.GONE);
            }

        } else {
            options.setVisibility(View.VISIBLE);
            barLayout.setVisibility(View.GONE);
        }
    }

    private void resetStartEqBtnStatus() {
        tvGr.setBackground(null);
        tvGr.setTextColor(getContext().getResources().getColor(R.color.black_15161A));
        tvEq.setBackground(null);
        tvEq.setTextColor(getContext().getResources().getColor(R.color.black_15161A));
        tvLt.setBackground(null);
        tvLt.setTextColor(getContext().getResources().getColor(R.color.black_15161A));

        if (modeInt.getOp().equals(OpValue.OP_GR)) {
            tvGr.setBackgroundResource(R.drawable.background_circle_bule);
            tvGr.setTextColor(getContext().getResources().getColor(R.color.white));
        } else if (modeInt.getOp().equals(OpValue.OP_EQ)) {
            tvEq.setBackgroundResource(R.drawable.background_circle_bule);
            tvEq.setTextColor(getContext().getResources().getColor(R.color.white));
        } else if (modeInt.getOp().equals(OpValue.OP_LT)) {
            tvLt.setBackgroundResource(R.drawable.background_circle_bule);
            tvLt.setTextColor(getContext().getResources().getColor(R.color.white));
        }
    }

    private OnRangeChangedListener onRangeChangedListener = new OnRangeChangedListener() {

        @Override
        public void onRangeChanged(RangeSeekBar view, float leftValue, float rightValue, boolean isFromUser) {
            progress = leftValue;
            if (!modeInt.getIfInteger()) {

                int len = Utils.Companion.length(modeInt.getStep());
                view.getLeftSeekBar().setIndicatorText(String.format("%." + len + "f", leftValue) + modeInt.getUnit());
            } else {
                view.getLeftSeekBar().setIndicatorText((int)leftValue + modeInt.getUnit());
            }
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
                case R.id.tv_gr:
                    modeInt.setOp(OpValue.OP_GR);
                    resetStartEqBtnStatus();
                    return;
                case R.id.tv_eq:
                    modeInt.setOp(OpValue.OP_EQ);
                    resetStartEqBtnStatus();
                    return;
                case R.id.tv_lt:
                    modeInt.setOp(OpValue.OP_LT);
                    resetStartEqBtnStatus();
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
