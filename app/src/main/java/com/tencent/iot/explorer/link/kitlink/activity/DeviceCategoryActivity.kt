package com.tencent.iot.explorer.link.kitlink.activity

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.text.TextUtils
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.animation.LinearInterpolator
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.JSONObject
import com.example.qrcode.Constant
import com.tencent.iot.explorer.link.App
import com.tencent.iot.explorer.link.R
import com.tencent.iot.explorer.link.T
import com.tencent.iot.explorer.link.core.auth.callback.MyCallback
import com.tencent.iot.explorer.link.core.auth.response.BaseResponse
import com.tencent.iot.explorer.link.core.auth.util.JsonManager
import com.tencent.iot.explorer.link.core.link.entity.BleDevBindCondition
import com.tencent.iot.explorer.link.core.link.entity.BleDevOtaUpdateResponse
import com.tencent.iot.explorer.link.core.link.entity.BleDevSignResult
import com.tencent.iot.explorer.link.core.link.entity.BleDevice
import com.tencent.iot.explorer.link.core.link.entity.BleDeviceFirmwareVersion
import com.tencent.iot.explorer.link.core.link.entity.BleDeviceInfo
import com.tencent.iot.explorer.link.core.link.entity.BleDeviceProperty
import com.tencent.iot.explorer.link.core.link.entity.BleWifiConnectInfo
import com.tencent.iot.explorer.link.core.link.entity.DeviceInfo
import com.tencent.iot.explorer.link.core.link.entity.TrtcDeviceInfo
import com.tencent.iot.explorer.link.core.link.exception.TCLinkException
import com.tencent.iot.explorer.link.core.link.listener.BleDeviceConnectionListener
import com.tencent.iot.explorer.link.core.link.service.BleConfigService
import com.tencent.iot.explorer.link.core.log.L
import com.tencent.iot.explorer.link.core.utils.Utils
import com.tencent.iot.explorer.link.customview.MyScrollView
import com.tencent.iot.explorer.link.customview.dialog.PermissionDialog
import com.tencent.iot.explorer.link.customview.recyclerview.CRecyclerView
import com.tencent.iot.explorer.link.customview.verticaltab.ITabView
import com.tencent.iot.explorer.link.customview.verticaltab.TabAdapter
import com.tencent.iot.explorer.link.customview.verticaltab.TabView
import com.tencent.iot.explorer.link.customview.verticaltab.VerticalTabLayout
import com.tencent.iot.explorer.link.kitlink.adapter.BleDeviceAdapter
import com.tencent.iot.explorer.link.kitlink.consts.CommonField
import com.tencent.iot.explorer.link.kitlink.entity.BindDevResponse
import com.tencent.iot.explorer.link.kitlink.entity.GatewaySubDevsResp
import com.tencent.iot.explorer.link.kitlink.entity.ProdConfigDetailEntity
import com.tencent.iot.explorer.link.kitlink.entity.ProductEntity
import com.tencent.iot.explorer.link.kitlink.entity.ProductGlobal
import com.tencent.iot.explorer.link.kitlink.entity.ProductsEntity
import com.tencent.iot.explorer.link.kitlink.fragment.DeviceFragment
import com.tencent.iot.explorer.link.kitlink.holder.DeviceListViewHolder
import com.tencent.iot.explorer.link.kitlink.response.DeviceCategoryListResponse
import com.tencent.iot.explorer.link.kitlink.response.ProductsConfigResponse
import com.tencent.iot.explorer.link.kitlink.util.HttpRequest
import com.tencent.iot.explorer.link.kitlink.util.RequestCode
import com.tencent.iot.explorer.link.mvp.IPresenter
import kotlinx.android.synthetic.main.activity_device_category.container_normal
import kotlinx.android.synthetic.main.activity_device_category.container_top
import kotlinx.android.synthetic.main.activity_device_category.gray_line_0
import kotlinx.android.synthetic.main.activity_device_category.gray_line_1
import kotlinx.android.synthetic.main.activity_device_category.iv_scann
import kotlinx.android.synthetic.main.activity_device_category.linearlayout_scann
import kotlinx.android.synthetic.main.activity_device_category.my_scroll_view
import kotlinx.android.synthetic.main.activity_device_category.not_found_dev
import kotlinx.android.synthetic.main.activity_device_category.scann_fail
import kotlinx.android.synthetic.main.activity_device_category.scanning
import kotlinx.android.synthetic.main.activity_device_category.vtab_device_category
import kotlinx.android.synthetic.main.bluetooth_adapter_invalid.retry_to_scann01
import kotlinx.android.synthetic.main.menu_back_layout.iv_back
import kotlinx.android.synthetic.main.menu_cancel_layout.tv_title
import kotlinx.android.synthetic.main.not_found_device.retry_to_scann02
import kotlinx.android.synthetic.main.not_found_device.tv_tag
import kotlinx.android.synthetic.main.scanned_devices.scanned_device_list
import kotlinx.android.synthetic.main.scanned_devices.tv_devs_tip
import kotlinx.android.synthetic.main.scanning.iv_loading_cirecle
import kotlinx.android.synthetic.main.scanning.tv_scanning_ble_devs


