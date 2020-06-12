package com.tencent.iot.explorer.link.util;

import android.app.Activity;
import android.util.DisplayMetrics;

/**
 * 窗口管理工具
 * 可使用方法：
 * 1.获取手机屏幕的高度和宽度及像素的密度
 */
public class WindowManage {

    /**
     * 获取手机屏幕的高度和宽度
     */
    public static Dispaly getDispaly(Activity activity) {
        DisplayMetrics dm = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(dm);
        int width = dm.widthPixels; //像素的宽度数
        int height = dm.heightPixels;//像素的高度数
        float density = dm.density; //像素的密度：1dp=?px;
        return new Dispaly(width, height, density);
    }

    /**
     * 屏幕高度的数据实体类
     */
    public static class Dispaly {
        public int width;
        public int height;
        public float density;

        public Dispaly() {
        }

        public Dispaly(int width, int height, float density) {
            this.width = width;
            this.height = height;
            this.density = density;
        }

    }

}
