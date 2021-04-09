package com.tencent.iot.explorer.link.kitlink.activity

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationSet
import android.view.animation.LinearInterpolator
import android.view.animation.RotateAnimation
import androidx.fragment.app.Fragment
import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.JSONObject
import com.example.qrcode.Constant
import com.example.qrcode.ScannerActivity
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.tencent.android.tpush.XGIOperateCallback
import com.tencent.android.tpush.XGPushConfig
import com.tencent.android.tpush.XGPushManager
import com.tencent.iot.explorer.link.App
import com.tencent.iot.explorer.link.BuildConfig
import com.tencent.iot.explorer.link.R
import com.tencent.iot.explorer.link.T
import com.tencent.iot.explorer.link.core.auth.entity.FamilyEntity
import com.tencent.iot.explorer.link.core.auth.response.BaseResponse
import com.tencent.iot.explorer.link.core.log.L
import com.tencent.iot.explorer.link.core.utils.FileUtils
import com.tencent.iot.explorer.link.core.utils.SharePreferenceUtil
import com.tencent.iot.explorer.link.core.utils.Utils
import com.tencent.iot.explorer.link.customview.dialog.*
import com.tencent.iot.explorer.link.customview.home.BottomItemEntity
import com.tencent.iot.explorer.link.kitlink.consts.CommonField
import com.tencent.iot.explorer.link.kitlink.fragment.CommentFragment
import com.tencent.iot.explorer.link.kitlink.fragment.HomeFragment
import com.tencent.iot.explorer.link.kitlink.fragment.MeFragment
import com.tencent.iot.explorer.link.kitlink.fragment.SmartFragment
import com.tencent.iot.explorer.link.kitlink.popup.FamilyListPopup
import com.tencent.iot.explorer.link.kitlink.util.DateUtils
import com.tencent.iot.explorer.link.kitlink.util.HttpRequest
import com.tencent.iot.explorer.link.core.auth.callback.MyCallback
import com.tencent.iot.explorer.link.core.auth.entity.DeviceEntity
import com.tencent.iot.explorer.link.core.auth.util.JsonManager
import com.tencent.iot.explorer.link.core.link.entity.DeviceInfo
import com.tencent.iot.explorer.link.core.link.entity.TrtcDeviceInfo
import com.tencent.iot.explorer.link.kitlink.entity.BindDevResponse
import com.tencent.iot.explorer.link.kitlink.entity.GatewaySubDevsResp
import com.tencent.iot.explorer.link.kitlink.entity.ProdConfigDetailEntity
import com.tencent.iot.explorer.link.kitlink.entity.ProductGlobal
import com.tencent.iot.explorer.link.kitlink.response.ProductsConfigResponse
import com.tencent.iot.explorer.link.kitlink.util.LogcatHelper
import com.tencent.iot.explorer.link.kitlink.util.RequestCode
import com.tencent.iot.explorer.link.mvp.IPresenter
import com.tencent.tpns.baseapi.XGApiConfig
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*
import kotlin.system.exitProcess

/**
 * main页面
 */
class MainActivity : PActivity(), MyCallback {
    private var previousPosition = 0

    private val fragments = arrayListOf<Fragment>()

    private var familyPopup: FamilyListPopup? = null

    private var isForceUpgrade = true

    private var permissions = arrayOf(
        Manifest.permission.CAMERA,
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.RECORD_AUDIO
    )

