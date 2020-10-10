package com.tencent.iot.explorer.link.util.check;

import android.content.Context;
import android.location.LocationManager;
import android.os.Looper;
import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.tencent.map.geolocation.TencentLocation;
import com.tencent.map.geolocation.TencentLocationListener;
import com.tencent.map.geolocation.TencentLocationManager;

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

    public static void getCurrentLocation(Context context, final TencentLocationListener lisetener) {
        TencentLocationManager tencentLocationManager = TencentLocationManager.getInstance(context);
        tencentLocationManager.requestSingleFreshLocation(null, new TencentLocationListener() {

            @Override
            public void onLocationChanged(TencentLocation tencentLocation, int error, String reason) {
                if (lisetener != null) {
                    lisetener.onLocationChanged(tencentLocation, error, reason);
                }
            }

            @Override
            public void onStatusUpdate(String s, int i, String s1) {
                if (lisetener != null) {
                    lisetener.onStatusUpdate(s, i, s1);
                }
            }
        }, Looper.getMainLooper());

    }
}
