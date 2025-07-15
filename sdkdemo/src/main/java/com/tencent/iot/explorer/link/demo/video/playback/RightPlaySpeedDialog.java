package com.tencent.iot.explorer.link.demo.video.playback;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.tencent.iot.explorer.link.demo.R;
import com.tencent.iot.explorer.link.demo.databinding.PopupRightOptionsLayoutBinding;
import com.tencent.iot.explorer.link.demo.video.utils.IosCenterStyleDialog;

import java.util.ArrayList;
import java.util.List;

public class RightPlaySpeedDialog extends IosCenterStyleDialog<PopupRightOptionsLayoutBinding> {

    private Context context;
    private int pos = 0;
    private OnDismisListener onDismisListener;
    private RightOptionsAdapter adapter;
    private List<String> optionsData = new ArrayList();

    public RightPlaySpeedDialog(Context context, int pos) {
        super(context, PopupRightOptionsLayoutBinding.inflate(LayoutInflater.from(context)));
        this.context = context;
        this.pos = pos;
    }

    @Override
    public void initView() {
        optionsData.add(context.getString(R.string.play_speed_0_5));
        optionsData.add(context.getString(R.string.play_speed_0_75));
        optionsData.add(context.getString(R.string.play_speed_1));
        optionsData.add(context.getString(R.string.play_speed_1_25));
        optionsData.add(context.getString(R.string.play_speed_1_5));
        optionsData.add(context.getString(R.string.play_speed_2));
        adapter = new RightOptionsAdapter(context, optionsData, pos);
        adapter.setOnItemClicked(onItemClicked);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this.context);
        binding.lvOptions.setLayoutManager(layoutManager);
        binding.lvOptions.setAdapter(adapter);

        binding.outsideDialogLayout.setOnClickListener(onClickListener);
        binding.insideLayout.setOnClickListener(onClickListener);
    }

    private RightOptionsAdapter.OnItemClicked onItemClicked = (postion, option) -> {
        if (onDismisListener != null) {
            onDismisListener.onItemClicked(postion);
        }
        dismiss();
    };

    private View.OnClickListener onClickListener = v -> {
        if (v.getId() == R.id.inside_layout) {
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
