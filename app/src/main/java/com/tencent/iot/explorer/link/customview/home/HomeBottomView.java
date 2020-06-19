package com.tencent.iot.explorer.link.customview.home;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;

import java.util.LinkedList;

/**
 * 底部控件
 * <p>
 * Created by lurensheng on 2018/4/25 0025.
 */

public class HomeBottomView extends LinearLayout {


    private LinkedList<BottomItemEntity> bottomList = new LinkedList();
    private LinkedList<MenuItemView> bottomViews = new LinkedList<>();
    private OnItemClickListener onItemClickListener;
    private int currentPosition = 0, previewPosition = -1;


    public HomeBottomView(Context context) {
        super(context);
    }

    public HomeBottomView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public HomeBottomView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    /**
     * 清除菜单
     */
    public void clearMenu() {
        bottomList.clear();
    }

    /**
     * 添加底部菜单
     *
     * @param entity
     */
    public HomeBottomView addMenu(BottomItemEntity entity) {
        bottomList.add(entity);
        return this;
    }

    /**
     * 显示底部菜单
     */
    public void showMenu() {
        if (bottomList != null) {
            LayoutParams lp = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT);
            lp.weight = 1;
            for (int i = 0; i < bottomList.size(); i++) {
                MenuItemView view = new MenuItemView(getContext());
                setListener(view, i);
                view.tvTitle.setText(bottomList.get(i).title);
                view.tvTitle.setTextColor(bottomList.get(i).normalColor);
                view.ivIcon.setImageResource(bottomList.get(i).normalSrc);
                if (i == 0) {
                    view.tvTitle.setTextColor(bottomList.get(i).hoverColor);
                    view.ivIcon.setImageResource(bottomList.get(i).hoverSrc);
                }
                addView(view, lp);
                bottomViews.add(view);
            }
        }
    }

    /**
     * 销毁控件
     */
    public void destroy() {
        bottomList.clear();
        bottomViews.clear();
        bottomList = null;
        bottomViews = null;
    }

    /**
     * 设置监听器
     *
     * @param itemView
     * @param position
     */
    private void setListener(final MenuItemView itemView, final int position) {
        itemView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentPosition == position)
                    return;
                refreshView(itemView, position);
                previewPosition = currentPosition;
                currentPosition = position;
                if (onItemClickListener != null)
                    onItemClickListener.onItemClickListener(itemView, position, previewPosition);
            }
        });
    }

    private void refreshView(MenuItemView itemView, int position) {
        for (int i = 0; i < bottomList.size(); i++) {
            MenuItemView view = bottomViews.get(i);
            view.tvTitle.setTextColor(bottomList.get(i).normalColor);
            view.ivIcon.setImageResource(bottomList.get(i).normalSrc);
        }
        itemView.tvTitle.setTextColor(bottomList.get(position).hoverColor);
        itemView.ivIcon.setImageResource(bottomList.get(position).hoverSrc);
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public interface OnItemClickListener {
        void onItemClickListener(MenuItemView menuItemView, int position, int previewPosition);
    }

}
