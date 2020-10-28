package com.tencent.iot.explorer.link.kitlink.fragment

import android.text.TextUtils
import android.view.View
import com.alibaba.fastjson.JSONObject
import com.tencent.iot.explorer.link.R
import com.tencent.iot.explorer.link.T
import com.tencent.iot.explorer.link.core.auth.response.BaseResponse
import com.tencent.iot.explorer.link.core.auth.util.JsonManager
import com.tencent.iot.explorer.link.kitlink.consts.CommonField
import com.tencent.iot.explorer.link.kitlink.consts.LoadViewTxtType
import com.tencent.iot.explorer.link.kitlink.entity.ConnectApGuide
import com.tencent.iot.explorer.link.kitlink.entity.ProdConfigDetailEntity
import com.tencent.iot.explorer.link.kitlink.response.ProductsConfigResponse
import com.tencent.iot.explorer.link.kitlink.util.HttpRequest
import com.tencent.iot.explorer.link.kitlink.util.MyCallback
import com.tencent.iot.explorer.link.mvp.IPresenter
import kotlinx.android.synthetic.main.fragment_soft_hotspot.*

class SoftHotspotFragment(type: Int, productId: String) : BaseFragment() {

    var onNextListener: OnNextListener? = null
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

    override fun startHere(view: View) {
        // 加载本地文案
        if (type == LoadViewTxtType.LoadLocalViewTxt.ordinal) {
            loadViewStandradInfo()
        } else {    // 异步加载远端配置文案
            loadViewInfo()
        }

        tv_soft_connect_hotspot.setOnClickListener {
            if (onNextListener != null) {
                onNextListener?.onNext()
            }
        }
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

    interface OnNextListener {
        fun onNext()
    }
}