class DeviceCategoryActivity  : PActivity(), MyCallback, CRecyclerView.RecyclerItemView, View.OnClickListener, VerticalTabLayout.OnTabSelectedListener{

    private val handler = Handler()
    private var bleDevAdapter: BleDeviceAdapter? = null
    private var bleDevs: MutableList<BleDevice> = ArrayList()

    private var permissions = arrayOf(
        Manifest.permission.CAMERA,
        Manifest.permission.ACCESS_WIFI_STATE,
        Manifest.permission.CHANGE_WIFI_STATE,
        Manifest.permission.CHANGE_WIFI_MULTICAST_STATE
    )
    private var permissionDialog: PermissionDialog? = null

    private var blueToothPermissions = arrayOf(
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.BLUETOOTH
    )

    private val android12BluetoothPermissions = arrayOf(
        Manifest.permission.BLUETOOTH_SCAN,
        Manifest.permission.BLUETOOTH_CONNECT,
        Manifest.permission.ACCESS_FINE_LOCATION,
    )

    private var hasPermission = false

    override fun getPresenter(): IPresenter? {
        return null
    }

    override fun getContentView(): Int {
        return R.layout.activity_device_category
    }

    override fun initView() {
        tv_title.text = getString(R.string.add_device)
        bleDevAdapter = BleDeviceAdapter(bleDevs)
        bleDevAdapter?.scaningTxt = tv_scanning_ble_devs
        bleDevAdapter?.titleTxt = tv_devs_tip
        var layoutManager = GridLayoutManager(this@DeviceCategoryActivity, 4)
        scanned_device_list.setLayoutManager(layoutManager)
        scanned_device_list.adapter = bleDevAdapter
        App.data.tabPosition = 0  // Reset the position of vertical tab
        App.data.screenWith = getScreenWidth()
        HttpRequest.instance.getParentCategoryList(this)
        BleConfigService.get().connetionListener = bleDeviceConnectionListener
        beginScanning()
    }

    private fun refreshDevInfo(bleDevice: BleDevice) {
        var productsList = arrayListOf<String>()
        productsList.add(bleDevice.productId)
        HttpRequest.instance.deviceProducts(productsList, object: MyCallback {
            override fun fail(msg: String?, reqCode: Int) {}

            override fun success(response: BaseResponse, reqCode: Int) {
                if (response.isSuccess()) {
                    if (TextUtils.isEmpty(response.data.toString())) return

                    var products = JSON.parseObject(response.data.toString(), ProductsEntity::class.java)
                    products?.Products?.let {
                        var product = JSON.parseObject(it.getString(0), ProductEntity::class.java)
                        product?.Name?.let { bleDevice.productName = it }
                        product?.IconUrl?.let { bleDevice.url = it }
                        bleDevAdapter?.notifyDataSetChanged()
                    }
                }
            }
        })
    }

