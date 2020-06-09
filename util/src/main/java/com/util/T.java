package com.util;

import android.content.Context;
import android.widget.Toast;

/**
 * Toast类
 * Created by THINK on 2017/12/27.
 */

public class T {
    private static Context context;

    public static Context getContext() {
        return context;
    }

    public static void setContext(Context context) {
        T.context = context;
    }

    public static void show(String text) {
        if (context != null) {
            Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
        }
    }

    public static void showLonger(String text) {
        if (context != null) {
            Toast.makeText(context, text, Toast.LENGTH_LONG).show();
        }
    }
}
