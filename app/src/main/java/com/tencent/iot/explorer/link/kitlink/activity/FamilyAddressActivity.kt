package com.tencent.iot.explorer.link.kitlink.activity

import android.Manifest.permission
import android.content.Intent
import android.content.pm.PackageManager
import android.text.TextUtils
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.JSONException
import com.alibaba.fastjson.JSONObject
import com.google.android.material.appbar.AppBarLayout
import com.scwang.smart.refresh.footer.ClassicsFooter
import com.scwang.smart.refresh.layout.api.RefreshLayout
import com.scwang.smart.refresh.layout.listener.OnRefreshLoadMoreListener
import com.tencent.iot.explorer.link.R
import com.tencent.iot.explorer.link.T
import com.tencent.iot.explorer.link.core.auth.callback.MyCallback
import com.tencent.iot.explorer.link.core.auth.http.HttpCallBack
import com.tencent.iot.explorer.link.core.auth.http.HttpUtil
import com.tencent.iot.explorer.link.core.auth.response.BaseResponse
import com.tencent.iot.explorer.link.core.auth.util.JsonManager
import com.tencent.iot.explorer.link.core.log.L
import com.tencent.iot.explorer.link.kitlink.adapter.PostionsAdapter
import com.tencent.iot.explorer.link.kitlink.consts.CommonField
import com.tencent.iot.explorer.link.kitlink.entity.Address
import com.tencent.iot.explorer.link.kitlink.entity.LocationResp
import com.tencent.iot.explorer.link.kitlink.entity.Postion
import com.tencent.iot.explorer.link.kitlink.util.AppBarStateChangeListener
import com.tencent.iot.explorer.link.kitlink.util.HttpRequest
import com.tencent.iot.explorer.link.kitlink.util.RequestCode
import com.tencent.map.geolocation.TencentLocation
import com.tencent.map.geolocation.TencentLocationListener
import com.tencent.map.geolocation.TencentLocationManager
import com.tencent.map.geolocation.TencentLocationRequest
import com.tencent.mapsdk.raster.model.BitmapDescriptorFactory
import com.tencent.mapsdk.raster.model.LatLng
import com.tencent.mapsdk.raster.model.Marker
import com.tencent.mapsdk.raster.model.MarkerOptions
import com.tencent.tencentmap.mapsdk.map.TencentMap
import kotlinx.android.synthetic.main.activity_family_address.*
import kotlinx.android.synthetic.main.menu_back_layout.*
import kotlinx.android.synthetic.main.menu_cancel_layout.tv_title

class FamilyAddressActivity : BaseActivity(), TencentLocationListener {

    private var PAGE_SIZE = 20
    private lateinit var locationManager: TencentLocationManager
    private lateinit var tencentMap: TencentMap
    private var marker: Marker? = null
    private var tencentMapKey = ""
    private var postions: MutableList<Postion> = ArrayList()
    private var adapter: PostionsAdapter? = null
    private var pageIndex = 1
    private var latLng: LatLng? = null
    private var defaultAddress = ""
    private var familyId = ""
    private var familyName = ""

    var permissions = arrayOf(
        permission.ACCESS_COARSE_LOCATION,
        permission.READ_PHONE_STATE,
        permission.WRITE_EXTERNAL_STORAGE
    )

    override fun getContentView(): Int {
        return R.layout.activity_family_address
    }

