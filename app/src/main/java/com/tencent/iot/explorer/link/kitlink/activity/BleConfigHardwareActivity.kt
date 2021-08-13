package com.tencent.iot.explorer.link.kitlink.activity

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.text.TextUtils
import android.view.View
import com.squareup.picasso.Picasso
import com.tencent.iot.explorer.link.App
import com.tencent.iot.explorer.link.R
import com.tencent.iot.explorer.link.T
import com.tencent.iot.explorer.link.core.auth.callback.MyCallback
import com.tencent.iot.explorer.link.core.auth.response.BaseResponse
import com.tencent.iot.explorer.link.core.auth.util.JsonManager
import com.tencent.iot.explorer.link.core.link.entity.BleDevice
import com.tencent.iot.explorer.link.customview.dialog.TipDialog
import com.tencent.iot.explorer.link.customview.dialog.entity.TipInfo
import com.tencent.iot.explorer.link.customview.progress.bean.StepBean
import com.tencent.iot.explorer.link.kitlink.consts.CommonField
import com.tencent.iot.explorer.link.kitlink.entity.ConfigType
import com.tencent.iot.explorer.link.kitlink.entity.ProdConfigDetailEntity
import com.tencent.iot.explorer.link.kitlink.response.ProductsConfigResponse
import com.tencent.iot.explorer.link.kitlink.util.HttpRequest
import com.tencent.iot.explorer.link.mvp.IPresenter
import kotlinx.android.synthetic.main.activity_ble_config_hardware.*
import java.util.*

class BleConfigHardwareActivity : PActivity() {

    companion object {
        fun startWithProductid(context: Context, productId: String) {
            if (TextUtils.isEmpty(productId)) {
                T.show(context.getString(R.string.no_product_info))
                return
            }
            var intent = Intent(context, BleConfigHardwareActivity::class.java)
            intent.putExtra(CommonField.PRODUCT_ID, productId)
            var dev = BleDevice()
            dev.productId = productId
            dev.indexWithDevname = false
            App.data.bleDevice = dev
            context.startActivity(intent)
        }
    }

    private var productId = ""
    private var tipInfo = TipInfo()

    override fun getContentView(): Int {
        return R.layout.activity_ble_config_hardware
    }

    private fun initStepView() {
        val stepsBeanList = ArrayList<StepBean>()
        stepsBeanList.add(StepBean(getString(R.string.config_hardware)))
        stepsBeanList.add(StepBean(getString(R.string.set_target_wifi)))
        stepsBeanList.add(StepBean(getString(R.string.start_config_network)))
        ble_step_progress.currentStep = 1
        ble_step_progress.setStepViewTexts(stepsBeanList)
        ble_step_progress.setTextSize(12)
    }

    override fun initView() {
        tipInfo.btn = getString(R.string.have_known)
        tipInfo.title = getString(R.string.reset_ble_dev_way)
        tipInfo.content = getString(R.string.reset_ble_dev_content)
        sv_content.visibility = View.GONE
        initStepView()
        if (intent.hasExtra(CommonField.PRODUCT_ID)) {
            productId = intent.getStringExtra(CommonField.PRODUCT_ID)
        }

        if (!TextUtils.isEmpty(productId)) {  // 尝试网络加载
            loadViewInfo(productId)
        } else {  // 本地加载
            sv_content.visibility = View.VISIBLE
        }
    }

    override fun setListener() {
        tv_ble_cancel.setOnClickListener { finish() }
        tv_ble_next.setOnClickListener {
            val intent = Intent(this, WifiActivity::class.java)
            intent.putExtra(CommonField.PRODUCT_ID, productId)
            intent.putExtra(CommonField.CONFIG_TYPE, ConfigType.BleConfig.id)
            startActivity(intent)
        }
        tv_more_guide.setOnClickListener {
            var dlg = TipDialog(this@BleConfigHardwareActivity, tipInfo)
            dlg.show()
        }
    }

    override fun getPresenter(): IPresenter? {
        return null
    }

    private fun loadViewInfo(productId: String?) {
        productId?:let { return }

        var productsList  = arrayListOf<String>()
        productsList.add(productId)
        HttpRequest.instance.getProductsConfig(productsList, productListener)
    }

    private var productListener =  object :MyCallback{
        override fun fail(msg: String?, reqCode: Int) {
            sv_content.visibility = View.VISIBLE
        }

        override fun success(response: BaseResponse, reqCode: Int) {
            sv_content.visibility = View.VISIBLE
            if (!response.isSuccess()) return

            response.parse(ProductsConfigResponse::class.java)?.run {
                var config = JsonManager.parseJson(Data[0].Config, ProdConfigDetailEntity::class.java)
                config?.let {
                    it.wifiLLSyncBle?.hardwareGuide?.let { content ->
                        if (!TextUtils.isEmpty(content.bgImg)) {
                            Picasso.get().load(content.bgImg).into(iv_soft_ap)
                        }

                        if (!TextUtils.isEmpty(content.guide)) {
                            tv_ble_content.text = content.guide
                        }

                        if (!TextUtils.isEmpty(content.btnText)) {
                            tv_ble_next.text = content.btnText
                        }

                        if (!TextUtils.isEmpty(content.message)) {
                            tipInfo.content = content.message
                        }
                    }
                }
            }
        }
    }
}