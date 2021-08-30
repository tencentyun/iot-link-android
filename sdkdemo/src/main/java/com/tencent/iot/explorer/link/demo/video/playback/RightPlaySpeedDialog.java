package com.tencent.iot.explorer.link.demo.video.playback;

import android.content.Context;
import android.view.View;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.tencent.iot.explorer.link.demo.R;
import com.tencent.iot.explorer.link.demo.video.utils.IosCenterStyleDialog;

import java.util.ArrayList;
import java.util.List;

public class RightPlaySpeedDialog extends IosCenterStyleDialog {

    private ConstraintLayout outsideLayout;
    private ConstraintLayout insideLayout;
    private RecyclerView options;
    private Context context;
    private int pos = 0;
    private OnDismisListener onDismisListener;
    private RightOptionsAdapter adapter;
    private List<String> optionsData = new ArrayList();

    public RightPlaySpeedDialog(Context context, int pos) {
        super(context, R.layout.popup_right_options_layout);
        this.context = context;
        this.pos = pos;
    }

    @Override
    public void initView() {
        outsideLayout = view.findViewById(R.id.outside_dialog_layout);
        insideLayout = view.findViewById(R.id.inside_layout);
        options = view.findViewById(R.id.lv_options);

        optionsData.add(context.getString(R.string.play_speed_0_5));
        optionsData.add(context.getString(R.string.play_speed_0_75));
        optionsData.add(context.getString(R.string.play_speed_1));
        optionsData.add(context.getString(R.string.play_speed_1_25));
        optionsData.add(context.getString(R.string.play_speed_1_5));
        optionsData.add(context.getString(R.string.play_speed_2));
        adapter = new RightOptionsAdapter(context, optionsData, pos);
        adapter.setOnItemClicked(onItemClicked);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this.context);
        options.setLayoutManager(layoutManager);
        options.setAdapter(adapter);

        outsideLayout.setOnClickListener(onClickListener);
        insideLayout.setOnClickListener(onClickListener);
    }

    private RightOptionsAdapter.OnItemClicked onItemClicked = (postion, option) -> {
        if (onDismisListener != null) {
            onDismisListener.onItemClicked(postion);
        }
        dismiss();
    };

    private View.OnClickListener onClickListener = v -> {
        switch (v.getId()) {
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
