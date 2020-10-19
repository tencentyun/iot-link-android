package com.tencent.iot.explorer.link.kitlink.fragment

import android.text.TextUtils
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.alibaba.fastjson.JSONObject
import com.squareup.picasso.Picasso
import com.tencent.iot.explorer.link.App
import com.tencent.iot.explorer.link.R
import com.tencent.iot.explorer.link.core.auth.util.JsonManager
import com.tencent.iot.explorer.link.kitlink.consts.CommonField
import com.tencent.iot.explorer.link.kitlink.consts.LoadViewTxtType
import com.tencent.iot.explorer.link.kitlink.entity.HardwareGuide
import com.tencent.iot.explorer.link.kitlink.entity.ProdConfigDetailEntity
import com.tencent.iot.explorer.link.kitlink.response.BaseResponse
import com.tencent.iot.explorer.link.kitlink.response.ProductsConfigResponse
import com.tencent.iot.explorer.link.kitlink.util.HttpRequest
import com.tencent.iot.explorer.link.kitlink.util.MyCallback
import com.tencent.iot.explorer.link.mvp.IPresenter
import com.tencent.iot.explorer.link.T
import kotlinx.android.synthetic.main.smart_config_first.*

/**
 * SmartConfig步骤展示
 */
class SCStepFragment(type: Int, productId: String) : BaseFragment() {

    var onNextListener: OnNextListener? = null
    private var tipContent: TextView? = null
    private var pic: ImageView? = null
    private var nextBtn: TextView? = null

    private var type: Int = type
    private var productId = ""

    init {
        this.productId = productId
    }

    constructor() : this( LoadViewTxtType.LoadLocalViewTxt.ordinal, "") {}

    override fun getPresenter(): IPresenter? {
        return null
    }

    override fun getContentView(): Int {
        return R.layout.fragment_smart_config_step
    }

    override fun startHere(view: View) {
        tipContent = view.findViewById(R.id.tv_smart_config_hint)
        pic = view.findViewById(R.id.iv_smart_config)
        nextBtn = view.findViewById(R.id.tv_smart_first_commit)

        // 加载本地文案
        if (type == LoadViewTxtType.LoadLocalViewTxt.ordinal) {
            loadViewStandradInfo()
        } else {    // 加载远端配置的文案
            loadViewInfo()
        }
        tv_smart_first_commit.setOnClickListener { onNextListener?.onNext() }
    }

    // 网络请求成功且返回自定义的内容，调用页面内容加载方法，网络请求失败无需调用
    private fun loadViewInfo() {
        if (TextUtils.isEmpty(productId)) {
            return
        }

        val productsList  = arrayListOf<String>()
        productsList.add(productId)
        HttpRequest.instance.getProductsConfig(productsList, object: MyCallback{
            override fun fail(msg: String?, reqCode: Int) {
                T.show(msg)
            }

            override fun success(response: BaseResponse, reqCode: Int) {
                response.parse(ProductsConfigResponse::class.java)?.run {
                    val config = JsonManager.parseJson(Data[0].Config, ProdConfigDetailEntity::class.java)

                    if (TextUtils.isEmpty(config.WifiSmartConfig)) {
                        loadViewStandradInfo()
                        return
                    }

                    var json = JSONObject.parseObject(config.WifiSmartConfig)
                    if (json.containsKey(CommonField.HARD_WARE_GUIDE)) {
                        var hardwareGuide = JSONObject.parseObject(json.getString(CommonField.HARD_WARE_GUIDE), HardwareGuide::class.java)
                        if (!TextUtils.isEmpty(hardwareGuide.btnText)) {
                            nextBtn?.setText(hardwareGuide.btnText)
                        } else {
                            nextBtn?.setText(R.string.smart_config_first_title)
                        }

                        if (!TextUtils.isEmpty(hardwareGuide.message)) {
                            tipContent?.setText(hardwareGuide.message)
                        } else {
                            tipContent?.setText(R.string.smart_config_first_hint2)
                        }

                        if (TextUtils.isEmpty(hardwareGuide.bgImg)) {
                            pic?.setImageResource(R.mipmap.image_smart_config)
                        } else {
                            Picasso.get().load(hardwareGuide.bgImg)
                                .placeholder(R.drawable.imageselector_default_error)
                                .resize(App.data.screenWith / 5, App.data.screenWith / 5)
                                .centerCrop()
                                .into(pic)
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
        nextBtn?.setText(R.string.smart_config_first_title)
        tipContent?.setText(R.string.smart_config_first_hint2)
        pic?.setImageResource(R.mipmap.image_smart_config)
    }

    interface OnNextListener {
        fun onNext()
    }

}