    private var bleDeviceConnectionListener = object: BleDeviceConnectionListener {
        override fun onBleDeviceFounded(bleDevice: BleDevice) {
            var index = bleDevs.indexOf(bleDevice)
            if (index < 0 && bleDevice.boundState != 2) {//非连接状态的蓝牙设备在此处可以展示出来进行扫描绑定
                bleDevs.add(bleDevice)
                refreshDevInfo(bleDevice)
            }
            bleDevAdapter?.notifyDataSetChanged()
        }

        override fun onBleDeviceConnected() {}
        override fun onBleDeviceDisconnected(exception: TCLinkException) {}
        override fun onBleDeviceInfo(bleDeviceInfo: BleDeviceInfo) {}
        override fun onBleSetWifiModeResult(success: Boolean) {}
        override fun onBleSendWifiInfoResult(success: Boolean) {}
        override fun onBleWifiConnectedInfo(wifiConnectInfo: BleWifiConnectInfo) {}
        override fun onBlePushTokenResult(success: Boolean) {}
        override fun onMtuChanged(mtu: Int, status: Int) {}
        override fun onBleBindSignInfo(bleDevBindCondition: BleDevBindCondition) {}
        override fun onBleSendSignInfo(bleDevSignResult: BleDevSignResult) {}
        override fun onBleUnbindSignInfo(signInfo: String) {}
        override fun onBlePropertyValue(bleDeviceProperty: BleDeviceProperty) {}
        override fun onBleControlPropertyResult(result: Int) {}
        override fun onBleRequestCurrentProperty() {}
        override fun onBleNeedPushProperty(eventId: Int, bleDeviceProperty: BleDeviceProperty) {}
        override fun onBleReportActionResult(reason: Int, actionId: Int, bleDeviceProperty: BleDeviceProperty) {}
        override fun onBleDeviceFirmwareVersion(firmwareVersion: BleDeviceFirmwareVersion) {}
        override fun onBleDevOtaUpdateResponse(otaUpdateResponse: BleDevOtaUpdateResponse) {}
        override fun onBleDevOtaUpdateResult(success: Boolean, errorCode: Int) {}

        override fun onBleDevOtaReceivedProgressResponse(progress: Int) {}

        override fun onBleDeviceMtuSize(size: Int) {}
        override fun onBleDeviceTimeOut(timeLong: Int) {}
    }

    private val runnable = Runnable {
        iv_loading_cirecle.clearAnimation()
        scanning.visibility = View.GONE
        not_found_dev.visibility = View.VISIBLE
        if (bleDevs.size > 0) {
            not_found_dev.visibility = View.GONE
        } else {
            tv_tag.setText(R.string.not_found_device)
        }
        if (hasPermission) {
            BleConfigService.get().stopScanBluetoothDevices()
        }
    }

    override fun onResume() {
        super.onResume()
        vtab_device_category.setTabSelected(App.data.tabPosition)
    }

    override fun onDestroy() {
        super.onDestroy()
        iv_loading_cirecle.clearAnimation()
        if (hasPermission){
            BleConfigService.get().stopScanBluetoothDevices()
        }
    }

    override fun setListener() {
        iv_back.setOnClickListener { finish() }
        iv_scann.setOnClickListener(this)
        vtab_device_category.addOnTabSelectedListener(this)
        retry_to_scann01.setOnClickListener(this)
        retry_to_scann02.setOnClickListener(this)
        my_scroll_view.setScrollChangedListener(object: MyScrollView.ScrollChangedListener{
            override fun onScrollChanged(scrollX: Int, scrollY: Int, oldScrollX: Int, oldScrollY: Int) {
                val height = gray_line_0.height + gray_line_1.height + linearlayout_scann.height
                if (scrollY >= height && vtab_device_category.parent ==container_normal) {
                    container_normal.removeView(vtab_device_category)
                    gray_line_1.visibility = View.GONE
                    container_top.visibility = View.VISIBLE
                    container_top.addView(vtab_device_category)
                } else if (scrollY < height && vtab_device_category.parent ==container_top){
                    gray_line_1.visibility = View.VISIBLE
                    container_top.visibility = View.GONE
                    container_top.removeView(vtab_device_category)
                    container_normal.addView(vtab_device_category, 0)
                }
            }
        })
        bleDevAdapter?.setOnItemClicked(onBleDevSeclectedListener)
    }

