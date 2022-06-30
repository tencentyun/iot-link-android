package com.tencent.iot.explorer.link.kitlink.util;

import android.content.Context;
import android.content.Intent;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

public class VideoUtils {
    public static String VIDEO_RESUME = "videoResume";

    public static void sendVideoBroadcast(Context context, int value) {
        Intent intent = new Intent("android.intent.action.CART_BROADCAST");
        intent.putExtra(VIDEO_RESUME, value);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
        context.sendBroadcast(intent);
    }
}
