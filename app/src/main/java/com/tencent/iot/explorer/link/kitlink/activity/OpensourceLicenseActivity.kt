package com.tencent.iot.explorer.link.kitlink.activity

import android.content.Context
import android.content.Intent
import android.view.View
import android.webkit.*
import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.JSONObject
import com.tencent.iot.explorer.link.R
import com.tencent.iot.explorer.link.T
import com.tencent.iot.explorer.link.kitlink.consts.CommonField
import com.tencent.iot.explorer.link.kitlink.util.HttpRequest
import com.tencent.iot.explorer.link.kitlink.util.MyCustomCallBack
import com.tencent.iot.explorer.link.kitlink.util.RequestCode
import kotlinx.android.synthetic.main.activity_opensource_license.*
import kotlinx.android.synthetic.main.menu_back_layout.*
import java.util.*

/**
 * 开源软件信息
 */
class OpensourceLicenseActivity : BaseActivity(), MyCustomCallBack {

    override fun getContentView(): Int {
        return R.layout.activity_opensource_license
    }

    companion object {

        var urlSet = Arrays.asList(CommonField.OPENSOURCE_LICENSE_URL_EN, CommonField.OPENSOURCE_LICENSE_URL_ZH,
            CommonField.PRIVACY_POLICY_URL_CN_EN, CommonField.PRIVACY_POLICY_URL_US_ZH, CommonField.PRIVACY_POLICY_URL_US_EN,
            CommonField.SERVICE_AGREEMENT_URL_CN_EN, CommonField.SERVICE_AGREEMENT_URL_US_ZH, CommonField.SERVICE_AGREEMENT_URL_US_EN,
            CommonField.DELET_ACCOUNT_POLICY_EN)

        fun startWebWithExtra(context: Context, title: String, url: String) {
            val intent = Intent(context, OpensourceLicenseActivity::class.java)
            intent.putExtra(CommonField.EXTRA_TITLE, title)
            intent.putExtra(CommonField.EXTRA_TEXT, url)
            context.startActivity(intent)
        }
    }

    override fun initView() {
        iv_back.setColorFilter(resources.getColor(R.color.black_15161A))
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
            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                if (urlSet.contains(request?.url.toString())) {
                    getOpensourceLicense(request?.url.toString())
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
        HttpRequest.instance.httpGetOpensourceLicense(url, this, RequestCode.get_opensource_license)
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
            wv_web.loadDataWithBaseURL(null, fileContentJson, "text/html", "utf-8", null);
        }
    }
}