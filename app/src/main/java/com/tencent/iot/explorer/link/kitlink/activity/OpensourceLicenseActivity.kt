package com.tencent.iot.explorer.link.kitlink.activity

import android.graphics.Color
import android.graphics.Typeface
import android.text.Html
import android.view.View
import android.webkit.*
import android.widget.TextView
import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.JSONArray
import com.alibaba.fastjson.JSONObject
import com.tencent.iot.explorer.link.R
import com.tencent.iot.explorer.link.T
import com.tencent.iot.explorer.link.core.auth.util.JsonManager
import com.tencent.iot.explorer.link.kitlink.consts.CommonField
import com.tencent.iot.explorer.link.kitlink.entity.OpensourceContentEntity
import com.tencent.iot.explorer.link.kitlink.util.HttpRequest
import com.tencent.iot.explorer.link.kitlink.util.MyCustomCallBack
import com.tencent.iot.explorer.link.kitlink.util.RequestCode
import kotlinx.android.synthetic.main.activity_opensource_license.*
import kotlinx.android.synthetic.main.activity_opensource_license.wv_web
import kotlinx.android.synthetic.main.activity_web.*
import kotlinx.android.synthetic.main.menu_back_layout.*
import java.net.URLEncoder


/**
 * 开源软件信息
 */
class OpensourceLicenseActivity : BaseActivity(), MyCustomCallBack {
    override fun getContentView(): Int {
        return R.layout.activity_opensource_license
    }

    override fun initView() {
        iv_back.setColorFilter(resources.getColor(R.color.black_333333))
        intent.getStringExtra(CommonField.EXTRA_TITLE)?.let {
            tv_title.text = it
        }

        intent.getStringExtra(CommonField.EXTRA_TEXT)?.let {
            getOpensourceLicense(it)
        }

        wv_web.settings.javaScriptEnabled = true
        wv_web.settings.domStorageEnabled = true
        wv_web.settings.useWideViewPort = true
        wv_web.settings.loadWithOverviewMode = true
        wv_web.settings.layoutAlgorithm = WebSettings.LayoutAlgorithm.TEXT_AUTOSIZING

        val mWebViewClient = object : WebViewClient(){
            override fun shouldOverrideUrlLoading(
                view: WebView?,
                request: WebResourceRequest?
            ): Boolean {
                if (request?.url.toString() == CommonField.OPENSOURCE_LICENSE_URL) { //开源软件信息
                    getOpensourceLicense(CommonField.OPENSOURCE_LICENSE_URL)
                    return false
                } else if (request?.url.toString() == CommonField.PRIVACY_POLICY_URL) { //隐私政策
                    getOpensourceLicense(CommonField.PRIVACY_POLICY_URL)
                    return false
                } else if (request?.url.toString() == CommonField.SERVICE_AGREEMENT_URL) { //用户协议
                    getOpensourceLicense(CommonField.SERVICE_AGREEMENT_URL)
                    return false
                } else {
                    return super.shouldOverrideUrlLoading(view, request)
                }
            }
        }
        wv_web.webViewClient = mWebViewClient
        wv_web.webChromeClient = WebChromeClient()
        wv_web.visibility = View.VISIBLE
    }

    override fun setListener() {
        iv_back.setOnClickListener { finish() }
    }

    private fun getOpensourceLicense(url: String) {
        HttpRequest.instance.getOpensourceLicense(url, this, RequestCode.get_opensource_license)
    }

    override fun fail(msg: String?, reqCode: Int) {
        T.show(msg)
    }

    override fun success(str: String, reqCode: Int) {
        getOpensourceLicenseJsonString(str)
    }

    private fun getOpensourceLicenseJsonString(str: String) {
        val startString = ";(function(){var params="
        val endString = ";\ntypeof callback_"
        val start = str.indexOf(startString, 0)
        val end = str.indexOf(endString, 0)
        val opensourceLicenseJson = str.substring(start + startString.length, end)
        val jsonObject = JSON.parse(opensourceLicenseJson) as JSONObject
        if (jsonObject.containsKey("filecontent")) {
            val fileContentJson = jsonObject.getString("filecontent")
            wv_web.loadData(fileContentJson, "text/html;charset=UTF-8",  null)
        }
    }
}