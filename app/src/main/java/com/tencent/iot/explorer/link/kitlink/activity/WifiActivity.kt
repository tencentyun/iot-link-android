package com.tencent.iot.explorer.link.kitlink.activity

import android.content.Context
import android.content.Intent
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import android.provider.Settings
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.View
import androidx.appcompat.content.res.AppCompatResources
import com.tencent.iot.explorer.link.R
import com.tencent.iot.explorer.link.T
import com.tencent.iot.explorer.link.core.log.L
import com.tencent.iot.explorer.link.core.utils.KeyBoardUtils
import com.tencent.iot.explorer.link.core.utils.LocationUtil
import com.tencent.iot.explorer.link.customview.dialog.WifiHelperDialog
import com.tencent.iot.explorer.link.customview.progress.bean.StepBean
import com.tencent.iot.explorer.link.kitlink.consts.CommonField
import com.tencent.iot.explorer.link.kitlink.consts.LoadViewTxtType
import com.tencent.iot.explorer.link.kitlink.fragment.DeviceFragment
import com.tencent.iot.explorer.link.mvp.IPresenter
import com.tencent.iot.explorer.link.mvp.presenter.GetBindDeviceTokenPresenter
import com.tencent.iot.explorer.link.mvp.view.GetBindDeviceTokenView
import kotlinx.android.synthetic.main.activity_wifi.*
import kotlinx.android.synthetic.main.smart_config_second.*
import java.util.ArrayList

class WifiActivity : PActivity(), GetBindDeviceTokenView {

    private lateinit var presenter: GetBindDeviceTokenPresenter
    private var loadViewTextType = LoadViewTxtType.LoadLocalViewTxt.ordinal // 0 加载本地文案  1 尝试加载远端配置文案
    private var productId = ""
    private var type = DeviceFragment.ConfigType.SmartConfig.id
    private var wifiInfo: WifiInfo? = null
    private var bssid = ""
    private var extraSsid = ""
    private var extraBssid = ""
    private var extraPwd = ""

    var openWifiDialog: WifiHelperDialog? = null
    var openLocationServiceDialog: WifiHelperDialog? = null

    override fun getContentView(): Int {
        return R.layout.activity_wifi
    }

    private fun refreshTypeView() {
        if (type == DeviceFragment.ConfigType.SoftAp.id){
            tv_soft_ap_title.setText(R.string.soft_config_network)
            val stepsBeanList = ArrayList<StepBean>()
            stepsBeanList.add(StepBean(getString(R.string.config_hardware)))
            stepsBeanList.add(StepBean(getString(R.string.set_target_wifi)))
            stepsBeanList.add(StepBean(getString(R.string.connect_device)))
            stepsBeanList.add(StepBean(getString(R.string.start_config_network)))
            softap_step_progress.currentStep = 2
            softap_step_progress.setStepViewTexts(stepsBeanList)
            softap_step_progress.setTextSize(12)
        } else {
            tv_soft_ap_title.setText(R.string.smart_config_config_network)
            val stepsBeanList = ArrayList<StepBean>()
            stepsBeanList.add(StepBean(getString(R.string.config_hardware)))
            stepsBeanList.add(StepBean(getString(R.string.select_wifi)))
            stepsBeanList.add(StepBean(getString(R.string.start_config_network)))
            softap_step_progress.currentStep = 2
            softap_step_progress.setStepViewTexts(stepsBeanList)
            softap_step_progress.setTextSize(12)
        }
    }

    override fun initView() {
        loadViewTextType = intent.getIntExtra(CommonField.LOAD_VIEW_TXT_TYPE, LoadViewTxtType.LoadLocalViewTxt.ordinal)
        if (loadViewTextType != LoadViewTxtType.LoadLocalViewTxt.ordinal) {
            productId = intent.getStringExtra(CommonField.PRODUCT_ID)
        }
        type = intent.getIntExtra(CommonField.CONFIG_TYPE, DeviceFragment.ConfigType.SmartConfig.id)
        if (intent.hasExtra(CommonField.SSID)) {
            extraSsid = intent.getStringExtra(CommonField.SSID)
        }
        if (intent.hasExtra(CommonField.BSSID)) {
            extraBssid = intent.getStringExtra(CommonField.BSSID)
        }
        if (intent.hasExtra(CommonField.PWD)) {
            extraPwd = intent.getStringExtra(CommonField.PWD)
        }
        presenter = GetBindDeviceTokenPresenter(this)
        refreshTypeView()

        openWifiDialog = WifiHelperDialog(this, getString(R.string.please_open_wifi))
        openLocationServiceDialog = WifiHelperDialog(this, getString(R.string.please_open_location_service))

        showWifiInfo()
        et_select_wifi_pwd.addClearImage(iv_wifi_eye_clear)
        et_select_wifi_pwd.addShowImage(iv_wifi_eye, R.mipmap.icon_visible, R.mipmap.icon_invisible)

    }

