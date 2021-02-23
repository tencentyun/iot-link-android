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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.tencent.iot.explorer.link.R;
import com.tencent.iot.explorer.link.customview.dialog.adapter.AddOptionsAdapter;
import com.tencent.iot.explorer.link.customview.dialog.adapter.ListOptionsAdapter;
import com.tencent.iot.explorer.link.customview.dialog.entity.AddItem;

import java.util.ArrayList;
import java.util.List;

public class AddOptionsDialog extends IosCenterStyleDialog {

    private ImageView addImg;
    private RecyclerView options;
    private ConstraintLayout outsideLayout;
    private ConstraintLayout insideLayout;
    private AddOptionsAdapter adapter;
    private Context context;
    private List<AddItem> content;

    public AddOptionsDialog(Context context) {
        super(context, R.layout.popup_add_options_layout, false);
        this.context = context;
        content = new ArrayList<>();
        AddItem opt1 = new AddItem();
        opt1.setResId(R.mipmap.add_option_add_dev);
        opt1.setOption(getContext().getString(R.string.add_option_add_dev));
        AddItem opt2 = new AddItem();
        opt2.setResId(R.mipmap.add_option_scan_dev);
        opt2.setOption(getContext().getString(R.string.add_option_scan_dev));
        AddItem opt3 = new AddItem();
        opt3.setResId(R.mipmap.add_option_add_task);
        opt3.setOption(getContext().getString(R.string.add_option_add_task));
        content.add(opt1);
        content.add(opt2);
        content.add(opt3);
        adapter = new AddOptionsAdapter(this.content);
    }

    @Override
    public void initView() {
        outsideLayout = view.findViewById(R.id.outside_dialog_layout);
        insideLayout = view.findViewById(R.id.inside_layout);
        addImg = view.findViewById(R.id.iv_main_add);
        options = view.findViewById(R.id.lv_options);

        GridLayoutManager layoutManager = new GridLayoutManager(this.context, 3);
        options.setLayoutManager(layoutManager);
        options.setAdapter(adapter);
        adapter.setOnItemClicked(onItemClicked);

        outsideLayout.setOnClickListener(onClickListener);
        addImg.setOnClickListener(onClickListener);
        insideLayout.setOnClickListener(onClickListener);

        RotateAnimation rotate = new RotateAnimation(0f, 45f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        LinearInterpolator lin = new LinearInterpolator();
        rotate.setInterpolator(lin);
        rotate.setDuration(200);//设置动画持续周期
        rotate.setFillAfter(true);//动画执行完后是否停留在执行完的状态
        addImg.setAnimation(rotate);
    }

    private AddOptionsAdapter.OnItemClicked onItemClicked = new AddOptionsAdapter.OnItemClicked() {
        @Override
        public void onItemClicked(int postion) {
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
                case R.id.inside_layout:
                    return;
            }
            dismiss();
            if (onDismisListener != null) {
                onDismisListener.onDismissed();
            }
        }
    };

    private OnDismisListener onDismisListener;

    public interface OnDismisListener {
        void onItemClicked(int pos);
        void onDismissed();
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
