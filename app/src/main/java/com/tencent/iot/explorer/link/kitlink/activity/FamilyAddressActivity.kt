package com.tencent.iot.explorer.link.kitlink.activity

import android.Manifest.permission
import android.content.Intent
import android.content.pm.PackageManager
import android.text.TextUtils
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import com.alibaba.fastjson.JSONObject
import com.google.android.material.appbar.AppBarLayout
import com.scwang.smart.refresh.footer.ClassicsFooter
import com.scwang.smart.refresh.header.ClassicsHeader
import com.scwang.smart.refresh.layout.api.RefreshLayout
import com.scwang.smart.refresh.layout.listener.OnRefreshLoadMoreListener
import com.tencent.iot.explorer.link.R
import com.tencent.iot.explorer.link.core.auth.http.HttpCallBack
import com.tencent.iot.explorer.link.core.auth.http.HttpUtil
import com.tencent.iot.explorer.link.core.auth.util.JsonManager
import com.tencent.iot.explorer.link.core.log.L
import com.tencent.iot.explorer.link.kitlink.adapter.PostionsAdapter
import com.tencent.iot.explorer.link.kitlink.entity.LocationResp
import com.tencent.iot.explorer.link.kitlink.entity.Postion
import com.tencent.iot.explorer.link.kitlink.util.AppBarStateChangeListener
import com.tencent.map.geolocation.TencentLocation
import com.tencent.map.geolocation.TencentLocationListener
import com.tencent.map.geolocation.TencentLocationManager
import com.tencent.map.geolocation.TencentLocationRequest
import com.tencent.mapsdk.raster.model.*
import com.tencent.tencentmap.mapsdk.map.TencentMap
import kotlinx.android.synthetic.main.activity_family_address.*
import kotlinx.android.synthetic.main.activity_family_address.app_bar
import kotlinx.android.synthetic.main.activity_family_address.smart_refreshLayout
import kotlinx.android.synthetic.main.fragment_main.*
import kotlinx.android.synthetic.main.menu_back_layout.*
import kotlinx.android.synthetic.main.menu_cancel_layout.tv_title

class FamilyAddressActivity : BaseActivity(), TencentLocationListener {

    private var PAGE_SIZE = 20
    private lateinit var locationManager: TencentLocationManager
    private lateinit var tencentMap: TencentMap
    private var marker: Marker? = null
    private var addressName = ""
    private var tencentMapKey = ""
    private var postions: MutableList<Postion> = ArrayList()
    private var adapter: PostionsAdapter? = null
    private var pageIndex = 1
    private var latLng: LatLng? = null

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
        var linearLayoutManager = LinearLayoutManager(this@FamilyAddressActivity)
        adapter = PostionsAdapter(postions)
        lv_pos.layoutManager = linearLayoutManager
        lv_pos.adapter = adapter

        initMap()
        if (checkPermissions(permissions)) {
            startLocation()
        } else {
            requestPermission(permissions)
        }

