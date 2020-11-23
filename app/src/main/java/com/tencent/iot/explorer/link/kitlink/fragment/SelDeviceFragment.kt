package com.tencent.iot.explorer.link.kitlink.fragment

import android.content.Context
import android.content.Intent
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.alibaba.fastjson.JSON
import com.squareup.picasso.Picasso
import com.tencent.iot.explorer.link.App
import com.tencent.iot.explorer.link.R
import com.tencent.iot.explorer.link.T
import com.tencent.iot.explorer.link.core.auth.entity.DeviceEntity
import com.tencent.iot.explorer.link.core.auth.response.BaseResponse
import com.tencent.iot.explorer.link.core.auth.response.DeviceListResponse
import com.tencent.iot.explorer.link.core.log.L
import com.tencent.iot.explorer.link.customview.FullGridView
import com.tencent.iot.explorer.link.kitlink.activity.DeviceModeInfoActivity
import com.tencent.iot.explorer.link.kitlink.adapter.DeviceAdapter
import com.tencent.iot.explorer.link.kitlink.consts.CommonField
import com.tencent.iot.explorer.link.kitlink.entity.CategoryDeviceEntity
import com.tencent.iot.explorer.link.kitlink.entity.RecommDeviceEntity
import com.tencent.iot.explorer.link.kitlink.entity.RouteType
import com.tencent.iot.explorer.link.kitlink.util.HttpRequest
import com.tencent.iot.explorer.link.kitlink.util.MyCallback
import com.tencent.iot.explorer.link.kitlink.util.RequestCode
import com.tencent.iot.explorer.link.mvp.IPresenter
import kotlinx.android.synthetic.main.fragment_devices.*
import kotlinx.android.synthetic.main.fragment_smart_log.*


class SelDeviceFragment() : BaseFragment(), MyCallback {

    private var mContext : Context? = null
    private var devicesGridView : RecyclerView? = null
    private var roomId = ""
    private val deviceList = ArrayList<DeviceEntity>() //App.data.deviceList
    var deviceListEnd = false
    private var routeType = RouteType.MANUAL_TASK_ROUTE

    constructor(c: Context, routeType: Int, roomId: String):this() {
        mContext = c
        this.roomId = roomId
        this.routeType = routeType
    }

    override fun getPresenter(): IPresenter? {
        return null
    }

    override fun getContentView(): Int {
        return R.layout.fragment_select_binded_devices
    }

    override fun startHere(view: View) {
        devicesGridView = view.findViewById(R.id.gv_devices)
        tv_recommend.visibility = View.GONE
        gv_recommend_devices.visibility = View.GONE
        split_line.visibility = View.GONE
        loadDeviceList()
    }

    fun loadDeviceList() {
        if (TextUtils.isEmpty(App.data.getCurrentFamily().FamilyId)) return
        if (deviceListEnd) return
        HttpRequest.instance.deviceList(
            App.data.getCurrentFamily().FamilyId,
            roomId,
            deviceList.size,
            this
        )
    }

    private var onItemClicked = object : DeviceAdapter.OnItemClicked {
        override fun onItemClicked(pos: Int, dev: DeviceEntity) {
            if (deviceList != null && deviceList.get(pos) != null) {
                var intent = Intent(context!!, DeviceModeInfoActivity::class.java)
                intent.putExtra(CommonField.EXTRA_PRODUCT_ID, JSON.toJSONString(deviceList.get(pos)))
                intent.putExtra(CommonField.EXTRA_ROUTE_TYPE, routeType)
                startActivity(intent)
            }
        }
    }

    override fun fail(msg: String?, reqCode: Int) {
        L.e(msg ?: "")
        T.show(msg)
    }

    override fun success(response: BaseResponse, reqCode: Int) {
        when (reqCode) {
            RequestCode.device_list -> {
                if (response.isSuccess()) {
                    response.parse(DeviceListResponse::class.java)?.run {
                        deviceList.addAll(DeviceList)
                        if (Total >= 0) {
                            deviceListEnd = deviceList.size >= Total
                        }

                        if (!deviceListEnd) {
                            HttpRequest.instance.deviceList(App.data.getCurrentFamily().FamilyId,
                                    roomId, deviceList.size, this@SelDeviceFragment)

                        }

                        if (deviceListEnd && roomId == "") {
                            val layoutManager = GridLayoutManager(context, 3)
                            devicesGridView?.setLayoutManager(layoutManager)
                            var adapter = context?.let { DeviceAdapter(deviceList) }
                            adapter?.setOnItemClicked(onItemClicked)
                            devicesGridView?.adapter = adapter
                        }
                    }
                }
            }
        }
    }
}