package com.tencent.iot.explorer.link.kitlink.activity

import android.Manifest
import android.annotation.TargetApi
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import android.webkit.*
import android.widget.LinearLayout
import android.widget.ProgressBar
import androidx.core.content.ContextCompat
import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.JSONObject
import com.tencent.iot.explorer.link.App
import com.tencent.iot.explorer.link.R
import com.tencent.iot.explorer.link.core.log.L
import com.tencent.iot.explorer.link.kitlink.consts.CommonField
import com.tencent.iot.explorer.link.kitlink.response.BaseResponse
import com.tencent.iot.explorer.link.kitlink.util.FileUtils
import com.tencent.iot.explorer.link.kitlink.util.HttpRequest
import com.tencent.iot.explorer.link.kitlink.util.MyCallback
import com.tencent.iot.explorer.link.kitlink.util.PhotoUtils
import com.tencent.iot.explorer.link.kitlink.webview.BridgeImpl
import com.tencent.iot.explorer.link.kitlink.webview.JSBridgeKt
import com.tencent.iot.explorer.link.kitlink.webview.WebCallBack
import com.tencent.iot.explorer.link.util.T
import kotlinx.android.synthetic.main.activity_help_feedback.*
import kotlinx.android.synthetic.main.menu_back_layout.*

class HelpWebViewActivity: BaseActivity(), MyCallback, View.OnClickListener {
    val TAG = this.javaClass.simpleName

    private var progressbar: ProgressBar? = null
    @Volatile
    private var webStatus = 0 //0：是webview 初始化  1：webview页面交互跳转
    @Volatile
    private var isCapture = false

    private var uploadMessage: ValueCallback<Uri>? = null
    private var uploadMessageAboveL: ValueCallback<Array<Uri>>? = null
    private val FILE_CHOOSER_RESULT_CODE = 10000
    private val FILE_CAMERA_RESULT_CODE = 9999

    override fun getContentView(): Int {
        return  R.layout.activity_help_feedback
    }

    override fun initView() {
        webStatus = 0
        initWebView()
        iv_back.setColorFilter(R.color.black_333333)
        getAppGetTokenTicket()
    }