        smart_refreshLayout.setEnableRefresh(false)
        smart_refreshLayout.setEnableLoadMore(true)
        smart_refreshLayout.setRefreshFooter(ClassicsFooter(this@FamilyAddressActivity))
    }

    override fun setListener() {
        iv_reset_loaction.setOnClickListener { startLocation() }
        iv_back.setOnClickListener { finish() }
        btn_add_family.setOnClickListener {
            val data = Intent()
            data.putExtra("address", addressName)
            setResult(RESULT_OK, data)
            finish()
        }
        app_bar.addOnOffsetChangedListener(object : AppBarStateChangeListener() {
            override fun onStateChanged(appBarLayout: AppBarLayout, state: State, percent: Float) {
                // 动态调整 mapview 的尺寸，保持标记处于 view 中心
                var space = mapView.width - toolbar.minimumHeight
                var lp = mapView.layoutParams
                lp.height = mapView.width - ((1 - percent) * space).toInt()
                mapView.layoutParams = lp
            }
        })

        smart_refreshLayout.setOnRefreshLoadMoreListener(object : OnRefreshLoadMoreListener {
            override fun onLoadMore(refreshLayout: RefreshLayout) {
                if (this@FamilyAddressActivity.latLng == null) {
                    refreshLayout.finishLoadMore()
                    return
                }
                requestAddress(this@FamilyAddressActivity.latLng!!, pageIndex)
            }

            override fun onRefresh(refreshLayout: RefreshLayout) {
                refreshLayout.finishRefresh()
            }
        })
    }

    private fun initMap() {
        locationManager = TencentLocationManager.getInstance(this)
        tencentMap = mapView.map
        tencentMapKey = packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA)
            .metaData.getString("TencentMapSDK") ?: ""
    }

    // 开始定位
    private fun startLocation() {
        L.e("开始定位")
        val request = TencentLocationRequest.create()
        request.requestLevel = TencentLocationRequest.REQUEST_LEVEL_POI
        when (locationManager.requestLocationUpdates(request, this)) {
            TencentLocation.ERROR_OK -> { L.e("定位成功") }
            else -> {
                L.e("定位失败")
                locationManager.removeUpdates(this)
            }
        }

        tencentMap.setOnMapCameraChangeListener(object : TencentMap.OnMapCameraChangeListener {
            override fun onCameraChangeFinish(cp: CameraPosition) {
                L.e("onCameraChangeFinish=${cp.target.latitude},${cp.target.longitude}")
                maskTag(cp.target, "")
                requestAddress(cp.target)
            }

            override fun onCameraChange(cp: CameraPosition) {}
        })
    }

    override fun onStatusUpdate(name: String, status: Int, reason: String) {}

    override fun onLocationChanged(location: TencentLocation, error: Int, desc: String) {
        L.e("location=${JsonManager.toJson(location)}")
        marker?.run {
            L.e("marker is visibly=$isInfoWindowShown")
        }
        if (TencentLocation.ERROR_OK == error) {    // 定位成功
            val target = LatLng(location.latitude, location.longitude)
            tencentMap.setCenter(target)
            maskTag(target, location.name ?: "")
            locationManager.removeUpdates(this)
        } else { } // 定位失败
    }

    private fun requestAddress(latLng: LatLng) {
        requestAddress(latLng, 1)
    }

    private fun requestAddress(latLng: LatLng, page: Int) {
        val sb = StringBuilder("https://apis.map.qq.com/ws/geocoder/v1/?location=")
        sb.append(latLng.latitude).append(",").append(latLng.longitude).append("&key=").append(tencentMapKey)
        sb.append("&get_poi=1").append("&poi_options=page_size=${PAGE_SIZE};page_index=${page}")
        Log.e("XXX", "str " + sb.toString())
        HttpUtil.get(sb.toString(), object : HttpCallBack {
            override fun onSuccess(response: String) {
                L.e("地址解析", "response=$response")
                var locationResp = JSONObject.parseObject(response, LocationResp::class.java)
                Log.e("XXX", "locationResp.status " + locationResp.status)
                Log.e("XXX", "pageIndex " + pageIndex)
                if (locationResp != null && locationResp.status == 0) {
                    addressName = locationResp.result.formatted_addresses.recommend
                    if (!TextUtils.isEmpty(addressName)) {
                        maskTag(latLng, addressName)
                    }
                    if (locationResp.result.pois.size > 0) {
                        pageIndex++
                    }
                    postions.addAll(locationResp.result.pois)
                    adapter?.notifyDataSetChanged()
                }

                if (smart_refreshLayout.isLoading) {
                    smart_refreshLayout.finishLoadMore()
                }
            }

            override fun onError(error: String) {
                if (smart_refreshLayout.isLoading) {
                    smart_refreshLayout.finishLoadMore()
                }
            }
        })
    }

    // 标记地图
    private fun maskTag(latLng: LatLng, title: String) {
        postions.clear()
        this@FamilyAddressActivity.latLng = latLng

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
