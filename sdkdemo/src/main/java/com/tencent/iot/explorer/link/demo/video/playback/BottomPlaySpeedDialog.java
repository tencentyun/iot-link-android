package com.tencent.iot.explorer.link.demo.video.playback;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.tencent.iot.explorer.link.demo.R;
import com.tencent.iot.explorer.link.demo.video.utils.IosCenterStyleDialog;

import java.util.ArrayList;
import java.util.List;

public class BottomPlaySpeedDialog extends IosCenterStyleDialog {

    private TextView cancel;
    private RecyclerView options;
    private ConstraintLayout outsideLayout;
    private GridOptionsAdapter adapter;
    private Context context;
    private List<String> content = new ArrayList();
    private TextView title;
    private View titleLine;
    private String titleStr = "";
    private int index = -1;

    public BottomPlaySpeedDialog(Context context, int index) {
        this(context);
        this.index = index;
    }

    public BottomPlaySpeedDialog(Context context) {
        super(context, R.layout.popup_grid_options_layout);
        this.context = context;
        content.add(context.getString(R.string.play_speed_0_5));
        content.add(context.getString(R.string.play_speed_0_75));
        content.add(context.getString(R.string.play_speed_1));
        content.add(context.getString(R.string.play_speed_1_25));
        content.add(context.getString(R.string.play_speed_1_5));
        content.add(context.getString(R.string.play_speed_2));
    }

    public BottomPlaySpeedDialog(Context context, String title, int index) {
        this(context);
        titleStr = title;
        this.index = index;
    }


    @Override
    public void initView() {
        outsideLayout = view.findViewById(R.id.outside_dialog_layout);
        cancel = view.findViewById(R.id.tv_cancel);
        options = view.findViewById(R.id.lv_options);
        title = view.findViewById(R.id.tv_title);
        titleLine = view.findViewById(R.id.v_space_3);

        if (TextUtils.isEmpty(titleStr)) {
            title.setVisibility(View.GONE);
            titleLine.setVisibility(View.GONE);
        }
        title.setText(titleStr);

        adapter = new GridOptionsAdapter(this.content, index);
        adapter.setOnItemClicked(onItemClicked);
        GridLayoutManager layoutManager = new GridLayoutManager(this.context, 4);
        options.setLayoutManager(layoutManager);
        options.setAdapter(adapter);

        cancel.setOnClickListener(onClickListener);
        outsideLayout.setOnClickListener(onClickListener);
    }

    private GridOptionsAdapter.OnItemClicked onItemClicked = new GridOptionsAdapter.OnItemClicked() {
        @Override
        public void onItemClicked(int postion, String option) {
            if (onDismisListener != null) {
                onDismisListener.onItemClicked(postion);
            }
            dismiss();
        }
    };

    private View.OnClickListener onClickListener = v -> {
        switch (v.getId()) { }
        dismiss();
    };

    private OnDismisListener onDismisListener;

    public interface OnDismisListener {
        void onItemClicked(int pos);
    }

    public void setOnDismisListener(OnDismisListener onDismisListener) {
        this.onDismisListener = onDismisListener;
    }

    public void show() {
        super.show();
    }

}