    //构建 webView 的数据内容
    private fun initWebView() {
        val drawable = resources.getDrawable(R.drawable.progress_bar_states, null)
        progressbar = ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal)
        var params = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 5)
        progressbar!!.layoutParams = LinearLayout.LayoutParams(params)
        progressbar!!.progressDrawable = drawable
        help_web!!.addView(progressbar)

        help_web.settings.javaScriptEnabled = true  // 设置js支持
        help_web.settings.domStorageEnabled = true  // 开启 DOM storage API 功能
        help_web.settings.textZoom = 100            // 设置网页字体不跟随系统字体发生改变
        help_web.settings.useWideViewPort = true    // 缩放至屏幕的大小
        help_web.settings.loadWithOverviewMode = true
        // 是否支持缩放
        help_web.settings.setSupportZoom(true)
        help_web.settings.builtInZoomControls = true
        help_web.settings.displayZoomControls = false
        help_web.settings.cacheMode = WebSettings.LOAD_NO_CACHE     // 不缓存
    }

    override fun setListener() {
        iv_back.setOnClickListener(this)
    }

    private fun getCallbackId(): Double {
        return Math.floor(Math.random() * (1 shl 30))
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CommonField.LOGIN_REQUEST_CODE && resultCode == CommonField.H5_REQUEST_LOGIN_CODE) {
            webStatus = 1
            getAppGetTokenTicket()

        } else if (requestCode == PhotoUtils.RESULT_CODE_CAMERA && resultCode == Activity.RESULT_OK) {
            //拍照并确定
            var bitmap = BitmapFactory.decodeFile(PhotoUtils.PATH_PHOTO)
            var file = FileUtils.compressImage(bitmap)
            uploadMessageAboveL?.onReceiveValue(arrayOf(Uri.fromFile(file)))

        } else if (requestCode == PhotoUtils.RESULT_CODE_PHOTO && resultCode == Activity.RESULT_OK) {
            onActivityResultAboveL(requestCode, resultCode, data);

        } else if(requestCode == FILE_CHOOSER_RESULT_CODE && resultCode == Activity.RESULT_OK){
            if (isCapture) {
                PhotoUtils.startCamera(this@HelpWebViewActivity)
            }

        } else {
            uploadMessageAboveL?.onReceiveValue(null)
        }
    }

    /**
     * 获取一次性的 TokenTicket
     */
    private fun getAppGetTokenTicket() {
        HttpRequest.instance.getOneTimeTokenTicket(this)
    }

    override fun fail(msg: String?, reqCode: Int) {
    }

    override fun success(response: BaseResponse, reqCode: Int) {
        if (webStatus == 0) {
            JSBridgeKt.register("help_center_bridge", BridgeImpl::class.java)

            help_web.webViewClient = webViewClient
            help_web.webChromeClient = webChromeClient

            if (response.code == 0) {
                var js = JSON.parse(response.data.toString()) as JSONObject
                var url ="https://iot.cloud.tencent.com/explorer-h5/help-center/?" +
                        "&ticket=" + js[CommonField.TOKEN_TICKET]
                if (App.DEBUG_VERSION) {
                    url += "&uin=help_center_h5&api_uin=help_center_h5_api"
                }
                if (!App.isOEMApp()) {
                    url += "&appID=" + T.getContext().applicationInfo.packageName
                }

                help_web.loadUrl(url)
            }

        } else {
            val data = JSONObject()
            val jsObject = JSONObject()
            jsObject.put(CommonField.HANDLER_NAME, "LoginResult")
            val callback = WebCallBack(help_web)

            var js = JSON.parse(response.data.toString()) as JSONObject
            data.put(CommonField.TICKET, js[CommonField.TOKEN_TICKET])
            jsObject.put("data", data)
            callback.apply(jsObject)
        }
    }

    // 拦截当前窗口的返回按钮的点击事件，保证网页之间的跳转
    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (help_web.canGoBack()) {
                help_web.goBack() //返回上一页面
                return true
            } else { // 关闭当前窗口
                this@HelpWebViewActivity.finish()
            }
        }
        return super.onKeyDown(keyCode, event)
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private fun onActivityResultAboveL(requestCode: Int, resultCode: Int, intent: Intent?) {
        if (requestCode != PhotoUtils.RESULT_CODE_PHOTO || uploadMessageAboveL == null)
            return

        var results: Array<Uri>? = null
        if (resultCode == Activity.RESULT_OK && intent != null) {
            val dataString = intent.dataString
            val clipData = intent.clipData

            L.e(TAG, "camera is op")
            if (clipData != null) {
                results = Array(clipData.itemCount) {
                        i -> clipData.getItemAt(i).uri
                }
            }

            if (dataString != null)
                results = arrayOf(Uri.parse(dataString))
        }
        uploadMessageAboveL!!.onReceiveValue(results)
        uploadMessageAboveL = null
    }

    // 检查相机权限是否开启
    private fun checkCameraPermission(): Boolean {

        // android M(6.0) 以上检查存储权限以及相机权限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
            (ContextCompat.checkSelfPermission(this@HelpWebViewActivity,
                Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this@HelpWebViewActivity,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)) {

            requestPermissions(arrayOf(Manifest.permission.CAMERA,
                Manifest.permission.WRITE_SETTINGS,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE),
                FILE_CHOOSER_RESULT_CODE)
            return false
        }
        return true
    }

    override fun onClick(v: View?) {
        when(v) {
            iv_back -> {
                if (help_web.canGoBack()) {
                    help_web.goBack() //返回上一页面
                } else { // 关闭当前窗口
                    this@HelpWebViewActivity.finish()
                }
            }
        }
    }

    val webChromeClient = object: WebChromeClient() {

        override fun onShowFileChooser (webView: WebView, filePathCallback: ValueCallback<Array<Uri>>, fileChooserParams: FileChooserParams?): Boolean {
            uploadMessageAboveL = filePathCallback
            if (fileChooserParams == null)
                return false

            if (fileChooserParams.isCaptureEnabled) {
                isCapture = true;
                if (checkCameraPermission()) {
                    PhotoUtils.startCamera(this@HelpWebViewActivity)
                } else {
                    return false
                }

            } else {
                isCapture = false;
                if (checkCameraPermission()) {
                    PhotoUtils.startAlbum(this@HelpWebViewActivity)
                } else {
                    return false
                }
            }
            return true
        }

        override fun onReceivedTitle(view: WebView?, title: String?) {
            super.onReceivedTitle(view, title)
            tv_title.text = title
        }

        override fun onJsPrompt(view: WebView, url: String, message: String, defaultValue: String, result: JsPromptResult): Boolean {
            result.confirm(JSBridgeKt.callNative(view, message))
            return true
        }
    }

    var webViewClient = object: WebViewClient() {
        override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {

            if (url.contains("iot-explorer-help-center://")) {  // 读取到 url 后通过 callJava 分析调用
                JSBridgeKt.callNative(view, url)

            } else {    // 当有新连接时，使用当前的 WebView
                view.loadUrl(url)
            }
            return true
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        help_web.clearHistory()
        help_web.clearCache(true)
        help_web.loadUrl("about:blank")
    }
}

