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
import com.tencent.iot.explorer.link.kitlink.util.HttpRequest
import kotlinx.android.synthetic.main.activity_product_introducation.*
import kotlinx.android.synthetic.main.menu_back_layout.*

class ProductIntroduceActivity : BaseActivity(), MyCallback {
    private var config: ProdConfigDetailEntity? = null
    private var productId = ""
    private var handler = Handler()

    override fun getContentView(): Int {
        return R.layout.activity_product_introducation
    }

    override fun initView() {
        tv_title.setText(getString(R.string.bind_dev))
        loadRemoteRes()
    }

    private fun loadRemoteRes() {
        var extra = intent.getStringExtra(CommonField.EXTRA_INFO)
        config = JSONObject.parseObject(extra, ProdConfigDetailEntity::class.java)
        var productGlobal = JSONObject.parseObject(config?.Global, ProductGlobal::class.java)

        if (TextUtils.isEmpty(productGlobal.IconUrlAdvertise)) {
            iv_product_intrduce?.setImageResource(R.mipmap.product_intrduce)
        } else {
            Picasso.get().load(productGlobal.IconUrlAdvertise)
                .placeholder(R.drawable.imageselector_default_error)
                .into(iv_product_intrduce)
        }

        if (TextUtils.isEmpty(productGlobal.addDeviceHintMsg)) {
            tv_use_tip.setText("")
        } else {
            tv_use_tip.setText(productGlobal.addDeviceHintMsg)
        }

        if (!TextUtils.isEmpty(config?.profile)) {
            var jsonProFile = JSON.parseObject(config?.profile)
            if (jsonProFile != null && jsonProFile.containsKey("ProductId") &&
                !TextUtils.isEmpty(jsonProFile.getString("ProductId"))) {
                productId = jsonProFile.getString("ProductId")
            }
        }

        HttpRequest.instance.deviceProducts(arrayListOf(productId), this)
    }

    override fun setListener() {
        iv_back.setOnClickListener { finish() }
        tv_bind_now.setOnClickListener { configNet4Dev() }
    }

    private fun configNet4Dev() {

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

        if (!TextUtils.isEmpty(response.data.toString())) {
            var products = JSON.parseObject(response.data.toString(), ProductsEntity::class.java)
            if (products == null || products.Products == null) return

            for (i in 0 until products!!.Products!!.size) {
                var productEntity = JSON.parseObject(products!!.Products!!.getString(i), ProductEntity::class.java)
                if (productEntity.ProductId == productId) { // 匹配到 productId 一致的数据，则显示返回的产品名
                    handler.post {
                        tv_product_name.setText(productEntity.Name)
                    }
                    return
                }
            }

            // 没有找到对应 productId 的产品名，显示默认信息
            handler.post {
                tv_product_name.setText(R.string.default_product_name)
            }
        }
    }
}