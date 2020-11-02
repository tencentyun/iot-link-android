package com.tencent.iot.explorer.link.customview.dialog;

import android.content.Context;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.tencent.iot.explorer.link.R;
import com.tencent.iot.explorer.link.customview.dialog.adapter.ListOptionsAdapter;

import java.util.ArrayList;
import java.util.List;

public class ListOptionsDialog extends IosCenterStyleDialog {

    private TextView cancel;
    private RecyclerView options;
    private ConstraintLayout outsideLayout;
    private ListOptionsAdapter adapter;
    private Context context;
    private List<String> content;

    public ListOptionsDialog(Context context, List<String> content) {
        super(context, R.layout.popup_list_options_layout);
        this.context = context;
        this.content = content;
        adapter = new ListOptionsAdapter(this.content);
    }

    @Override
    public void initView() {
        outsideLayout = view.findViewById(R.id.outside_dialog_layout);
        cancel = view.findViewById(R.id.tv_cancel);
        options = view.findViewById(R.id.lv_options);

        adapter.setOnItemClicked(onItemClicked);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this.context);
        options.setLayoutManager(layoutManager);
        options.setAdapter(adapter);

        cancel.setOnClickListener(onClickListener);
        outsideLayout.setOnClickListener(onClickListener);
    }

    private ListOptionsAdapter.OnItemClicked onItemClicked = new ListOptionsAdapter.OnItemClicked() {
        @Override
        public void onItemClicked(int postion, String option) {
            if (onDismisListener != null) {
                onDismisListener.onItemClicked(postion);
            }
            dismiss();
        }
    };

    private View.OnClickListener onClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            switch (v.getId())
            {

            }
            dismiss();
        }
    };

    private OnDismisListener onDismisListener;

    public interface OnDismisListener {
        void onItemClicked(int pos);
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
