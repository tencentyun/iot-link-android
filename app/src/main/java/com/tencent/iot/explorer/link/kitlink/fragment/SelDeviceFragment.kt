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
import com.tencent.iot.explorer.link.kitlink.consts.CommonField
import com.tencent.iot.explorer.link.kitlink.entity.CategoryDeviceEntity
import com.tencent.iot.explorer.link.kitlink.entity.RecommDeviceEntity
import com.tencent.iot.explorer.link.kitlink.entity.RouteType
import com.tencent.iot.explorer.link.kitlink.util.HttpRequest
import com.tencent.iot.explorer.link.kitlink.util.MyCallback
import com.tencent.iot.explorer.link.kitlink.util.RequestCode
import com.tencent.iot.explorer.link.mvp.IPresenter
import kotlinx.android.synthetic.main.fragment_devices.*


class SelDeviceFragment() : BaseFragment(), MyCallback {

    private var mContext : Context? = null
    private var devicesGridView : FullGridView? = null
    private var roomId = ""
    private val deviceList = ArrayList<DeviceEntity>() //App.data.deviceList
    var deviceListEnd = false
    private var deviceTotal = 0
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
        return R.layout.fragment_devices
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

    private var onItemClicked = object : GridAdapter.OnItemClicked {
        override fun onItemClicked(pos: Int) {
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
                            deviceTotal = Total
                            deviceListEnd = deviceList.size >= Total
                        }

                        if (deviceListEnd && roomId == "") {
                            //到底时开始加载共享的设备列表,并且是在全部设备这个房间时
                            var adapter = context?.let { GridAdapter(it, deviceList) }
                            adapter?.setOnItemClicked(onItemClicked)
                            devicesGridView?.adapter = adapter
                        }
                    }
                }
            }
        }
    }

    class GridAdapter : BaseAdapter {
        var deviceList : List<DeviceEntity>? = null
        var context : Context? = null

        constructor(contxt : Context, list : List<DeviceEntity>) {
            context = contxt
            deviceList = list
        }

        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            val viewHolder : ViewHolder
            val retView : View
            if (convertView == null) {
                viewHolder = ViewHolder()
                retView = LayoutInflater.from(parent?.context). inflate(R.layout.device_item, parent, false)
                viewHolder.image = retView.findViewById(R.id.iv_device_icon)
                viewHolder.text = retView.findViewById(R.id.tv_device_name)
                retView.tag = viewHolder
            } else {
                viewHolder = convertView.tag as ViewHolder
                retView = convertView
            }

            if (TextUtils.isEmpty(deviceList?.get(position)?.IconUrl)) {
                Picasso.get().load(R.drawable.device_placeholder).into(viewHolder.image)
            } else {
                Picasso.get().load(deviceList?.get(position)?.IconUrl).into(viewHolder.image)
            }

            viewHolder.text.setText(deviceList?.get(position)?.getAlias())
            retView.setOnClickListener{
                if (onItemClicked != null) {
                    onItemClicked!!.onItemClicked(position)
                }
            }
            return retView
        }

        override fun getItem(position: Int): Any? {
            return deviceList?.get(position)
        }

        override fun getItemId(position: Int): Long {
            return position.toLong()
        }

        override fun getCount(): Int {
            if (deviceList == null) {
                return 0
            }
            return deviceList!!.size
        }

        inner class ViewHolder {
            lateinit var image: ImageView
            lateinit var text: TextView
        }

        private var onItemClicked: OnItemClicked? = null

        interface OnItemClicked {
            fun onItemClicked(pos: Int)
        }

        fun setOnItemClicked(onItemClicked: OnItemClicked) {
            this.onItemClicked = onItemClicked
        }
    }

}