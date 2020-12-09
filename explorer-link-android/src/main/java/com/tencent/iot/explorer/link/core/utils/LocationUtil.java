package com.tencent.iot.explorer.link.core.utils;

import android.content.Context;
import android.location.LocationManager;
import android.os.Looper;
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
                return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
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
