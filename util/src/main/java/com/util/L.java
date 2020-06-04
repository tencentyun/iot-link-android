package com.util;

import android.util.Log;

/**
 * 我的日志
 * Created by THINK on 2017/12/27.
 */

public class L {

    private static boolean isLog = false;
    private static final String TAG = "**********";

    public static void isLog(boolean isLog) {
        L.isLog = isLog;
    }

    public static void d(String msg) {
        d(TAG, msg);
    }

    public static void d(String tag, String msg) {
        if (isLog) {
            //因为String的length是字符数量不是字节数量所以为了防止中文字符过多，
            //  把4*1024的MAX字节打印长度改为2001字符数
            int max_str_length = 2001 - tag.length();
            //大于4000时
            while (msg.length() > max_str_length) {
                Log.d(tag, msg.substring(0, max_str_length));
                msg = msg.substring(max_str_length);
            }
            //剩余部分
            Log.d(tag, msg);
        }
    }

    public static void e(String msg) {
        e(TAG, msg);
    }

    public static void e(String tag, String msg) {
        if (isLog) {
            //因为String的length是字符数量不是字节数量所以为了防止中文字符过多，
            //  把4*1024的MAX字节打印长度改为2001字符数
            int max_str_length = 2001 - tag.length();
            //大于4000时
            while (msg.length() > max_str_length) {
                Log.e(tag, msg.substring(0, max_str_length));
                msg = msg.substring(max_str_length);
            }
            //剩余部分
            Log.e(tag, msg);
        }
    }
}
