package com.tencent.iot.explorer.link.kitlink.fragment

import android.content.Context
import android.content.Intent
import android.net.wifi.WifiManager
import android.provider.Settings
import android.text.TextUtils
import android.view.View
import com.alibaba.fastjson.JSONObject
import com.tencent.iot.explorer.link.R
import com.tencent.iot.explorer.link.core.auth.util.JsonManager
import com.tencent.iot.explorer.link.kitlink.consts.CommonField
import com.tencent.iot.explorer.link.kitlink.consts.LoadViewTxtType
import com.tencent.iot.explorer.link.kitlink.entity.ConnectApGuide
import com.tencent.iot.explorer.link.kitlink.entity.ProdConfigDetailEntity
import com.tencent.iot.explorer.link.kitlink.response.ProductsConfigResponse
import com.tencent.iot.explorer.link.kitlink.util.HttpRequest
import com.tencent.iot.explorer.link.kitlink.util.MyCallback
import com.tencent.iot.explorer.link.mvp.IPresenter
import com.tencent.iot.explorer.link.T
import com.tencent.iot.explorer.link.core.auth.response.BaseResponse
import kotlinx.android.synthetic.main.fragment_soft_hotspot.*

class SoftHotspotFragment(type: Int, productId: String) : BaseFragment() {

    var onNextListener: OnNextListener? = null
    private var isFirst = true
    private var type: Int
    private var productId: String

    override fun getPresenter(): IPresenter? {
        return null
    }

    init {
        this.type = type
        this.productId = productId
    }

    override fun getContentView(): Int {
        return R.layout.fragment_soft_hotspot
    }

    override fun onResume() {
        super.onResume()
        showButton()
    }

    private fun showConnect() {
        tv_soft_connect_hotspot.visibility = View.VISIBLE
        tv_soft_reconnect_hotspot.visibility = View.GONE
        tv_soft_connect_confirm.visibility = View.GONE
    }

    private fun showReconnect() {
        tv_soft_connect_hotspot.visibility = View.GONE
        tv_soft_reconnect_hotspot.visibility = View.VISIBLE
        tv_soft_connect_confirm.visibility = View.VISIBLE
    }

    private fun showButton() {
        if (isFirst) return
        val wifiManager =
            context!!.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        if (wifiManager.connectionInfo.bssid != null) {
            showReconnect()
        } else {
            showConnect()
        }
        isFirst = false
    }

    override fun startHere(view: View) {
        // 加载本地文案
        if (type == LoadViewTxtType.LoadLocalViewTxt.ordinal) {
            loadViewStandradInfo()
        } else {    // 异步加载远端配置文案
            loadViewInfo()
        }

        setListener()
    }

    private fun loadViewStandradInfo() {
        tv_hotspot_hint.visibility = View.VISIBLE
        tv_hotspot_hint.setText(R.string.soft_ap_hotspot_step_1)
        tv_soft_connect_hotspot_tip.visibility = View.VISIBLE
        tv_soft_connect_hotspot_tip.setText(R.string.soft_ap_hotspot_step_2)
    }

    private fun loadViewInfo() {
        tv_hotspot_hint.visibility = View.GONE
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

    private fun setListener() {
        tv_soft_connect_hotspot.setOnClickListener {
            startActivity(Intent(Settings.ACTION_WIFI_SETTINGS))
            isFirst = false
        }
        tv_soft_reconnect_hotspot.setOnClickListener {
            startActivity(Intent(Settings.ACTION_WIFI_SETTINGS))
        }
        tv_soft_connect_confirm.setOnClickListener {
            onNextListener?.onNext()
            isFirst = true
            showConnect()
        }
    }

    interface OnNextListener {
        fun onNext()
    }


}