    private var scanPermissions = arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.ACCESS_WIFI_STATE,
            Manifest.permission.CHANGE_WIFI_STATE,
            Manifest.permission.CHANGE_WIFI_MULTICAST_STATE,
            Manifest.permission.ACCESS_FINE_LOCATION
    )

    override fun getContentView(): Int {
        return R.layout.activity_main
    }

    override fun getPresenter(): IPresenter? {
        return null
    }

    override fun onResume() {
        super.onResume()
        login(this)
        val cancelAccountTime = SharePreferenceUtil.getLong(this, App.CONFIG, CommonField.CANCEL_ACCOUNT_TIME)
        if (cancelAccountTime > 0) {
            showCancelAccountStoppedDialog(cancelAccountTime)
        }
        if (isForceUpgrade) {
            startUpdateApp()
        }
    }

    private fun startUpdateApp() {
        HttpRequest.instance.getLastVersion(object: MyCallback{
            override fun fail(msg: String?, reqCode: Int) {
                T.show(msg)
            }
            override fun success(response: BaseResponse, reqCode: Int) {
                if (response.isSuccess()) {
                    val json = response.data as JSONObject
                    val info = UpgradeInfo.convertJson2UpgradeInfo(json)
                    if (App.needUpgrade(info!!.version) && info.upgradeType != 2 && !UpgradeDialog.dialogShowing()) {
                        isForceUpgrade = info.upgradeType == 1 // 2:静默更新不提示 1:强制升级 0:用户确认
                        if (isForceUpgrade || (!isForceUpgrade && !UpgradeDialog.dialogShowed())) {
                            val dialog = UpgradeDialog(this@MainActivity, info)
                            dialog.setOnDismisListener(upgradeDialogListener)

                            if (!this@MainActivity.isFinishing) {
                                dialog.show()
                            }
                        }
                    }
                }
            }
        })
    }

    private var upgradeDialogListener =
        UpgradeDialog.OnDismisListener { url ->
            val dialog = ProgressDialog(this@MainActivity, url)
            dialog.setOnDismisListener(downloadListener)
            dialog.show()
        }

    private var downloadListener = object: ProgressDialog.OnDismisListener {
        override fun onDownloadSuccess(path: String) {
            FileUtils.installApk(this@MainActivity, path)
        }
        override fun onDownloadFailed() {
            T.show(resources.getString(R.string.download_failed))
        }
        override fun onDownloadProgress(currentProgress: Int, size: Int) { }
    }

    override fun initView() {
        val userId = SharePreferenceUtil.getString(this@MainActivity, App.CONFIG, CommonField.USER_ID)
        FirebaseCrashlytics.getInstance().setUserId(userId)
        FirebaseAnalytics.getInstance(this).setUserId(userId)
        openXGPush()
        home_bottom_view.addUnclickAbleItem(2) // 限定2号位置不可选中
        requestPermission(permissions)
        LogcatHelper.getInstance(this).start()
        home_bottom_view.addMenu(
            BottomItemEntity(
                getString(R.string.main_tab_1),
                resources.getColor(R.color.main_tab_normal), resources.getColor(R.color.main_tab_hover),
                R.mipmap.main_tab_1_normal, R.mipmap.main_tab_1_hover
            )
        )

            .addMenu(
                BottomItemEntity(
                    getString(R.string.main_tab_5),
                    resources.getColor(R.color.main_tab_normal), resources.getColor(R.color.main_tab_hover),
                    R.mipmap.smart_unpressed, R.mipmap.smart_pressed
                )
            )
            .addMenu(
                BottomItemEntity(
                    "",
                    resources.getColor(R.color.translucent), resources.getColor(R.color.translucent),
                    R.color.translucent, R.color.translucent
                )
            )
            .addMenu(
                BottomItemEntity(
                    getString(R.string.main_tab_4),
                    resources.getColor(R.color.main_tab_normal), resources.getColor(R.color.main_tab_hover),
                    R.mipmap.commet_unpressed, R.mipmap.commet_pressed
                )
            )
            .addMenu(
                BottomItemEntity(
                    getString(R.string.main_tab_3),
                    resources.getColor(R.color.main_tab_normal), resources.getColor(R.color.main_tab_hover),
                    R.mipmap.main_tab_3_normal, R.mipmap.main_tab_3_hover
                )
            ).showMenu()

        fragments.clear()
        fragments.add(HomeFragment())
        fragments.add(SmartFragment())
        fragments.add(CommentFragment())
        fragments.add(MeFragment())
        this.supportFragmentManager.beginTransaction()
            .add(R.id.main_container, fragments[0])
            .show(fragments[0])
            .commit()

        window.decorView.systemUiVisibility =
            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
    }

    override fun setListener() {
        iv_main_add.setOnClickListener{
            var dlg = AddOptionsDialog(this@MainActivity)
            dlg.show()

            // 恢复动画
            val rotate = RotateAnimation(45f, 0f,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f)
            val lin = LinearInterpolator()
            rotate.interpolator = lin
            rotate.duration = 200 //设置动画持续周期
            rotate.fillAfter = true //动画执行完后是否停留在执行完的状态

            dlg.setOnDismisListener(object : AddOptionsDialog.OnDismisListener {
                override fun onDismissed() {
                    iv_main_add.startAnimation(rotate)
                }

                override fun onItemClicked(pos: Int) {
                    iv_main_add.startAnimation(rotate)
                    if (pos == 2) {
                        var options = ArrayList<String>()
                        options.add(getString(R.string.smart_option_1))
                        options.add(getString(R.string.smart_option_2))
                        var addDialog = ListOptionsDialog(this@MainActivity, options)
                        addDialog.setOnDismisListener(onItemClickedListener)
                        addDialog.show()
                    } else if (pos == 0) {
                        jumpAddDevActivity()
                    } else if (pos == 1) {
                        if (checkPermissions(scanPermissions)) {
                            var intent = Intent(this@MainActivity, ScannerActivity::class.java)
                            intent.putExtra(Constant.EXTRA_IS_ENABLE_SCAN_FROM_PIC, true)
                            startActivityForResult(intent, CommonField.QR_CODE_REQUEST_CODE)
                        } else {
                            requestPermission(scanPermissions)
                        }
                    }
                }
            })

            val animationSet = AnimationSet(true)
            val rotateAnimation = RotateAnimation(0f, 45f,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f)
            animationSet.addAnimation(rotateAnimation)
            animationSet.fillAfter = true
            iv_main_add.startAnimation(animationSet)
        }

        home_bottom_view.setOnItemClickListener { _, position, previewPosition ->
            if (position == 2) {
                return@setOnItemClickListener
            }

            var tagPos = position
            if (tagPos > 2) {
                tagPos = tagPos - 1
            }
            showFragment(tagPos)
            if (tagPos == 0) {
                window.decorView.systemUiVisibility =
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            } else {
                window.decorView.systemUiVisibility =
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            }
        }

        if (fragments != null && fragments.size > 0) {
            (fragments[0] as? HomeFragment)?.run {
                popupListener = object : HomeFragment.PopupListener {
                    override fun onPopupListener(familyList: List<FamilyEntity>) {
                        this@MainActivity.showFamilyPopup(familyList)
                    }
                }
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 102) {
            if (permissions.contentEquals(scanPermissions)) {
                var intent = Intent(Intent(this, ScannerActivity::class.java))
                intent.putExtra(Constant.EXTRA_IS_ENABLE_SCAN_FROM_PIC,true)
                startActivityForResult(intent, CommonField.QR_CODE_REQUEST_CODE)
            }
        }
    }

    private var onItemClickedListener = ListOptionsDialog.OnDismisListener {
        if (it == 0) {
            jumpActivity(AddManualTaskActivity::class.java)
        } else if (it == 1) {
            jumpActivity(AddAutoicTaskActivity::class.java)
        }
    }

    private fun openXGPush() {
        XGPushConfig.init(applicationContext)
        if (App.data.regionId == "1") {// 中国大陆
            XGPushConfig.setAccessId(applicationContext, BuildConfig.XgAccessId.toLong())
            XGPushConfig.setAccessKey(applicationContext, BuildConfig.XgAccessKey)
            XGApiConfig.setServerSuffix(applicationContext, CommonField.XG_ACCESS_POINT_CHINA)
        } else if (App.data.regionId == "22") {// 美国
            XGPushConfig.setAccessId(applicationContext, BuildConfig.XgUSAAccessId.toLong())
            XGPushConfig.setAccessKey(applicationContext, BuildConfig.XgUSAAccessKey)
            XGApiConfig.setServerSuffix(applicationContext, CommonField.XG_ACCESS_POINT_USA)
        }
        XGPushManager.registerPush(applicationContext, object : XGIOperateCallback {
            override fun onSuccess(data: Any?, p1: Int) {
                L.e("注册成功，设备token为：$data")
                data?.let {
                    App.data.xg_token = it.toString()
                    bindXG()
                }
            }

            override fun onFail(data: Any?, errCode: Int, msg: String?) {
                L.e("注册失败，错误码：$errCode ,错误信息：$msg")
            }
        })
    }

    /**
     * 绑定信鸽推送
     */
    private fun bindXG() {
        if (TextUtils.isEmpty(App.data.xg_token)) return
        HttpRequest.instance.bindXG(App.data.xg_token, this)
    }

    /**
     * 解绑信鸽推送
     */
    private fun unbindXG() {
        if (TextUtils.isEmpty(App.data.xg_token)) return
        HttpRequest.instance.unbindXG(App.data.xg_token, this)
    }

    override fun fail(msg: String?, reqCode: Int) {
        L.e(msg ?: "")
    }

    override fun success(response: BaseResponse, reqCode: Int) {
        when (reqCode) {
            RequestCode.scan_bind_device, RequestCode.sig_bind_device -> {
                if (response.isSuccess()) {
                    T.show(getString(R.string.add_sucess)) //添加成功
                    App.data.refresh = true
                    App.data.setRefreshLevel(2)
                    com.tencent.iot.explorer.link.kitlink.util.Utils.sendRefreshBroadcast(this@MainActivity)

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
                    com.tencent.iot.explorer.link.kitlink.util.Utils.sendRefreshBroadcast(this@MainActivity)
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
                                        HttpRequest.instance.bindGatwaySubDev(dev.ProductId, dev.DeviceName, subDev.ProductId, subDev.DeviceName, this@MainActivity)
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

    private fun showFragment(position: Int) {
        val transaction = this.supportFragmentManager.beginTransaction()
        if (fragments[position].isAdded) {
            transaction.show(fragments[position]).hide(fragments[previousPosition]).commit()
        } else {
            transaction.add(R.id.main_container, fragments[position])
                .show(fragments[position]).hide(fragments[previousPosition])
                .commit()
        }
        previousPosition = position
    }

    private fun showFamilyPopup(familyList: List<FamilyEntity>) {
        if (familyPopup == null) {
            familyPopup = FamilyListPopup(this)
            familyPopup?.setList(familyList)
        }
        familyPopup?.show(main)
        familyPopup?.onItemClickListener = object : FamilyListPopup.OnItemClickListener {
            override fun onItemClick(popupWindow: FamilyListPopup, position: Int) {
                popupWindow.dismiss()
                (fragments[0] as? HomeFragment)?.run {
                    tabFamily(position)
                }
            }
        }
        familyPopup?.setOnClickManagerListener(View.OnClickListener {
            jumpActivity(FamilyListActivity::class.java)
            familyPopup?.dismiss()
        })
    }


    override fun onDestroy() {
        unbindXG()
        super.onDestroy()
        LogcatHelper.getInstance(this).stop()
    }

    private var timestamp = 0L

    /**
     * 连续按两个关闭app
     */
    override fun onBackPressed() {
        val t = System.currentTimeMillis()
        if (t - timestamp < 1000) {
            exitApp()
            exitProcess(0)
        } else {
            timestamp = t
            T.show(getString(R.string.tap_more_exit)) //再按一下退出应用
        }
    }

    private fun showCancelAccountStoppedDialog(time: Long){
        var content = getString(R.string.cancel_account_stopped_content)
        val cancelAccountTime = DateUtils.getFormatDate(Date(time*1000))
        content = content.replace("date", cancelAccountTime)
        val builder = AlertDialog.Builder(this)
        builder.setTitle(R.string.cancel_account_stopped_title)
            .setMessage(content)
            .setCancelable(false)
            .setPositiveButton(R.string.have_known,
                DialogInterface.OnClickListener { dialog, id ->
                    SharePreferenceUtil.saveLong(this, App.CONFIG, CommonField.CANCEL_ACCOUNT_TIME, 0)
                })
        builder.create()
        builder.show()
    }

    private fun bindDevice(signature: String) {
        HttpRequest.instance.scanBindDevice(App.data.getCurrentFamily().FamilyId, App.data.getCurrentRoom().RoomId, signature, this)
    }

    private fun bleSigBindDevice(deviceInfo: TrtcDeviceInfo, bindType: String) {
        HttpRequest.instance.sigBindDevice(App.data.getCurrentFamily().FamilyId, App.data.getCurrentRoom().RoomId,
                deviceInfo, bindType, this)
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
                        var intent = Intent(this@MainActivity, ProductIntroduceActivity::class.java)
                        intent.putExtra(CommonField.EXTRA_INFO, productId)
                        startActivity(intent)
                        return@run
                    }

                    if (wifiConfigTypeList.equals("{}") || TextUtils.isEmpty(wifiConfigTypeList)) {
                        SmartConfigStepActivity.startActivityWithExtra(this@MainActivity, productId)

                    } else if (wifiConfigTypeList.contains("[")) {
                        val typeList = JsonManager.parseArray(wifiConfigTypeList)
                        if (typeList.size > 0 && typeList[0] == "softap") {
                            SoftApStepActivity.startActivityWithExtra(this@MainActivity, productId)
                        } else {
                            SmartConfigStepActivity.startActivityWithExtra(this@MainActivity, productId)
                        }
                    }
                }
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
                            var uri = Uri.parse(this)
                            var productId = uri.getQueryParameter(CommonField.EXTRA_PRODUCT_ID)
                            SoftApStepActivity.startActivityWithExtra(this@MainActivity, productId)
                        }
                        contains("page=smartconfig") -> {
                            var uri = Uri.parse(this)
                            var productId = uri.getQueryParameter(CommonField.EXTRA_PRODUCT_ID)
                            SmartConfigStepActivity.startActivityWithExtra(this@MainActivity, productId)
                        }
                        contains("page=adddevice") && contains("productId") -> {
                            var productid = Utils.getUrlParamValue(this, "productId")
                            val productsList = arrayListOf<String>()
                            productsList.add(productid!!)
                            if (contains("preview=1")) {
                                var intent = Intent(this@MainActivity, ProductIntroduceActivity::class.java)
                                intent.putExtra(CommonField.EXTRA_INFO, productid)
                                startActivity(intent)
                            } else {
                                HttpRequest.instance.getProductsConfig(productsList, patchProductListener)
                            }

                        }
                        contains("hmacsha") && contains(";") -> { //蓝牙签名绑定 的设备
                            // ${product_id};${device_name};${random};${timestamp};hmacsha256;sign
                            val deviceInfo = TrtcDeviceInfo(this)
                            bleSigBindDevice(deviceInfo, "bluetooth_sign")
                        }
                        else -> {//之前旧版本虚拟设备二维码只有签名
                            bindDevice(this)
                        }
                    }
                }
            }
        }
    }
}