    override fun initView() {
        tv_title.text = getString(R.string.map_select_postion)
        var linearLayoutManager = LinearLayoutManager(this@FamilyAddressActivity)
        adapter = PostionsAdapter(postions)
        lv_pos.layoutManager = linearLayoutManager
        lv_pos.adapter = adapter

        var bundel = intent.getBundleExtra(CommonField.ADDRESS)
        if (bundel != null) {
            defaultAddress = bundel.getString(CommonField.ADDRESS, "")
            familyId = bundel.getString(CommonField.FAMILY_ID, "")
            familyName = bundel.getString(CommonField.FAMILY_NAME, "")
        }

        initMap()
        if (checkPermissions(permissions)) {
            if (TextUtils.isEmpty(defaultAddress)) {
                startLocation()
            } else {
                tagPostionByAddress(defaultAddress)
            }
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
            if (TextUtils.isEmpty(familyId)) { // 新增房间的方式
                if (adapter != null) {
                    val data = Intent()
                    data.putExtra(CommonField.ADDRESS, JSON.toJSONString(adapter!!.selectPostion))
                    setResult(RESULT_OK, data)
                }
                finish()

            } else { // 修改房间地址的方式
                if (adapter != null || adapter!!.selectPostion == null) {
                    var address = Address()
                    address.name = adapter?.selectPostion!!.title
                    address.address = adapter?.selectPostion!!.address
                    address.latitude = adapter?.selectPostion!!.location!!.lat
                    address.longitude = adapter?.selectPostion!!.location!!.lng
                    address.city = adapter?.selectPostion!!.ad_info!!.city
                    HttpRequest.instance.modifyFamily(familyId, familyName, JSON.toJSONString(address),
                    object: MyCallback {
                        override fun fail(msg: String?, reqCode: Int) {
                            T.show(msg?:"")
                        }

                        override fun success(response: BaseResponse, reqCode: Int) {
                            when (reqCode) {
                                RequestCode.modify_family -> {
                                    if (response.isSuccess()) {
                                        val data = Intent()
                                        data.putExtra(CommonField.ADDRESS, JSON.toJSONString(adapter!!.selectPostion))
                                        setResult(RESULT_OK, data)
                                        finish()
                                    } else {
                                        T.show(response.msg)
                                    }
                                }
                            }
                        }
                    })
                }
            }

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
                if (this@FamilyAddressActivity.latLng == null || pageIndex > 20) {
                    refreshLayout.finishLoadMore()
                    return
                }
                requestAddress(this@FamilyAddressActivity.latLng!!, pageIndex)
            }

            override fun onRefresh(refreshLayout: RefreshLayout) {
                refreshLayout.finishRefresh()
            }
        })

        adapter?.setOnItemClicked(object: PostionsAdapter.OnItemClicked {
            override fun onItemClicked(pos: Int) {
                adapter?.selectPos = pos
                adapter?.notifyDataSetChanged()
            }
        })
        tencentMap.setOnMapClickListener(object: TencentMap.OnMapClickListener {
            override fun onMapClick(p0: LatLng?) {
                if (p0 == null) return
                maskTag(p0, "")
                requestAddress(p0, pageIndex)
            }
        })
    }

    private fun initMap() {
        locationManager = TencentLocationManager.getInstance(this)
        tencentMap = mapView.map
        tencentMapKey = packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA)
            .metaData.getString("TencentMapSDK") ?: ""
    }

    private fun tagPostionByAddress(address: String) {
        try {
            var loc = JSONObject.parseObject(address, Address::class.java)
            if (loc.latitude == 0f && loc.longitude == 0f) {
                startLocation()
                return
            }
            val target = LatLng(loc.latitude.toDouble(), loc.longitude.toDouble())
            tencentMap.setCenter(target)
            maskTag(target, "")
            requestAddress(target, pageIndex)
        } catch (e: JSONException) {
            e.printStackTrace()
            startLocation()
        }
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
            requestAddress(target, pageIndex)
            locationManager.removeUpdates(this)
        }
    }

    private fun requestAddress(latLng: LatLng, page: Int) {
        val sb = StringBuilder("https://apis.map.qq.com/ws/geocoder/v1/?location=")
        sb.append(latLng.latitude).append(",").append(latLng.longitude).append("&key=").append(tencentMapKey)
        sb.append("&get_poi=1").append("&poi_options=page_size=${PAGE_SIZE};page_index=${page}")
        HttpUtil.get(sb.toString(), object : HttpCallBack {
            override fun onSuccess(response: String) {
                L.e("地址解析", "response=$response")
                var locationResp = JSONObject.parseObject(response, LocationResp::class.java)
                if (locationResp != null && locationResp.status == 0) {

                    // 查询到数据，且获取到的数据量少于总数据量
                    if (locationResp.result.pois.size > 0 &&
                            (postions.size + locationResp.result.pois.size) < locationResp.result.poi_count) {
                        pageIndex++
                        postions.addAll(locationResp.result.pois)
                    }
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
        pageIndex = 1
        this@FamilyAddressActivity.latLng = latLng

        runOnUiThread {
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
        if (TextUtils.isEmpty(defaultAddress)) {
            startLocation()
        } else {
            tagPostionByAddress(defaultAddress)
        }
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
