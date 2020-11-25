package com.tencent.iot.explorer.link.kitlink.activity

import android.content.Intent
import android.text.TextUtils
import android.view.View
import com.alibaba.fastjson.JSONObject
import com.tencent.iot.explorer.link.R
import com.tencent.iot.explorer.link.T
import com.tencent.iot.explorer.link.core.auth.response.BaseResponse
import com.tencent.iot.explorer.link.core.auth.util.JsonManager
import com.tencent.iot.explorer.link.customview.progress.bean.StepBean
import com.tencent.iot.explorer.link.kitlink.consts.CommonField
import com.tencent.iot.explorer.link.kitlink.consts.LoadViewTxtType
import com.tencent.iot.explorer.link.kitlink.entity.ConnectApGuide
import com.tencent.iot.explorer.link.kitlink.entity.ProdConfigDetailEntity
import com.tencent.iot.explorer.link.kitlink.fragment.DeviceFragment
import com.tencent.iot.explorer.link.kitlink.response.ProductsConfigResponse
import com.tencent.iot.explorer.link.kitlink.util.HttpRequest
import com.tencent.iot.explorer.link.core.auth.callback.MyCallback
import com.tencent.iot.explorer.link.mvp.IPresenter
import kotlinx.android.synthetic.main.activity_soft_hotspot.*
import java.util.ArrayList

class SoftHotspotActivity : PActivity() {

    private var loadViewTextType = LoadViewTxtType.LoadLocalViewTxt.ordinal // 0 加载本地文案  1 尝试加载远端配置文案
    private var productId = ""
    private var type = DeviceFragment.ConfigType.SoftAp.id
    private var extraSsid = ""
    private var extraBssid = ""
    private var extraPwd = ""

    override fun getContentView(): Int {
        return R.layout.activity_soft_hotspot
    }

    private fun showProgress() {
        val stepsBeanList = ArrayList<StepBean>()
        stepsBeanList.add(StepBean(getString(R.string.config_hardware)))
        stepsBeanList.add(StepBean(getString(R.string.set_target_wifi)))
        stepsBeanList.add(StepBean(getString(R.string.connect_device)))
        stepsBeanList.add(StepBean(getString(R.string.start_config_network)))
        softap_step_progress.currentStep = 3
        softap_step_progress.setStepViewTexts(stepsBeanList)
        softap_step_progress.setTextSize(12)
    }

    override fun initView() {
        loadViewTextType = intent.getIntExtra(CommonField.LOAD_VIEW_TXT_TYPE, LoadViewTxtType.LoadLocalViewTxt.ordinal)
        if (loadViewTextType != LoadViewTxtType.LoadLocalViewTxt.ordinal) {
            productId = intent.getStringExtra(CommonField.PRODUCT_ID)
        }
        if (intent.hasExtra(CommonField.SSID)) {
            extraSsid = intent.getStringExtra(CommonField.SSID)
        }
        if (intent.hasExtra(CommonField.BSSID)) {
            extraBssid = intent.getStringExtra(CommonField.BSSID)
        }
        if (intent.hasExtra(CommonField.PWD)) {
            extraPwd = intent.getStringExtra(CommonField.PWD)
        }
        if (loadViewTextType == LoadViewTxtType.LoadLocalViewTxt.ordinal) {
            loadViewStandradInfo()
        } else {
            loadViewInfo()
        }
        showProgress()
    }

    override fun setListener() {
        tv_soft_ap_cancel.setOnClickListener { finish() }
        tv_soft_connect_hotspot.setOnClickListener {
            val intent = Intent(this, DeviceWifiActivity::class.java)
            intent.putExtra(CommonField.LOAD_VIEW_TXT_TYPE, LoadViewTxtType.LoadRemoteViewTxt.ordinal)
            intent.putExtra(CommonField.PRODUCT_ID, productId)
            intent.putExtra(CommonField.CONFIG_TYPE, type)
            intent.putExtra(CommonField.SSID, extraSsid)
            intent.putExtra(CommonField.BSSID, extraBssid)
            intent.putExtra(CommonField.PWD, extraPwd)
            startActivity(intent)
        }
    }

    override fun getPresenter(): IPresenter? {
        return null
    }

    private fun loadViewStandradInfo() {
        tv_soft_connect_hotspot_tip.visibility = View.VISIBLE
        tv_soft_connect_hotspot_tip.setText(R.string.soft_ap_hotspot_step_1)
    }

    private fun loadViewInfo() {
        tv_soft_connect_hotspot_tip.visibility = View.VISIBLE
        if (TextUtils.isEmpty(productId)) {
            return
        }

        val productsList  = arrayListOf<String>()
        productsList.add(productId)
        HttpRequest.instance.getProductsConfig(productsList, object: MyCallback {
            override fun fail(msg: String?, reqCode: Int) {
                T.show(msg)
            }

            override fun success(response: BaseResponse, reqCode: Int) {
                response.parse(ProductsConfigResponse::class.java)?.run {
                    val config = JsonManager.parseJson(Data[0].Config, ProdConfigDetailEntity::class.java)

                    if (TextUtils.isEmpty(config.WifiSoftAP)) {
                        return
                    }

                    var json = JSONObject.parseObject(config.WifiSoftAP)
                    if (json.containsKey(CommonField.HARD_WARE_GUIDE)) {
                        var connectApGuide = JSONObject.parseObject(json.getString(CommonField.CONNECT_AP_GUIDE), ConnectApGuide::class.java)
                        if (connectApGuide != null && !TextUtils.isEmpty(connectApGuide.message)) {
                            tv_soft_connect_hotspot_tip.setText(connectApGuide.message)
                        }
                    }
                }
            }
        })
    }
}