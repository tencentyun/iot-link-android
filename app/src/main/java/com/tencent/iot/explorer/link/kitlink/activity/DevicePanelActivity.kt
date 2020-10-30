package com.tencent.iot.explorer.link.kitlink.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.text.TextUtils
import android.view.View
import android.webkit.*
import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.JSONObject
import com.tencent.iot.explorer.link.App
import com.tencent.iot.explorer.link.AppLifeCircleListener
import com.tencent.iot.explorer.link.R
import com.tencent.iot.explorer.link.T
import com.tencent.iot.explorer.link.core.auth.entity.DeviceEntity
import com.tencent.iot.explorer.link.core.auth.response.BaseResponse
import com.tencent.iot.explorer.link.core.log.L
import com.tencent.iot.explorer.link.core.utils.Utils
import com.tencent.iot.explorer.link.kitlink.consts.CommonField
import com.tencent.iot.explorer.link.kitlink.popup.EditPopupWindow
import com.tencent.iot.explorer.link.kitlink.util.HttpRequest
import com.tencent.iot.explorer.link.kitlink.util.MyCallback
import com.tencent.iot.explorer.link.kitlink.util.RequestCode
import com.tencent.iot.explorer.link.kitlink.webview.WebCallBack
import kotlinx.android.synthetic.main.activity_device_details.*
import kotlinx.android.synthetic.main.activity_device_panel.*
import kotlinx.android.synthetic.main.menu_back_layout.*

class DevicePanelActivity: BaseActivity(), View.OnClickListener, MyCallback, AppLifeCircleListener {

    private val H5_PANEL_BASE_URL = "https://iot.cloud.tencent.com/scf/h5panel/"
    private var callback: WebCallBack? = null
    private var deviceEntity: DeviceEntity? = null
    private var editPopupWindow: EditPopupWindow? = null

    override fun getContentView(): Int {
        return  R.layout.activity_device_panel
    }

    override fun initView() {
        iv_back.setColorFilter(R.color.black_333333)
        tv_title.text = ""
        deviceEntity = get("device")
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
                    val ticketResponse = JSON.parse(response.data.toString()) as JSONObject
                    var url = H5_PANEL_BASE_URL +
                            "?deviceId=${deviceEntity?.DeviceId}" +
                            "&familyId=${deviceEntity?.FamilyId}" +
                            "&roomId=${deviceEntity?.RoomId}" +
                            "&familyType=0" +
                            "&lid=${App.data.appLifeCircleId}" +
                            "&quid=${Utils.getAndroidID(this)}" +
                            "&ticket=${ticketResponse[CommonField.TOKEN_TICKET]}" +
                            "&appID=${T.getContext().applicationInfo.packageName}" +
                            "&platform=android" +
                            "&regionId=${App.data.regionId}"
                    if (deviceEntity?.shareDevice!!) {
                        url += "&isShareDevice=true"
                    }
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

    override fun onResume() {
        super.onResume()
        callback?.apply(buildEventByName("pageShow"))
    }

    override fun onPause() {
        super.onPause()
        callback?.apply(buildEventByName("pageHide"))
    }

    override fun onAppGoforeground() {
        callback?.apply(buildEventByName("appShow"))
    }

    override fun onAppGoBackground() {
        callback?.apply(buildEventByName("appHide"))
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
            return true
        }
    }

    private val webViewClient = object: WebViewClient() {
        override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
            L.e("shouldOverrideUrlLoading: " + url)
            when {
                url.contains("goDeviceDetailPage") -> {
                    jumpActivity(DeviceDetailsActivity::class.java)
                }
                url.contains("goFeedBackPage") -> {
                    jumpActivity(FeedbackForH5Activity::class.java)
                }
                url.contains("goDeviceInfoPage") -> {
                    callBackToH5(getCallbackId(url))
                    jumpActivity(DeviceInfoActivity::class.java)
                }
                url.contains("goEditDeviceNamePage") -> {
                    callBackToH5(getCallbackId(url))
                    showEditPopup()
                }
                url.contains("goRoomSettingPage") -> {
                    callBackToH5(getCallbackId(url))
                    jumpActivity(SelectRoomActivity::class.java)
                }
                url.contains("goShareDevicePage") -> {
                    callBackToH5(getCallbackId(url))
                    jumpActivity(ShareUserListActivity::class.java)
                }
                url.contains("navBack") -> {
                    callBackToH5(getCallbackId(url))
                    App.data.setRefreshLevel(2) // 2: 刷新设备列表
                    backToMain()
                }
                url.contains("reloadAfterUnmount") -> {
                    callBackToH5(getCallbackId(url))
                }
                url.contains("setShareConfig") -> {
                    callBackToH5(getCallbackId(url))
                }
                url.contains("goFirmwareUpgradePage") -> {
                    callBackToH5(getCallbackId(url))
                }
            }
            return true
        }
    }

    private fun callBackToH5(id: String) {
        if (callback != null) {
            val jsonObject1 = JSONObject()
            val jsonObject2 = JSONObject()
            jsonObject2["result"] = "true"
            jsonObject2["callbackId"] = id
            jsonObject1[CommonField.HANDLER_NAME] = "callResult"
            jsonObject1["data"] = jsonObject2
            callback?.apply(jsonObject1)
        }
    }

    private fun getCallbackId(url: String): String {
        val uri = Uri.parse(url)
        val param = JSON.parseObject(uri.query)
        return param["callbackId"].toString()
    }

    private fun showEditPopup() {
        if (editPopupWindow == null) {
            editPopupWindow = EditPopupWindow(this)
        }
        deviceEntity?.run {
            editPopupWindow?.setShowData(getString(R.string.device_name), AliasName)
        }
        editPopupWindow?.show(device_panel)
        editPopupWindow?.onVerifyListener = object : EditPopupWindow.OnVerifyListener {
            override fun onVerify(text: String) {
                commitAlias(text)
            }
        }
    }

    private fun commitAlias(aliasName: String) {
        if (TextUtils.isEmpty(aliasName)) return
        deviceEntity?.let {
            HttpRequest.instance.modifyDeviceAliasName(it.ProductId, it.DeviceName, aliasName,
                object : MyCallback {
                    override fun fail(msg: String?, reqCode: Int) {
                        L.e(msg ?: "")
                    }

                    override fun success(response: BaseResponse, reqCode: Int) {
                        if (response.isSuccess()) {
                            deviceEntity?.AliasName = aliasName
                        } else {
                            if (!TextUtils.isEmpty(response.msg))
                                T.show(response.msg)
                        }
                        editPopupWindow?.dismiss()
                    }
                }
            )
        }
    }

    private fun buildEventByName(name: String): JSONObject {
        val jsonObject1 = JSONObject()
        val jsonObject2 = JSONObject()
        jsonObject2["name"] = name
        jsonObject1[CommonField.HANDLER_NAME] = "emitEvent"
        jsonObject1["data"] = jsonObject2
        return jsonObject1
    }
}