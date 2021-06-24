package com.tencent.iot.explorer.link.kitlink.activity

import android.annotation.SuppressLint
import android.net.Uri
import android.os.Build
import android.view.View
import android.webkit.*
import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.JSONObject
import com.tencent.iot.explorer.link.App
import com.tencent.iot.explorer.link.R
import com.tencent.iot.explorer.link.T
import com.tencent.iot.explorer.link.core.auth.callback.MyCallback
import com.tencent.iot.explorer.link.core.auth.response.BaseResponse
import com.tencent.iot.explorer.link.core.log.L
import com.tencent.iot.explorer.link.core.utils.Utils
import com.tencent.iot.explorer.link.kitlink.consts.CommonField
import com.tencent.iot.explorer.link.kitlink.util.HttpRequest
import com.tencent.iot.explorer.link.kitlink.util.RequestCode
import com.tencent.iot.explorer.link.kitlink.webview.BridgeImpl
import com.tencent.iot.explorer.link.kitlink.webview.JSBridgeKt
import com.tencent.iot.explorer.link.kitlink.webview.WebCallBack
import kotlinx.android.synthetic.main.activity_device_panel.*
import kotlinx.android.synthetic.main.menu_back_layout.*

class BuleToothActivity: BaseActivity(), View.OnClickListener, MyCallback {

    private val H5_PANEL_BASE_URL = "https://iot.cloud.tencent.com/scf/h5panel/bluetooth-search"
    private var callback: WebCallBack? = null
    private var productId = ""

    override fun getContentView(): Int {
        return  R.layout.activity_device_panel
    }

    override fun initView() {
        iv_back.setColorFilter(R.color.black_333333)
        tv_title.text = ""
        var bundle = intent.getBundleExtra(CommonField.EXTRA_INFO)
        if (bundle != null) {
            productId = bundle.getString(CommonField.EXTRA_INFO, "")
        }
        getAppGetTokenTicket()
    }

    private fun getAppGetTokenTicket() {
        HttpRequest.instance.getOneTimeTokenTicket(this)
    }

    override fun setListener() {
        iv_back.setOnClickListener { finish() }
    }

    override fun onClick(v: View?) {
    }

    override fun fail(msg: String?, reqCode: Int) {
        msg?.let { L.e(it) }
    }

    override fun success(response: BaseResponse, reqCode: Int) {
        when (reqCode) {
            RequestCode.token_ticket -> {
                if (response.isSuccess()) {
                    JSBridgeKt.register("help_center_bridge", BridgeImpl::class.java)
                    val ticketResponse = JSON.parse(response.data.toString()) as JSONObject
                    var url = H5_PANEL_BASE_URL +
                            "?productId=${productId}" +
                            "&uin=${Utils.getAndroidID(this)}" +
                            "&lid=${App.data.appLifeCircleId}" +
                            "&quid=${Utils.getAndroidID(this)}" +
                            "&ticket=${ticketResponse[CommonField.TOKEN_TICKET]}" +
                            "&appID=${T.getContext().applicationInfo.packageName}" +
                            "&platform=${HttpRequest.PLATFORM_TAG}" +
                            "&regionId=${App.data.regionId}"
                    showUrl(url)
                } else {
                    T.show(response.msg)
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        device_panel_webview.clearHistory()
        device_panel_webview.clearCache(true)
        device_panel_webview.loadUrl("about:blank")
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun showUrl(url: String) {
        device_panel_webview.settings.javaScriptEnabled = true  // 设置js支持
        device_panel_webview.settings.domStorageEnabled = true  // 开启 DOM storage API 功能
        device_panel_webview.settings.textZoom = 100            // 设置网页字体不跟随系统字体发生改变
        device_panel_webview.settings.useWideViewPort = true    // 缩放至屏幕的大小
        device_panel_webview.settings.loadWithOverviewMode = true
        device_panel_webview.settings.setSupportZoom(true) // 是否支持缩放
        device_panel_webview.settings.builtInZoomControls = true
        device_panel_webview.settings.displayZoomControls = false
        device_panel_webview.settings.cacheMode = WebSettings.LOAD_NO_CACHE  // 不缓存
        device_panel_webview.settings.allowContentAccess = true

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            device_panel_webview.settings.mixedContentMode = WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE;
        }
        device_panel_webview.settings.blockNetworkImage = false
        device_panel_webview.loadUrl(url)
        device_panel_webview.webViewClient = webViewClient
        device_panel_webview.webChromeClient = webChromeClient

        callback = WebCallBack(device_panel_webview)
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
            result.confirm(JSBridgeKt.callNative(view, message))
            return true
        }
    }

    var webViewClient = object: WebViewClient() {
        override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {

            if (url.contains("iot-explorer-help-center://")) {
                JSBridgeKt.callNative(view, url)
            } else {
                view.loadUrl(url)
            }
            return true
        }
    }

}