    private var onBleDevSeclectedListener = object: BleDeviceAdapter.OnItemClicked {
        override fun onItemClicked(pos: Int, dev: BleDevice) {
            if (TextUtils.isEmpty(dev.productId)) {
                T.show(getString(R.string.no_product_info))
                return
            }
            BleConfigHardwareActivity.startWithProductid(this@DeviceCategoryActivity, dev.productId, dev.type)
            BleConfigService.get().stopScanBluetoothDevices()
        }
    }

    override fun fail(msg: String?, reqCode: Int) {
        L.e(msg ?: "")
    }

    override fun success(response: BaseResponse, reqCode: Int) {
        when (reqCode) {
            RequestCode.get_parent_category_list-> {
                if (response.isSuccess()) {
                    response.parse(DeviceCategoryListResponse::class.java)?.run {
                        App.data.recommendDeviceCategoryList = List
                        val adapter = MyTabAdapter(this@DeviceCategoryActivity)
                        for (item in List) {
                            adapter.titleList.add(item.CategoryName)
                        }
                        vtab_device_category.layoutParams.width = App.data.screenWith/4
                        vtab_device_category.setupWithFragment(
                            supportFragmentManager,
                            R.id.devce_fragment_container,
                            generateFragments(),
                            adapter
                        )
                    }
                }
            }
            RequestCode.scan_bind_device, RequestCode.sig_bind_device-> {
                if (response.isSuccess()) {
                    T.show(getString(R.string.add_sucess)) //添加成功
                    App.data.setRefreshLevel(2)
                    finish()

                    var resData = JSON.parseObject(response.data.toString(), BindDevResponse::class.java)
                    bindSubDev(resData)
                } else {
                    T.show(response.msg)
                }
            }

            RequestCode.bind_gateway_sub_device -> {
                if (response.isSuccess()) {
                    App.data.refresh = true
                    App.data.setRefreshLevel(2)
                    com.tencent.iot.explorer.link.kitlink.util.Utils.sendRefreshBroadcast(this@DeviceCategoryActivity)
                } else {
                    T.show(response.msg)
                }
            }
        }
    }

    private fun bindSubDev(gatwayDev: BindDevResponse) {
        if (gatwayDev != null && gatwayDev.Data != null && gatwayDev.Data!!.AppDeviceInfo != null) {
            var dev = gatwayDev.Data!!.AppDeviceInfo
            if (dev == null) return

            if (dev.DeviceType == "1") {
                HttpRequest.instance.gatwaySubDevList(dev.ProductId, dev.DeviceName, object: MyCallback{
                    override fun fail(msg: String?, reqCode: Int) {
                        T.show(msg?:"")
                    }

                    override fun success(response: BaseResponse, reqCode: Int) {
                        if (response.isSuccess()) {
                            var gatewaySubDevsResp = JSON.parseObject(response.data.toString(), GatewaySubDevsResp::class.java)
                            if (gatewaySubDevsResp != null && gatewaySubDevsResp.DeviceList != null && gatewaySubDevsResp.DeviceList.size > 0) {
                                for (subDev in gatewaySubDevsResp.DeviceList) {
                                    if (subDev.BindStatus == 0) {
                                        HttpRequest.instance.bindGatwaySubDev(dev.ProductId, dev.DeviceName, subDev.ProductId, subDev.DeviceName, this@DeviceCategoryActivity)
                                    }
                                }
                            }
                        } else {
                            T.show(response.msg)
                        }
                    }
                })
            }
        }
    }

    override fun doAction(
        viewHolder: CRecyclerView.CViewHolder<*>,
        clickView: View,
        position: Int
    ) {}

    override fun getViewHolder(parent: ViewGroup, viewType: Int): CRecyclerView.CViewHolder<*> {
        return DeviceListViewHolder(LayoutInflater.from(this)
                .inflate(R.layout.item_scanned_device, parent, false))
    }

