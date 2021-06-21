package com.tencent.iot.explorer.link.kitlink.activity

import android.content.Context
import android.content.Intent
import android.text.TextUtils
import android.util.Log
import com.alibaba.fastjson.JSONObject
import com.squareup.picasso.Picasso
import com.tencent.iot.explorer.link.App
import com.tencent.iot.explorer.link.R
import com.tencent.iot.explorer.link.T
import com.tencent.iot.explorer.link.core.auth.response.BaseResponse
import com.tencent.iot.explorer.link.core.auth.util.JsonManager
import com.tencent.iot.explorer.link.customview.progress.bean.StepBean
import com.tencent.iot.explorer.link.kitlink.consts.CommonField
import com.tencent.iot.explorer.link.kitlink.consts.LoadViewTxtType
import com.tencent.iot.explorer.link.kitlink.entity.HardwareGuide
import com.tencent.iot.explorer.link.kitlink.entity.ProdConfigDetailEntity
import com.tencent.iot.explorer.link.kitlink.fragment.DeviceFragment
import com.tencent.iot.explorer.link.kitlink.response.ProductsConfigResponse
import com.tencent.iot.explorer.link.kitlink.util.HttpRequest
import com.tencent.iot.explorer.link.core.auth.callback.MyCallback
import com.tencent.iot.explorer.link.mvp.IPresenter
import kotlinx.android.synthetic.main.activity_smart_config_step.*
import java.util.ArrayList

class SmartConfigStepActivity : PActivity() {

    private var loadViewTextType =
        LoadViewTxtType.LoadLocalViewTxt.ordinal // 0 加载本地文案  1 尝试加载远端配置文案
    private var productId = ""
    private var type = DeviceFragment.ConfigType.SmartConfig.id

    override fun getContentView(): Int {
        return R.layout.activity_smart_config_step
    }

    companion object {
        fun startActivityWithExtra(context: Context, productId: String?) {
            val intent = Intent(context, SmartConfigStepActivity::class.java)
            if (!TextUtils.isEmpty(productId)) {
                intent.putExtra(CommonField.LOAD_VIEW_TXT_TYPE, LoadViewTxtType.LoadRemoteViewTxt.ordinal)
                intent.putExtra(CommonField.PRODUCT_ID, productId)
            }
            context.startActivity(intent)
        }
    }

    private fun refreshTypeView() {
        tv_soft_ap_title.setText(R.string.smart_config_config_network)
        tip_title.setText(R.string.smart_config_first)
        tv_soft_banner_title.setText(R.string.smart_config_first_hint2)
        val stepsBeanList = ArrayList<StepBean>()
        stepsBeanList.add(StepBean(getString(R.string.config_hardware)))
        stepsBeanList.add(StepBean(getString(R.string.select_wifi)))
        stepsBeanList.add(StepBean(getString(R.string.start_config_network)))
        softap_step_progress.currentStep = 1
        softap_step_progress.setStepViewTexts(stepsBeanList)
        softap_step_progress.setTextSize(12)
    }

    override fun initView() {
        loadViewTextType = intent.getIntExtra(
            CommonField.LOAD_VIEW_TXT_TYPE,
            LoadViewTxtType.LoadLocalViewTxt.ordinal
        )
        if (loadViewTextType != LoadViewTxtType.LoadLocalViewTxt.ordinal) {
            productId = intent.getStringExtra(CommonField.PRODUCT_ID)
        }
        type = intent.getIntExtra(CommonField.CONFIG_TYPE, DeviceFragment.ConfigType.SmartConfig.id)
        if (loadViewTextType == LoadViewTxtType.LoadLocalViewTxt.ordinal) {
            loadViewStandradInfo()
        } else {
            loadViewInfo()
        }
        refreshTypeView()
    }

    override fun setListener() {
        tv_soft_ap_cancel.setOnClickListener { finish() }
        tv_soft_first_commit.setOnClickListener {
            val intent = Intent(this, WifiActivity::class.java)
            intent.putExtra(
                CommonField.LOAD_VIEW_TXT_TYPE,
                LoadViewTxtType.LoadRemoteViewTxt.ordinal
            )
            intent.putExtra(CommonField.PRODUCT_ID, productId)
            intent.putExtra(CommonField.CONFIG_TYPE, type)
            startActivity(intent)
        }
    }

    override fun getPresenter(): IPresenter? {
        return null;
    }

    // 网络请求成功且返回自定义的内容，调用页面内容加载方法，网络请求失败无需调用
    private fun loadViewInfo() {
        if (TextUtils.isEmpty(productId)) {
            return
        }

        val productsList = arrayListOf<String>()
        productsList.add(productId)
        HttpRequest.instance.getProductsConfig(productsList, object : MyCallback {
            override fun fail(msg: String?, reqCode: Int) {
                T.show(msg)
            }

            override fun success(response: BaseResponse, reqCode: Int) {
                response.parse(ProductsConfigResponse::class.java)?.run {
                    val config =
                        JsonManager.parseJson(Data[0].Config, ProdConfigDetailEntity::class.java)

                    if (TextUtils.isEmpty(config.WifiSmartConfig)) {
                        loadViewStandradInfo()
                        return
                    }
                    var json = JSONObject.parseObject(config.WifiSmartConfig)
                    if (json.containsKey(CommonField.HARD_WARE_GUIDE)) {

                        var hardwareGuide = JSONObject.parseObject(
                            json.getString(CommonField.HARD_WARE_GUIDE),
                            HardwareGuide::class.java
                        )
                        if (TextUtils.isEmpty(hardwareGuide.btnText)) {
                            tv_soft_first_commit?.setText(R.string.soft_ap_first_button)
                        } else {
                            tv_soft_first_commit?.setText(hardwareGuide.btnText)
                        }

                        if (TextUtils.isEmpty(hardwareGuide.message)) {
                            tv_soft_banner_title?.setText(R.string.smart_config_first_hint2)
                        } else {
                            tv_soft_banner_title?.setText(hardwareGuide.message)
                        }

                        if (TextUtils.isEmpty(hardwareGuide.bgImg)) {
                            iv_soft_ap?.setImageResource(R.mipmap.image_soft_ap)
                        } else {
                            Picasso.get().load(hardwareGuide.bgImg)
                                .placeholder(R.mipmap.imageselector_default_error)
                                .into(iv_soft_ap)
                        }
                    } else {
                        loadViewStandradInfo()
                    }
                }
            }
        })
    }

    // 网络请求成功且返回标准内容
    private fun loadViewStandradInfo() {
        tv_soft_first_commit?.setText(R.string.soft_ap_first_button)
        tv_soft_banner_title.setText(R.string.smart_config_first_hint2)
        iv_soft_ap?.setImageResource(R.mipmap.image_smart_config)
    }
}