package com.tencent.iot.explorer.link.kitlink.activity

import android.annotation.SuppressLint
import android.net.Uri
import android.os.Build
import android.view.View
import android.webkit.*
import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.JSONObject
import com.tencent.iot.explorer.link.R
import com.tencent.iot.explorer.link.T
import com.tencent.iot.explorer.link.core.auth.callback.MyCallback
import com.tencent.iot.explorer.link.core.auth.response.BaseResponse
import com.tencent.iot.explorer.link.core.log.L
import com.tencent.iot.explorer.link.kitlink.consts.CommonField
import com.tencent.iot.explorer.link.kitlink.util.HttpRequest
import kotlinx.android.synthetic.main.activity_feedback_for_h5.*
import kotlinx.android.synthetic.main.menu_back_layout.*

class FeedbackForH5Activity : BaseActivity(), View.OnClickListener, MyCallback {

    override fun getContentView(): Int {
        return R.layout.activity_feedback_for_h5
    }

    override fun initView() {
        iv_back.setColorFilter(R.color.black_333333)
        tv_title.text = ""
        getAppGetTokenTicket()
    }

    override fun setListener() {
        iv_back.setOnClickListener { finish() }
    }

    override fun onClick(v: View?) { }

    override fun fail(msg: String?, reqCode: Int) {
        msg?.let { L.e(it) }
    }

    override fun success(response: BaseResponse, reqCode: Int) {
        if (response.isSuccess()) {
            val ticketResponse = JSON.parse(response.data.toString()) as JSONObject
            val url = "https://iot.cloud.tencent.com/explorer-h5/help-center/?" +
                    "&appID=${T.getContext().applicationInfo.packageName}" +
                    "&ticket=${ticketResponse[CommonField.TOKEN_TICKET]}" +
                    "#/pages/User/Feedback/Feedback"
            showUrl(url)
        } else {
            L.e(response.msg)
        }
    }

    private fun getAppGetTokenTicket() {
        HttpRequest.instance.getOneTimeTokenTicket(this)
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun showUrl(url: String) {
        feedback_detail_web.settings.javaScriptEnabled = true  // 设置js支持
        feedback_detail_web.settings.domStorageEnabled = true  // 开启 DOM storage API 功能
        feedback_detail_web.settings.textZoom = 100            // 设置网页字体不跟随系统字体发生改变
        feedback_detail_web.settings.useWideViewPort = true    // 缩放至屏幕的大小
        feedback_detail_web.settings.loadWithOverviewMode = true
        feedback_detail_web.settings.setSupportZoom(true) // 是否支持缩放
        feedback_detail_web.settings.builtInZoomControls = true
        feedback_detail_web.settings.displayZoomControls = false
        feedback_detail_web.settings.cacheMode = WebSettings.LOAD_NO_CACHE  // 不缓存
        feedback_detail_web.settings.allowContentAccess = true

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            feedback_detail_web.settings.mixedContentMode = WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE;
        }
        feedback_detail_web.settings.blockNetworkImage = false
        feedback_detail_web.webChromeClient = webChromeClient
        feedback_detail_web.webViewClient = webViewClient
        feedback_detail_web.loadUrl(url)
    }

    private val webChromeClient = object: WebChromeClient() {
        override fun onShowFileChooser (
            webView: WebView,
            filePathCallback: ValueCallback<Array<Uri>>,
            fileChooserParams: FileChooserParams?): Boolean {
            return true
        }
        override fun onReceivedTitle(view: WebView?, title: String?) {
            super.onReceivedTitle(view, title)
            tv_title.text = title
        }
        override fun onReceivedTouchIconUrl(view: WebView?, url: String?, precomposed: Boolean) { }
        override fun onJsPrompt(view: WebView, url: String, message: String, defaultValue: String, result: JsPromptResult): Boolean {
            return true
        }
    }
    private val webViewClient = object: WebViewClient() {
        override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
            return true
        }
    }
}