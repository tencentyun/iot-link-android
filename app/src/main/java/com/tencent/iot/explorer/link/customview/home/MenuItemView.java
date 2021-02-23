package com.tencent.iot.explorer.link.customview.home;

import android.content.Context;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

public class MenuItemView extends RelativeLayout {

    protected TextView tvTitle;
    protected ImageView ivIcon;
    private Context mContext;

    public MenuItemView(Context context) {
        super(context);
        mContext = context;
        initView();
    }

    public MenuItemView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        initView();
    }

    public MenuItemView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        initView();
    }

    private void initView() {
        tvTitle = new TextView(getContext());
        tvTitle.setId(View.generateViewId());
        tvTitle.setGravity(Gravity.CENTER_HORIZONTAL);
        tvTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11);
        LayoutParams lptv = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        lptv.bottomMargin = (int) getContext().getResources().getDisplayMetrics().density * 3;
        lptv.topMargin = (int) getContext().getResources().getDisplayMetrics().density * 5;
        lptv.addRule(ALIGN_PARENT_BOTTOM);
        lptv.addRule(CENTER_HORIZONTAL);
        addView(tvTitle, lptv);
        ivIcon = new ImageView(getContext());
        LayoutParams lpiv = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        lpiv.topMargin = (int) getContext().getResources().getDisplayMetrics().density * 4;
        lpiv.addRule(ABOVE, tvTitle.getId());
        lpiv.addRule(CENTER_HORIZONTAL);
        // dp 转 px
        int size = (int)(mContext.getResources().getDisplayMetrics().density * 24 + 0.5);
        lpiv.height = size;
        lpiv.width = size;
        ivIcon.setAdjustViewBounds(true);
        addView(ivIcon, lpiv);
    }
}
