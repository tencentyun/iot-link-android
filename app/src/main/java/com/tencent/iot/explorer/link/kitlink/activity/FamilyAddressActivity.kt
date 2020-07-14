package com.tencent.iot.explorer.link.kitlink.activity

import android.Manifest.permission
import android.content.Intent
import android.content.pm.PackageManager
import android.text.TextUtils
import com.alibaba.fastjson.JSON
import com.tencent.iot.explorer.link.R
import com.tencent.iot.explorer.link.core.log.L
import com.tencent.iot.explorer.link.kitlink.util.HttpCallBack
import com.tencent.iot.explorer.link.kitlink.util.HttpUtil
import com.tencent.iot.explorer.link.kitlink.util.JsonManager
import com.tencent.map.geolocation.TencentLocation
import com.tencent.map.geolocation.TencentLocationListener
import com.tencent.map.geolocation.TencentLocationManager
import com.tencent.map.geolocation.TencentLocationRequest
import com.tencent.mapsdk.raster.model.*
import com.tencent.tencentmap.mapsdk.map.TencentMap
import kotlinx.android.synthetic.main.activity_family_address.*
import kotlinx.android.synthetic.main.menu_cancel_layout.*


/**
 * 家庭位置
 */
class FamilyAddressActivity : BaseActivity(), TencentLocationListener {

    private lateinit var locationManager: TencentLocationManager
    private lateinit var tencentMap: TencentMap
    private var marker: Marker? = null
    private var addressName = ""

    private val BEIJING = LatLng(39.906016, 116.397657)
    private var tencentMapKey = ""

    var permissions = arrayOf(
        permission.ACCESS_COARSE_LOCATION,
        permission.READ_PHONE_STATE,
        permission.WRITE_EXTERNAL_STORAGE
    )

    override fun getContentView(): Int {
        return R.layout.activity_family_address
    }

    override fun initView() {
        tv_title.text = getString(R.string.family_address)
        initMap()
        if (checkPermissions(permissions)) {
            startLocation()
        } else {
            requestPermission(permissions)
        }
    }

    override fun setListener() {
        tv_back.setOnClickListener { finish() }
        btn_add_family.setOnClickListener {
            val data = Intent()
            data.putExtra("address", addressName)
            setResult(200, data)
            finish()
        }
    }

    /**
     * 初始化地图设置
     */
    private fun initMap() {
        locationManager = TencentLocationManager.getInstance(this)
        tencentMap = mapView.map
        tencentMapKey = packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA)
            .metaData.getString("TencentMapSDK") ?: ""
    }

    /**
     * 开始定位
     */
    private fun startLocation() {
        L.e("开始定位")
        val request = TencentLocationRequest.create()
        request.requestLevel = TencentLocationRequest.REQUEST_LEVEL_POI
        request.isAllowCache = true
        when (locationManager.requestLocationUpdates(request, this)) {
            TencentLocation.ERROR_OK -> {
                L.e("定位成功")
            }
            else -> {
                L.e("定位失败")
                locationManager.removeUpdates(this)
            }
        }
        tencentMap.setOnMapCameraChangeListener(object : TencentMap.OnMapCameraChangeListener {
            override fun onCameraChangeFinish(cp: CameraPosition) {
                L.e("onCameraChangeFinish=${cp.target.latitude},${cp.target.longitude}")
                maskTag(cp.target, "")
//                requestAddress(cp.target)
            }

            override fun onCameraChange(cp: CameraPosition) {
                L.e("onCameraChange=${cp.target.latitude},${cp.target.longitude}")
                maskTag(cp.target, "")
            }
        })
    }

    override fun onStatusUpdate(name: String, status: Int, reason: String) {
    }

    override fun onLocationChanged(location: TencentLocation, error: Int, desc: String) {
        L.e("location=${JsonManager.toJson(location)}")
        marker?.run {
            L.e("marker is visibly=$isInfoWindowShown")
        }
        if (TencentLocation.ERROR_OK == error) {
            // 定位成功
            val target = LatLng(location.latitude, location.longitude)
            tencentMap.setCenter(target)
            maskTag(target, location.name ?: "")
            locationManager.removeUpdates(this)
        } else {
            // 定位失败
        }
    }

    private fun requestAddress(latLng: LatLng) {
        val sb = StringBuilder("https://apis.map.qq.com/ws/geocoder/v1/?location=")
        sb.append(latLng.latitude).append(",").append(latLng.longitude).append("&key=")
            .append(tencentMapKey)
        HttpUtil.get(sb.toString(), object : HttpCallBack {
            override fun onSuccess(response: String) {
                L.e("地址解析", "response=$response")
                val result = JSON.parseObject(response)
                if (result.getIntValue("status") == 0) {
                    result.getJSONObject("result").run {
                        getJSONObject("formatted_addresses").let {
                            addressName = it.getString("recommend")
                            if (!TextUtils.isEmpty(addressName)) {
                                maskTag(latLng, addressName)
                            }
                        }
                    }
                }
            }

            override fun onError(error: String) {
            }
        })
    }

    /**
     * 标记地图
     */
    private fun maskTag(latLng: LatLng, title: String) {
        runOnUiThread {
            addressName = title
            if (tencentMap.zoomLevel < tencentMap.maxZoomLevel) {
                tencentMap.setZoom(tencentMap.maxZoomLevel)
            }
            marker?.remove()
            marker = tencentMap.addMarker(
                MarkerOptions().position(latLng)
                    .title(title)
                    .anchor(0.5f, 0.5f)
                    .icon(BitmapDescriptorFactory.defaultMarker())
                    .draggable(false)
            )
            marker?.showInfoWindow()
        }
    }

    override fun permissionAllGranted() {
        startLocation()
    }

    override fun onResume() {
        mapView.onResume()
        super.onResume()
    }

    override fun onPause() {
        mapView.onPause()
        super.onPause()
    }

    override fun onStop() {
        mapView.onStop()
        super.onStop()
    }

    override fun onDestroy() {
        mapView.onDestroy()
        super.onDestroy()
    }
}