    override fun getViewType(position: Int): Int {
        return 0
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 102) {
            permissionDialog?.dismiss()
            permissionDialog = null
            for (i in permissions.indices) {
                if (permissions.contains(Manifest.permission.ACCESS_COARSE_LOCATION) && grantResults[i] == PackageManager.PERMISSION_GRANTED) { //同意了蓝牙的定位权限
                    beginScanning()
                    return
                }
            }
            //同意了相机权限
            var intent = Intent(Intent(this, ScannerActivity::class.java))
            intent.putExtra(Constant.EXTRA_IS_ENABLE_SCAN_FROM_PIC,true)
            startActivityForResult(intent, CommonField.QR_CODE_REQUEST_CODE)
            permissionDialog?.dismiss()
            permissionDialog = null
        }
    }

    override fun permissionDenied(permission: String) {
        permissionDialog?.dismiss()
        permissionDialog = null
    }

    override fun permissionAllGranted() {
        super.permissionAllGranted()
        hasPermission = true
    }

    override fun onClick(v: View?) {
        when (v) {
            iv_scann -> {
                if (checkPermissions(permissions)) {
                    var intent = Intent(Intent(this, ScannerActivity::class.java))
                    intent.putExtra(Constant.EXTRA_IS_ENABLE_SCAN_FROM_PIC,true)
                    startActivityForResult(intent, CommonField.QR_CODE_REQUEST_CODE)
                } else {
                    // 查看请求camera权限的时间是否大于48小时
                    var cameraJsonString = Utils.getStringValueFromXml(T.getContext(), CommonField.PERMISSION_CAMERA, CommonField.PERMISSION_CAMERA)
                    var cameraJson: JSONObject? = JSONObject.parse(cameraJsonString) as JSONObject?
                    val lasttime = cameraJson?.getLong(CommonField.PERMISSION_CAMERA)
                    if (lasttime != null && lasttime > 0 && System.currentTimeMillis() / 1000 - lasttime < 48*60*60) {
                        T.show(getString(R.string.permission_of_camera_refuse))
                        return
                    }
                    requestPermission(permissions)
                    permissionDialog = PermissionDialog(App.activity, R.mipmap.permission_camera ,getString(R.string.permission_camera_lips), getString(R.string.permission_camera))
                    permissionDialog!!.show()

                    // 记录请求camera权限的时间
                    var json = JSONObject()
                    json.put(CommonField.PERMISSION_CAMERA, System.currentTimeMillis() / 1000)
                    Utils.setXmlStringValue(T.getContext(), CommonField.PERMISSION_CAMERA, CommonField.PERMISSION_CAMERA, json.toJSONString())

                }
            }
//            iv_question -> {
////                jumpActivity(HelpCenterActivity::class.java)
//                jumpActivity(HelpWebViewActivity::class.java)
//            }
            retry_to_scann01, retry_to_scann02 -> {
                beginScanning()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        data?.let {
            if (resultCode == Activity.RESULT_OK) {
                val type = it.getStringExtra(Constant.EXTRA_RESULT_CODE_TYPE)
                it.getStringExtra(Constant.EXTRA_RESULT_CONTENT)?.run {
                    L.d("type=$type,content=$this")

                    when {
                        contains("signature=") -> {//虚拟设备
                            bindDevice(this.substringAfterLast("signature="))
                        }
                        contains("\"DeviceName\"") and contains("\"Signature\"") -> {//真实设备
                            val deviceInfo = DeviceInfo(this)
                            if (!TextUtils.isEmpty(deviceInfo.productId)) {
                                bindDevice(deviceInfo.signature)
                            }
                        }
                        contains("page=softap") -> {
                            var productid = Utils.getUrlParamValue(this, "productId")
                            val productsList = arrayListOf<String>()
                            productsList.add(productid?:"")
                            HttpRequest.instance.getProductsConfig(productsList, patchProductListener)
                        }
                        contains("page=smartconfig") -> {
                            var productid = Utils.getUrlParamValue(this, "productId")
                            val productsList = arrayListOf<String>()
                            productsList.add(productid?:"")
                            HttpRequest.instance.getProductsConfig(productsList, patchProductListener)
                        }
                        contains("page=adddevice") && contains("productId") -> {
                            var productid = Utils.getUrlParamValue(this, "productId")
                            val productsList = arrayListOf<String>()
                            productsList.add(productid!!)
                            if (contains("preview=1")) {
                                var intent = Intent(this@DeviceCategoryActivity, ProductIntroduceActivity::class.java)
                                intent.putExtra(CommonField.PRODUCT_ID, productid)
                                startActivity(intent)
                            } else {
                                HttpRequest.instance.getProductsConfig(productsList, patchProductListener)
                            }

                        }
                        contains("hmacsha") && contains(";") -> { //蓝牙签名绑定 的设备
                            // ${product_id};${device_name};${random};${timestamp};hmacsha256;sign
                            val deviceInfo = TrtcDeviceInfo(this)
                            bleSigBindDevice(deviceInfo, "other_sign")
                        }
                        else -> {//之前旧版本虚拟设备二维码只有签名
                            bindDevice(this)
                        }
                    }
                }
            }
        }
    }

    private var patchProductListener =  object :MyCallback{
        override fun fail(msg: String?, reqCode: Int) {
            T.show(msg)
        }

        override fun success(response: BaseResponse, reqCode: Int) {
            if (response.isSuccess() && reqCode == RequestCode.get_products_config) {
                response.parse(ProductsConfigResponse::class.java)?.run {
                    val config = JsonManager.parseJson(Data[0].Config, ProdConfigDetailEntity::class.java)
                    val wifiConfigTypeList = config.WifiConfTypeList
                    var productId = ""
                    if (!TextUtils.isEmpty(config.profile)) {
                        var jsonProFile = JSON.parseObject(config.profile)
                        if (jsonProFile != null && jsonProFile.containsKey("ProductId") &&
                            !TextUtils.isEmpty(jsonProFile.getString("ProductId"))) {
                            productId = jsonProFile.getString("ProductId")
                        }
                    }

                    if (config != null && !TextUtils.isEmpty(config.Global) && ProductGlobal.isProductGlobalLegal(config.Global)) {
                        var intent = Intent(this@DeviceCategoryActivity, ProductIntroduceActivity::class.java)
                        intent.putExtra(CommonField.PRODUCT_ID, productId)
                        startActivity(intent)
                        return@run
                    }

                    if (wifiConfigTypeList.equals("{}") || TextUtils.isEmpty(wifiConfigTypeList)) {
                        SmartConfigStepActivity.startActivityWithExtra(this@DeviceCategoryActivity, productId)

                    } else if (wifiConfigTypeList.contains("[")) {
                        val typeList = JsonManager.parseArray(wifiConfigTypeList)
                        if (typeList.size > 0 && typeList[0] == "softap") {
                            SoftApStepActivity.startActivityWithExtra(this@DeviceCategoryActivity, productId)
                        } else if (typeList.size > 0 && typeList[0] == "llsyncble") {
                            BleConfigHardwareActivity.startWithProductid(this@DeviceCategoryActivity, productId)
                        } else {
                            SmartConfigStepActivity.startActivityWithExtra(this@DeviceCategoryActivity, productId)
                        }
                    }
                }
            }
        }
    }

    /**
     * 绑定虚拟设备
     */
    private fun bindDevice(signature: String) {
        HttpRequest.instance.scanBindDevice(App.data.getCurrentFamily().FamilyId, App.data.getCurrentRoom().RoomId, signature, this)
    }

    /**
     * 蓝牙签名绑定设备
     */
    private fun bleSigBindDevice(deviceInfo: TrtcDeviceInfo, bindType: String) {
        HttpRequest.instance.sigBindDevice(App.data.getCurrentFamily().FamilyId, App.data.getCurrentRoom().RoomId,
            deviceInfo, bindType, this)
    }

    private fun generateFragments() : List<Fragment>{
        val fragmentList = arrayListOf<Fragment>()
        for (item in App.data.recommendDeviceCategoryList) {
            val fragment = DeviceFragment(this, DeviceFragment.CHECK_H5_CONDITION)
            val bundle = Bundle()
            bundle.putString("CategoryKey", item.CategoryKey)
            fragment.arguments = bundle
            fragmentList.add(fragment)
        }
        App.data.numOfCategories = fragmentList.size
        return fragmentList
    }

    private fun getScreenWidth() : Int {
        val wm = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val metrics = DisplayMetrics()
        wm.defaultDisplay.getMetrics(metrics)
        return metrics.widthPixels
    }

    private fun isBluetoothValid() : Boolean {
        val adapter = BluetoothAdapter.getDefaultAdapter()
        return adapter?.isEnabled ?: false
    }

    private fun beginScanning() {
        val permissions =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) android12BluetoothPermissions else blueToothPermissions
        hasPermission = checkPermissions(permissions)
        if (!hasPermission) {
            // 查看请求ble location权限的时间是否大于48小时
            var locationJsonString = Utils.getStringValueFromXml(T.getContext(), CommonField.PERMISSION_LOCATION, CommonField.PERMISSION_LOCATION)
            var locationJson: JSONObject? = JSONObject.parse(locationJsonString) as JSONObject?
            val lasttime = locationJson?.getLong(CommonField.PERMISSION_LOCATION)
            if (lasttime != null && lasttime > 0 && System.currentTimeMillis() / 1000 - lasttime < 48*60*60) {
                T.show(getString(R.string.permission_of_location_add_device_refuse))
                scann_fail.visibility = View.VISIBLE
                scanning.visibility = View.GONE
                not_found_dev.visibility = View.GONE
                return
            }
            requestPermission(permissions, 103)
            permissionDialog = PermissionDialog(this@DeviceCategoryActivity, R.mipmap.permission_location ,getString(R.string.permission_location_lips), getString(R.string.permission_location_ssid_ble))
            permissionDialog!!.show()

            // 记录请求camera权限的时间
            var json = JSONObject()
            json.put(CommonField.PERMISSION_LOCATION, System.currentTimeMillis() / 1000)
            Utils.setXmlStringValue(T.getContext(), CommonField.PERMISSION_LOCATION, CommonField.PERMISSION_LOCATION, json.toJSONString())
            return
        }

        val rotateAnimation : Animation = AnimationUtils.loadAnimation(this, R.anim.circle_rotate)
        val interpolator =  LinearInterpolator()
        rotateAnimation.interpolator = interpolator
        if (isBluetoothValid()) {
            scanning.visibility = View.VISIBLE
            not_found_dev.visibility = View.GONE
            scann_fail.visibility = View.GONE
            iv_loading_cirecle.startAnimation(rotateAnimation)
            handler.postDelayed(runnable, 120000)
            bleDevs.clear()
            bleDevAdapter?.notifyDataSetChanged()
            BleConfigService.get().startScanBluetoothDevices()
        } else {
            scann_fail.visibility = View.VISIBLE
            scanning.visibility = View.GONE
            not_found_dev.visibility = View.GONE
        }
    }

    private fun stopScanning() {}

    class MyTabAdapter(ctx: Context) : TabAdapter {
        var titleList = arrayListOf<String>()
        var context = ctx

        override fun getBackground(position: Int): Int {
            if (position == 0) return R.mipmap.tab
            return 0
        }

        override fun getTitle(position: Int): ITabView.TabTitle {
            return ITabView.TabTitle.Builder(context).setContent(titleList[position]).setTextColor(
                0xFF0066FF.toInt(),0xFF6C7078.toInt() // 蓝色:0xFF0052D9, 黑色:0xFF000000
            ).setTextSize(12).build()
        }

        override fun getCount(): Int {
            return titleList.size
        }
    }

    override fun onTabReselected(tab: TabView?, position: Int) {}

    override fun onTabSelected(tab: TabView?, position: Int) {
        App.data.tabPosition = position
        for (i in 0 until App.data.numOfCategories) {
            if (i != position)
                vtab_device_category.getTabAt(i).setBackgroundColor(resources.getColor(R.color.gray_F5F5F5))
        }
        tab?.setBackground(R.mipmap.tab)
    }

    // 从当前页面离开，即停止扫描蓝牙设备
    override fun onPause() {
        super.onPause()
        handler.removeCallbacks(runnable)
        handler.postDelayed(runnable, 100)
    }
}
