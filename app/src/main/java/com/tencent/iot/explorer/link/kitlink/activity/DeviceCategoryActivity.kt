package com.tencent.iot.explorer.link.kitlink.activity

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.text.TextUtils
import android.util.DisplayMetrics
import android.util.Log
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
import com.example.qrcode.Constant
import com.example.qrcode.ScannerActivity
import com.tencent.iot.explorer.link.App
import com.tencent.iot.explorer.link.R
import com.tencent.iot.explorer.link.T
import com.tencent.iot.explorer.link.core.auth.callback.MyCallback
import com.tencent.iot.explorer.link.core.auth.entity.DevModeInfo
import com.tencent.iot.explorer.link.core.auth.response.BaseResponse
import com.tencent.iot.explorer.link.core.auth.util.JsonManager
import com.tencent.iot.explorer.link.core.link.entity.*
import com.tencent.iot.explorer.link.core.link.entity.DeviceInfo
import com.tencent.iot.explorer.link.core.link.exception.TCLinkException
import com.tencent.iot.explorer.link.core.link.listener.BleDeviceConnectionListener
import com.tencent.iot.explorer.link.core.link.service.BleConfigService
import com.tencent.iot.explorer.link.core.log.L
import com.tencent.iot.explorer.link.core.utils.Utils
import com.tencent.iot.explorer.link.customview.MyScrollView
import com.tencent.iot.explorer.link.customview.dialog.PermissionDialog
import com.tencent.iot.explorer.link.customview.recyclerview.CRecyclerView
import com.tencent.iot.explorer.link.customview.verticaltab.*
import com.tencent.iot.explorer.link.kitlink.adapter.BleDeviceAdapter
import com.tencent.iot.explorer.link.kitlink.adapter.DeviceAdapter
import com.tencent.iot.explorer.link.kitlink.consts.CommonField
import com.tencent.iot.explorer.link.kitlink.entity.*
import com.tencent.iot.explorer.link.kitlink.fragment.DeviceFragment
import com.tencent.iot.explorer.link.kitlink.holder.DeviceListViewHolder
import com.tencent.iot.explorer.link.kitlink.response.DeviceCategoryListResponse
import com.tencent.iot.explorer.link.kitlink.response.ProductsConfigResponse
import com.tencent.iot.explorer.link.kitlink.util.*
import com.tencent.iot.explorer.link.mvp.IPresenter
import kotlinx.android.synthetic.main.activity_device_category.*
import kotlinx.android.synthetic.main.bluetooth_adapter_invalid.*
import kotlinx.android.synthetic.main.menu_back_layout.*
import kotlinx.android.synthetic.main.menu_cancel_layout.tv_title
import kotlinx.android.synthetic.main.not_found_device.*
import kotlinx.android.synthetic.main.scanned_devices.*
import kotlinx.android.synthetic.main.scanning.*


class DeviceCategoryActivity  : PActivity(), MyCallback, CRecyclerView.RecyclerItemView, View.OnClickListener, VerticalTabLayout.OnTabSelectedListener{

    private val handler = Handler()
    private var bleDevAdapter: BleDeviceAdapter? = null
    private var bleDevs: MutableList<BleDevice> = ArrayList()

    private var permissions = arrayOf(
        Manifest.permission.CAMERA,
        Manifest.permission.ACCESS_WIFI_STATE,
        Manifest.permission.CHANGE_WIFI_STATE,
        Manifest.permission.CHANGE_WIFI_MULTICAST_STATE,
        Manifest.permission.ACCESS_FINE_LOCATION
    )

    private var blueToothPermissions = arrayOf(
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.ACCESS_FINE_LOCATION
    )

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
            if (index < 0) {
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
        BleConfigService.get().stopScanBluetoothDevices()
    }

    override fun onResume() {
        super.onResume()
        vtab_device_category.setTabSelected(App.data.tabPosition)
    }

    override fun onDestroy() {
        super.onDestroy()
        iv_loading_cirecle.clearAnimation()
        BleConfigService.get().stopScanBluetoothDevices()
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
            var intent = Intent(this@DeviceCategoryActivity, BleConfigHardwareActivity::class.java)
            intent.putExtra(CommonField.PRODUCT_ID, dev.productId)
            App.data.bleDevice = dev
            this@DeviceCategoryActivity.startActivity(intent)
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

    override fun permissionAllGranted() {
        var intent = Intent(Intent(this, ScannerActivity::class.java))
        intent.putExtra(Constant.EXTRA_IS_ENABLE_SCAN_FROM_PIC,true)
        startActivityForResult(intent, CommonField.QR_CODE_REQUEST_CODE)
    }

    override fun permissionDenied(permission: String) {
//        requestPermission(arrayOf(permission))
    }

    override fun onClick(v: View?) {
        when (v) {
            iv_scann -> {
                if (checkPermissions(permissions)) {
                    var intent = Intent(Intent(this, ScannerActivity::class.java))
                    intent.putExtra(Constant.EXTRA_IS_ENABLE_SCAN_FROM_PIC,true)
                    startActivityForResult(intent, CommonField.QR_CODE_REQUEST_CODE)
                } else {
                    var dlg = PermissionDialog(this@DeviceCategoryActivity, getString(R.string.permission_of_wifi), getString(R.string.permission_of_wifi_lips))
                    dlg.show()
                    dlg.setOnDismisListener(object : PermissionDialog.OnDismisListener {
                        override fun OnClickRefuse() {

                        }

                        override fun OnClickOK() {
                            requestPermission(permissions)
                        }

                    })
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
        if (!checkPermissions(blueToothPermissions)) {
            var dlg = PermissionDialog(this@DeviceCategoryActivity, getString(R.string.permission_of_wifi), getString(R.string.permission_of_wifi_lips))
            dlg.show()
            dlg.setOnDismisListener(object : PermissionDialog.OnDismisListener {
                override fun OnClickRefuse() {

                }

                override fun OnClickOK() {
                    requestPermission(blueToothPermissions)
                }

            })
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
