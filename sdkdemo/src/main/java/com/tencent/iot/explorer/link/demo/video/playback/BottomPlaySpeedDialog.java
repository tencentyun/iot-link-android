package com.tencent.iot.explorer.link.demo.video.playback;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.tencent.iot.explorer.link.demo.R;
import com.tencent.iot.explorer.link.demo.databinding.PopupGridOptionsLayoutBinding;
import com.tencent.iot.explorer.link.demo.video.utils.IosCenterStyleDialog;

import java.util.ArrayList;
import java.util.List;

public class BottomPlaySpeedDialog extends IosCenterStyleDialog<PopupGridOptionsLayoutBinding> {

    private GridOptionsAdapter adapter;
    private Context context;
    private List<String> content = new ArrayList();
    private String titleStr = "";
    private int index = -1;

    public BottomPlaySpeedDialog(Context context, int index) {
        this(context);
        this.index = index;
    }

    public BottomPlaySpeedDialog(Context context) {
        super(context, PopupGridOptionsLayoutBinding.inflate(LayoutInflater.from(context)));
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
        if (TextUtils.isEmpty(titleStr)) {
            binding.tvTitle.setVisibility(View.GONE);
            binding.vSpace3.setVisibility(View.GONE);
        }

        binding.tvTitle.setText(titleStr);
        adapter = new GridOptionsAdapter(this.content, index);
        adapter.setOnItemClicked(onItemClicked);
        GridLayoutManager layoutManager = new GridLayoutManager(this.context, 4);
        binding.lvOptions.setLayoutManager(layoutManager);
        binding.lvOptions.setAdapter(adapter);

        binding.tvCancel.setOnClickListener(onClickListener);
        binding.outsideDialogLayout.setOnClickListener(onClickListener);
    }

    private GridOptionsAdapter.OnItemClicked onItemClicked = new GridOptionsAdapter.OnItemClicked() {
        @Override
        public void onItemClicked(int position, String option) {
            if (onDismisListener != null) {
                onDismisListener.onItemClicked(position);
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
