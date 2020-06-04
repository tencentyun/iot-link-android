package com.util.keyboard;

/**
 * Created by smg on 2017/3/31.
 */

public interface OnSoftKeyBoardListener {


    /**
     * 软键盘显示后调用此方法
     *
     * @param newY      软键盘显示时控件inputView的y坐标位置
     * @param keyHeight 软键盘的高度
     */
    void onShowSoftKeyboard(int newY, int keyHeight);

    /**
     * 软键盘隐藏后调用此方法
     *
     * @param myOldY 软键盘隐藏时控件inputView的y坐标位置
     */
    void onHideSoftKeyboard(int myOldY);

}
