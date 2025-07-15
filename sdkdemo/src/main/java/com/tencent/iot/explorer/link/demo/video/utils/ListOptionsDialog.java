package com.tencent.iot.explorer.link.demo.video.utils;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.tencent.iot.explorer.link.demo.R;
import com.tencent.iot.explorer.link.demo.databinding.PopupListOptionsLayoutBinding;

import java.util.List;

public class ListOptionsDialog extends IosCenterStyleDialog<PopupListOptionsLayoutBinding> {

    private ListOptionsAdapter adapter;
    private Context context;
    private List<String> content;

    public ListOptionsDialog(Context context, List<String> content) {
        super(context, PopupListOptionsLayoutBinding.inflate(LayoutInflater.from(context)));
        this.context = context;
        this.content = content;
        adapter = new ListOptionsAdapter(this.content);
    }

    @Override
    public void initView() {
        adapter.setOnItemClicked(onItemClicked);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this.context);
        binding.lvOptions.setLayoutManager(layoutManager);
        binding.lvOptions.setAdapter(adapter);

        binding.tvCancel.setOnClickListener(onClickListener);
        binding.outsideDialogLayout.setOnClickListener(onClickListener);
    }

    private ListOptionsAdapter.OnItemClicked onItemClicked = new ListOptionsAdapter.OnItemClicked() {
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
