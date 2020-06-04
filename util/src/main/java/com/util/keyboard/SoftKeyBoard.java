package com.util.keyboard;

import android.app.Activity;
import android.content.res.Resources;
import android.graphics.Rect;
import android.os.Handler;
import android.view.View;
import android.view.Window;

import com.util.L;

/**
 * Created by smg on 2017/6/16.
 */

public class SoftKeyBoard {

    private Activity activity;

    private boolean isKeyShowListener = false;
    private OnSoftKeyBoardListener listener;

    public SoftKeyBoard(Activity activity) {
        this.activity = activity;
    }

    public boolean isKeyShowListener() {
        return isKeyShowListener;
    }

    public void setKeyShowListener(boolean keyShowListener) {
        isKeyShowListener = keyShowListener;
    }

    private Handler handler = new Handler();
    private boolean newStatus = true;
    private boolean currentStatus = false;
    private boolean waitHandler = false;
    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            if (listener != null && waitHandler && newStatus != currentStatus) {
                waitHandler = false;
                currentStatus = newStatus;
                if (currentStatus) {
                    listener.onShowSoftKeyboard(newY, keyHeight);
                } else {
                    listener.onHideSoftKeyboard(oldY);
                }
            } else {
                waitHandler = false;
            }
        }
    };

    private int newY = 0, oldY = 0, keyHeight = 0;

    /**
     * 监听器
     *
     * @param inputView
     * @param listener
     */
    public void setSoftKeyBoardShowListener(final View inputView, final OnSoftKeyBoardListener listener) {
        this.listener = listener;
        activity.getWindow().getDecorView().addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                if (isKeyShowListener) {
                    //获取View可见区域的bottom
                    Rect rect = new Rect();
                    activity.getWindow().getDecorView().getWindowVisibleDisplayFrame(rect);
                    int titleHeight = getTitleBarHeight();
                    int statusHeight = getStatusHeight();
                    int navigationBarHeight = getNavigationBarHeight();
                    keyHeight = bottom - rect.bottom - navigationBarHeight;
                    newY = rect.bottom - inputView.getHeight();
                    oldY = inputView.getHeight();
                    if (bottom != 0 && oldBottom != 0 && keyHeight <= 0) {
                        newStatus = false;
//                        listener.onHideSoftKeyboard(oldY);
                    } else {
                        newStatus = true;
//                        listener.onShowSoftKeyboard(newY, keyHeight);
                    }
                    tab();
                } else {
                    isKeyShowListener = true;
                }
            }
        });
    }

    private void tab() {
        if (!waitHandler) {
            waitHandler = true;
            handler.postDelayed(runnable, 100);
        }
    }

    public void destroy() {
        handler.removeCallbacks(runnable);
    }

    /**
     * 获得状态栏的高度
     *
     * @return
     */
    private int getNavigationBarHeight() {
        Resources resources = activity.getResources();
        int resourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android");
        int height = resources.getDimensionPixelSize(resourceId);
        return height;
    }

    /**
     * 获得导航栏的高度
     *
     * @return
     */
    public int getStatusHeight() {
        Rect rect = new Rect();
        activity.getWindow().getDecorView().getWindowVisibleDisplayFrame(rect);
        return rect.top;
    }

    /**
     * 获得标题栏的高度
     *
     * @return
     */
    public int getTitleBarHeight() {
        int height = activity.getWindow().findViewById(Window.ID_ANDROID_CONTENT).getTop();
        return height;
    }

}
