package com.tencent.iot.explorer.link.customview.dialog;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.tencent.iot.explorer.link.R;
import com.tencent.iot.explorer.link.customview.dialog.IosCenterStyleDialog;
import com.tencent.iot.explorer.link.customview.dialog.adapter.ListOptionsAdapter;

import java.util.List;

public class SuccessToastDialog extends IosCenterStyleDialog {

    public SuccessToastDialog(Context context) {
        super(context, R.layout.popup_toast_success_layout);
    }

    @Override
    public void initView() { }

    public void show() {
        if (!isShowing()) {
            super.show();
        }
    }
}
