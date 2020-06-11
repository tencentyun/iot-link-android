package com.tencent.iot.explorer.link.kitlink.util;

import android.content.Context;
import android.net.wifi.WifiManager;

public class WifiUtil {

    private static int getFrequency(Context context) {
        WifiManager wifi = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        return wifi.getConnectionInfo().getFrequency();
    }

    /**
     * 判断是否是 2.4GHz 的 wifi
     * @param context 上下文
     * @return true：wifi 是 2.4GHz， false：wifi 不是 2.4GHz
     */
    public static boolean is24GHz(Context context) {
        int freq = getFrequency(context);
        return freq > 2400 && freq < 2500;
    }

    /**
     * 判断是否是 5GHz 的 wifi
     * @param context 上下文
     * @return true：wifi 是 5GHz， false：wifi 不是 5GHz
     */
    public static boolean is5GHz(Context context) {
        int freq = getFrequency(context);
        return freq > 4900 && freq < 5900;
    }
}