    override fun onResume() {
        super.onResume()
        showWifiInfo()
    }

    /**
     * 展示wifi
     */
    private fun showWifiInfo() {
        let {
            val wifiManager = it.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
            wifiInfo = wifiManager.connectionInfo

            if (wifiInfo == null || wifiInfo!!.bssid == null) {
                tv_select_wifi.hint = getString(R.string.not_network)
                tv_wifi_commit.isEnabled = false
                tv_select_wifi.setText("")
                openWifiDialog?.show()
            } else {
                var ssid2Set = wifiManager.connectionInfo.ssid.replace("\"", "")
                if (!LocationUtil.isLocationServiceEnable(this)) {
                    tv_select_wifi.hint = getString(R.string.open_location_tip)
                    ssid2Set = ""
                    openLocationServiceDialog?.show()
                }
                tv_select_wifi.setText(ssid2Set)
                if (tv_select_wifi.text.contains(CommonField.SSID_UNKNOWN)) {
                    T.show(getString(R.string.open_location_tip))
                }
                bssid = wifiInfo!!.bssid
            }
            tv_select_wifi.isEnabled = type == DeviceFragment.ConfigType.SoftAp.id

            isNextClickable()

            tv_method.visibility = View.GONE
            tv_method_tip.visibility = View.GONE
            tv_tip_wifi.setText(R.string.input_wifi_pwd)
        }
    }

    override fun setListener() {
        tv_soft_ap_cancel.setOnClickListener { finish() }
        openWifiDialog?.setOnDismisListener(object: WifiHelperDialog.OnDismisListener{
            override fun onOkClicked() {
                startActivity(Intent(Settings.ACTION_WIFI_SETTINGS))
            }
            override fun onCancelClicked() {}
        })
        openLocationServiceDialog?.setOnDismisListener(object: WifiHelperDialog.OnDismisListener{
            override fun onOkClicked() {
                startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
            }
            override fun onCancelClicked() {}
        })
        tv_select_wifi.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }
        })
        et_select_wifi_pwd.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                isNextClickable()
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }
        })
        container_wifi.setOnClickListener {
            KeyBoardUtils.hideKeyBoard(this, et_select_wifi_pwd)
        }

        iv_select_wifi.setOnClickListener {
            startActivity(Intent(Settings.ACTION_WIFI_SETTINGS))
        }
        tv_wifi_commit.setOnClickListener {
            presenter.getBindDeviceToken()
            wifiInfo?.let {
                extraSsid = it.ssid.replace("\"", "")
                extraBssid = it.bssid
                extraPwd = et_select_wifi_pwd.text.trim().toString()
            }
            KeyBoardUtils.hideKeyBoard(
                this,
                et_select_wifi_pwd
            )
        }
    }

    private fun startActivityWithExtra(cls: Class<*>?) {
        val intent = Intent(this, cls)
        intent.putExtra(CommonField.LOAD_VIEW_TXT_TYPE, LoadViewTxtType.LoadRemoteViewTxt.ordinal)
        intent.putExtra(CommonField.PRODUCT_ID, productId)
        intent.putExtra(CommonField.CONFIG_TYPE, type)
        intent.putExtra(CommonField.SSID, extraSsid)
        intent.putExtra(CommonField.BSSID, extraBssid)
        intent.putExtra(CommonField.PWD, extraPwd)
        startActivity(intent)
    }

    override fun getPresenter(): IPresenter? {
        return presenter
    }

    private fun isNextClickable() {
        if (tv_select_wifi.text != null && (TextUtils.isEmpty(tv_select_wifi.text.toString())) ||
            tv_select_wifi.text.toString().equals(CommonField.SSID_UNKNOWN) ||
            (et_select_wifi_pwd.text != null && (TextUtils.isEmpty(et_select_wifi_pwd.text.toString())))) {
            tv_wifi_commit.isClickable = false
            tv_wifi_commit.background =
                AppCompatResources.getDrawable(this, R.drawable.background_grey_dark_cell)
            return
        }

        tv_wifi_commit.isClickable = true
        tv_wifi_commit.background = AppCompatResources.getDrawable(this, R.drawable.background_circle_bule_gradient)
    }

    override fun onSuccess(token: String) {
        L.e("getToken onSuccess token:" + token)

        if (type == DeviceFragment.ConfigType.SoftAp.id){
            startActivityWithExtra(SoftHotspotActivity::class.java);
        } else {
            startActivityWithExtra(ConnectProgressActivity::class.java);
        }
    }

    override fun onFail(msg: String) {
        L.e("getToken onFail msg:" + msg)
    }
}