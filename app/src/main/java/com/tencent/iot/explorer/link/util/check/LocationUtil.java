package com.tencent.iot.explorer.link.util.check;

import android.content.Context;
import android.location.LocationManager;

/**
 * 位置相关的工具类
 */
public class LocationUtil {
    public static boolean isLocationServiceEnable(Context context) {
        if (context != null) {
            LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
            if (locationManager != null) {
                boolean gps = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
                // 通过网络定位的方式
//                boolean network = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
//                return gps || network;
                return gps;
            }
        }
        return false;
    }
}
