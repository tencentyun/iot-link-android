package com.tencent.iot.explorer.link.kitlink.activity

import android.os.Handler
import android.text.TextUtils
import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.JSONObject
import com.squareup.picasso.Picasso
import com.tencent.iot.explorer.link.R
import com.tencent.iot.explorer.link.T
import com.tencent.iot.explorer.link.core.auth.callback.MyCallback
import com.tencent.iot.explorer.link.core.auth.response.BaseResponse
import com.tencent.iot.explorer.link.core.auth.util.JsonManager
import com.tencent.iot.explorer.link.kitlink.consts.CommonField
import com.tencent.iot.explorer.link.kitlink.entity.*
import com.tencent.iot.explorer.link.kitlink.response.ProductsConfigResponse
import com.tencent.iot.explorer.link.kitlink.util.HttpRequest
import com.tencent.iot.explorer.link.kitlink.util.RequestCode
import kotlinx.android.synthetic.main.activity_product_introducation.*
import kotlinx.android.synthetic.main.menu_back_layout.*

class ProductIntroduceActivity : BaseActivity(), MyCallback {
    private var config: ProdConfigDetailEntity? = null
    private var productId = ""

    override fun getContentView(): Int {
        return R.layout.activity_product_introducation
    }

    override fun initView() {
        tv_title.setText(getString(R.string.bind_dev))
        productId = intent.getStringExtra(CommonField.EXTRA_INFO)
        HttpRequest.instance.getProductsConfig(arrayListOf(productId), this)
    }

    private fun loadRemoteRes(productGlobalStr: String) {
        var productGlobal = JSON.parseObject(productGlobalStr, ProductGlobal::class.java)
        if (productGlobal == null) {
            productGlobal = ProductGlobal()
        }
        loadRemoteRes(productGlobal)
    }

    private fun loadRemoteRes(productGlobal: ProductGlobal) {

        if (TextUtils.isEmpty(productGlobal.IconUrlAdvertise)) {
            iv_product_intrduce?.setImageResource(R.mipmap.product_intrduce)
        } else {
            Picasso.get().load(productGlobal.IconUrlAdvertise)
                .placeholder(R.mipmap.imageselector_default_error)
                .into(iv_product_intrduce)
        }

        if (TextUtils.isEmpty(productGlobal.addDeviceHintMsg)) {
            tv_use_tip.setText("")
        } else {
            tv_use_tip.setText(productGlobal.addDeviceHintMsg)
        }

        HttpRequest.instance.deviceProducts(arrayListOf(productId), this)
    }

    override fun setListener() {
        iv_back.setOnClickListener { finish() }
        tv_bind_now.setOnClickListener { configNet4Dev() }
    }

    private fun configNet4Dev() {

        if (config == null || config!!.WifiConfTypeList == null) return

        val wifiConfigTypeList = config!!.WifiConfTypeList
        if (wifiConfigTypeList.equals("{}") || TextUtils.isEmpty(wifiConfigTypeList)) {
            SmartConfigStepActivity.startActivityWithExtra(this@ProductIntroduceActivity, productId)

        } else if (wifiConfigTypeList.contains("[")) {
            val typeList = JsonManager.parseArray(wifiConfigTypeList)
            if (typeList.size > 0 && typeList[0] == "softap") {
                SoftApStepActivity.startActivityWithExtra(this@ProductIntroduceActivity, productId)
            } else {
                SmartConfigStepActivity.startActivityWithExtra(this@ProductIntroduceActivity, productId)
            }
        }
    }

    override fun fail(msg: String?, reqCode: Int) {
        T.show(msg)
    }

    override fun success(response: BaseResponse, reqCode: Int) {
        if (!response.isSuccess()) {
            T.show(response.msg)
            return
        }

        when(reqCode) {
            RequestCode.get_products_config -> {
                response.parse(ProductsConfigResponse::class.java)?.run {
                    config = JsonManager.parseJson(Data[0].Config, ProdConfigDetailEntity::class.java)
                    if (config != null) {
                        loadRemoteRes(config!!.Global)
                    }
                }
            }

            RequestCode.device_product -> {
                if (!TextUtils.isEmpty(response.data.toString())) {
                    var products = JSON.parseObject(response.data.toString(), ProductsEntity::class.java)
                    if (products == null || products.Products == null) return

                    for (i in 0 until products!!.Products!!.size) {
                        var productEntity = JSON.parseObject(products!!.Products!!.getString(i), ProductEntity::class.java)
                        if (productEntity.ProductId == productId) { // 匹配到 productId 一致的数据，则显示返回的产品名
                            tv_product_name.setText(productEntity.Name)
                            return
                        }
                    }

                    tv_product_name.setText(R.string.default_product_name)
                }
            }
        }
    }
}