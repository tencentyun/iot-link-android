package com.tencent.iot.explorer.link.customview.verticaltab;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.text.TextUtils;
import android.view.Gravity;
import android.widget.Checkable;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.Px;
import androidx.annotation.RequiresApi;

import com.tencent.iot.explorer.link.core.utils.Utils;


public class TabView extends FrameLayout implements Checkable, ITabView {

    private Context mContext;
    private TextView mTitle;
    private TabTitle mTabTitle;
    private boolean mChecked;
    private Drawable mDefaultBackground;

    public TabView(Context context) {
        super(context);
        mContext = context;
        mTabTitle = new TabTitle.Builder().build();
        initView();
        int[] attrs;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            attrs = new int[]{android.R.attr.selectableItemBackgroundBorderless};
        } else {
            attrs = new int[]{android.R.attr.selectableItemBackground};
        }
        TypedArray a = mContext.getTheme().obtainStyledAttributes(attrs);
        mDefaultBackground = a.getDrawable(0);
        a.recycle();
        setDefaultBackground();
    }

    @Override
    public TabView getTabView() {
        return this;
    }

    private void initView() {
        setMinimumHeight(Utils.INSTANCE.dp2px(mContext,25));
        if (mTitle == null) {
            mTitle = new TextView(mContext);
            LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT);
            params.gravity = Gravity.CENTER;
            mTitle.setLayoutParams(params);
            this.addView(mTitle);
        }
        initTitleView();
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public void setPaddingRelative(@Px int start, @Px int top, @Px int end, @Px int bottom) {
        mTitle.setPaddingRelative(start, top, end, bottom);
    }

    @Override
    public void setPadding(@Px int left, @Px int top, @Px int right, @Px int bottom) {
        mTitle.setPadding(left, top, right, bottom);
    }

    private void initTitleView() {
        mTitle.setTextColor(isChecked() ? mTabTitle.getColorSelected() : mTabTitle.getColorNormal());
        mTitle.setTextSize(mTabTitle.getTitleTextSize());
        mTitle.setText(mTabTitle.getContent());
        mTitle.setGravity(Gravity.CENTER);
        mTitle.setEllipsize(TextUtils.TruncateAt.END);
    }

    @Override
    public TabView setTitle(TabTitle title) {
        if (title != null) {
            mTabTitle = title;
        }
        initTitleView();
        return this;
    }

    @Override
    public TabView setBackground(int resId) {
        if (resId == 0) {
            setDefaultBackground();
        } else if (resId <= 0) {
            setBackground(null);
        } else {
            super.setBackgroundResource(resId);
        }
        return this;
    }

    @Override
    public TabTitle getTitle() {
        return mTabTitle;
    }

    @Override
    public void setBackground(Drawable background) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            super.setBackground(background);
        } else {
            super.setBackgroundDrawable(background);
        }
    }

    @Override
    public void setBackgroundResource(int resid) {
        setBackground(resid);
    }

    private void setDefaultBackground() {
        if (getBackground() != mDefaultBackground) {
            setBackground(mDefaultBackground);
        }
    }

    @Override
    public void setChecked(boolean checked) {
        mChecked = checked;
        setSelected(checked);
        refreshDrawableState();
        mTitle.setTextColor(checked ? mTabTitle.getColorSelected() : mTabTitle.getColorNormal());
    }

    @Override
    public boolean isChecked() {
        return mChecked;
    }

    @Override
    public void toggle() {
        setChecked(!mChecked);
    }
}