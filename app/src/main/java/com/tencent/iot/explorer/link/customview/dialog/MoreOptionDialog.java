package com.tencent.iot.explorer.link.customview.dialog;

import android.content.Context;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.tencent.iot.explorer.link.R;
import com.tencent.iot.explorer.link.customview.dialog.adapter.AddOptionsAdapter;
import com.tencent.iot.explorer.link.customview.dialog.adapter.MoreOptionAdapter;
import com.tencent.iot.explorer.link.customview.dialog.entity.AddItem;
import com.tencent.iot.explorer.link.customview.dialog.entity.DevOption;
import com.tencent.iot.explorer.link.customview.dialog.entity.OptionMore;

import java.util.ArrayList;
import java.util.List;

public class MoreOptionDialog extends IosCenterStyleDialog {

    private RecyclerView options;
    private ConstraintLayout outsideLayout;
    private ConstraintLayout insideLayout;
    private MoreOptionAdapter adapter;
    private Context context;
    private List<DevOption> content;
    private String title;
    private TextView go;
    private TextView titleTv;

    public MoreOptionDialog(Context context, OptionMore optionMore) {
        super(context, R.layout.popup_more_options_layout, false);
        this.context = context;
        this.title = optionMore.getTitle();
        this.content = optionMore.getOptions();
        adapter = new MoreOptionAdapter(this.content);
    }

    @Override
    public void initView() {
        outsideLayout = view.findViewById(R.id.outside_dialog_layout);
        insideLayout = view.findViewById(R.id.inside_layout);
        options = view.findViewById(R.id.lv_options);
        go = view.findViewById(R.id.tv_space);
        titleTv = view.findViewById(R.id.tv_title);

        titleTv.setText(title);
        GridLayoutManager layoutManager = new GridLayoutManager(this.context, 4);
        options.setLayoutManager(layoutManager);
        options.setAdapter(adapter);
        adapter.setOnItemClicked(onItemClicked);

        outsideLayout.setOnClickListener(onClickListener);
        insideLayout.setOnClickListener(onClickListener);
        go.setOnClickListener(onClickListener);
    }

    private MoreOptionAdapter.OnItemClicked onItemClicked = new MoreOptionAdapter.OnItemClicked() {
        @Override
        public void onItemClicked(int postion) {
            if (onDismisListener != null) {
                onDismisListener.onItemClicked(postion, content.get(postion));
            }
            dismiss();
        }
    };

    private View.OnClickListener onClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            switch (v.getId())
            {
                case R.id.inside_layout:
                    return;
                case R.id.tv_space:
                    if (onDismisListener != null) {
                        onDismisListener.onGoClicked();
                    }
                    break;
            }
            dismiss();
            if (onDismisListener != null) {
                onDismisListener.onDismissed();
            }
        }
    };

    private OnDismisListener onDismisListener;

    public interface OnDismisListener {
        void onItemClicked(int pos, DevOption devOption);
        void onDismissed();
        void onGoClicked();
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
