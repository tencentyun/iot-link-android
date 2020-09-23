package com.tencent.iot.explorer.link.kitlink.util

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityCompat.requestPermissions

object LocationUtils {

    private var permissions = arrayOf(
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.ACCESS_FINE_LOCATION
    )

    @SuppressLint("MissingPermission")
    open fun getLastKnownLocation(context: Context): Location? {
        val locationManager = context.applicationContext.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val providers = locationManager.getProviders(true)
        var lastestLocation: Location? = null

        for (provider in providers) {
            val location = locationManager.getLastKnownLocation(provider) ?: continue
            if (lastestLocation == null || location.accuracy < lastestLocation.accuracy) {
                lastestLocation = location
            }
        }
        return lastestLocation
    }

    fun requestPermissions(context: Activity, backCode: Int) {
        requestPermissions(context, permissions, backCode)
    }

    fun checkPermissions(context: Context, permissions: Array<String>): Boolean {
        for (permission in permissions) {
            if (ActivityCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_DENIED) {
                Log.e("lurs", permission + "未申请")
                return false
            }
            Log.e("lurs", permission + "已申请")
        }
        return true
    }
}