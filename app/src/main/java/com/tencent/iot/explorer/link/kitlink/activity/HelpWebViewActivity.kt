package com.tencent.iot.explorer.link.kitlink.activity

import android.Manifest
import android.annotation.TargetApi
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import android.webkit.*
import android.widget.LinearLayout
import android.widget.ProgressBar
import androidx.core.content.ContextCompat
import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.JSONObject
import com.tencent.iot.explorer.link.R
import com.tencent.iot.explorer.link.kitlink.response.BaseResponse
import com.tencent.iot.explorer.link.kitlink.util.FileUtils
import com.tencent.iot.explorer.link.kitlink.util.HttpRequest
import com.tencent.iot.explorer.link.kitlink.util.MyCallback
import com.tencent.iot.explorer.link.kitlink.util.PhotoUtils
import com.tencent.iot.explorer.link.kitlink.webview.BridgeImpl
import com.tencent.iot.explorer.link.kitlink.webview.JSBridgeKt
import com.tencent.iot.explorer.link.kitlink.webview.WebCallBack
import com.tencent.iot.explorer.link.util.L
import kotlinx.android.synthetic.main.activity_help_feedback.*
import kotlinx.android.synthetic.main.menu_back_layout.*
import java.io.File

class HelpWebViewActivity: BaseActivity(), MyCallback, View.OnClickListener {
    val TAG = this.javaClass.simpleName

    private val LOGIN_REQUEST_CODE = 108
    private var progressbar: ProgressBar? = null
    @Volatile
    private var webStatus: Int = 0 //0：是webview 初始化  1：webview页面交互跳转
    @Volatile
    private var isCapture: Boolean = false

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
        iv_back.setColorFilter(resources.getColor(R.color.black_333333))
        getAppGetTokenTicket()
    }

    //构建 webView 的数据内容
    private fun initWebView() {
//        val drawable = resources.getDrawable(R.drawable.progress_bar_states, null)
//        progressbar = ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal)
//        var params = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 5)
//        progressbar!!.layoutParams = LinearLayout.LayoutParams(params)
//        progressbar!!.progressDrawable = drawable
//        help_web!!.addView(progressbar)

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
        btn1.setOnClickListener(this)
        btn2.setOnClickListener(this)
        btn0.setOnClickListener(this)
        iv_back.setOnClickListener(this)
    }

    private fun getCallbackId(): Double {
        return Math.floor(Math.random() * (1 shl 30))
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.d(TAG,"onActivityResult")
        if (requestCode === LOGIN_REQUEST_CODE && resultCode === 10) {
            webStatus = 1
            getAppGetTokenTicket()

        } else if (requestCode == PhotoUtils.RESULT_CODE_CAMERA && resultCode == Activity.RESULT_OK) {
            //拍照并确定
            var bitmap = BitmapFactory.decodeFile(PhotoUtils.PATH_PHOTO)
            var file: File? = FileUtils.compressImage(bitmap)
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
        Log.e("XXX", "fail")
    }

    override fun success(response: BaseResponse, reqCode: Int) {
        Log.e("XXX", "success resp=" + JSON.toJSONString(response))
        Log.e("XXX", "reqCode resp=" + reqCode)
        if (webStatus == 0) {
            JSBridgeKt.register("help_center_bridge", BridgeImpl::class.java)

            help_web.webViewClient = object: WebViewClient() {
                override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {

                    Log.e("XXX", "shouldOverrideUrlLoading")
                    if (url.contains("iot-explorer-help-center://")) {
                        // 读取到 url 后通过 callJava 分析调用
                        Log.e("XXX", "shouldOverrideUrlLoading 1")
                        JSBridgeKt.callNative(view, url)
                    } else {
                        // 当有新连接时，使用当前的 WebView
                        Log.e("XXX", "shouldOverrideUrlLoading 2")
                        view.loadUrl(url)
                    }
                    return true
                }
            }

            help_web.setWebChromeClient(webChromeClient)

            if (response.code == 0) {
                var js = JSON.parse(response.data.toString()) as JSONObject
                var url: String="https://iot.cloud.tencent.com/explorer-h5/help-center/?uin=help_center_h5&api_uin=help_center_h5_api&ticket=" + js["TokenTicket"]
                Log.e("XXX", "url=" + url)
                help_web!!.loadUrl(url)
            }

        } else {
            val data = JSONObject()
            val jsObject = JSONObject()
            jsObject.put("handlerName", "LoginResult")
            val callback = WebCallBack(help_web)

            var js = JSON.parse(response.data.toString()) as JSONObject
            data.put("ticket", js["TokenTicket"])
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

    private fun openImageChooserActivity() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.type = "image/*"
        startActivityForResult(Intent.createChooser(intent, "Image Chooser"),
            PhotoUtils.RESULT_CODE_PHOTO)
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
                        Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
        ) {
            L.e(TAG, "open camera need permission")
            requestPermissions(arrayOf(Manifest.permission.CAMERA,
                Manifest.permission.WRITE_SETTINGS,
                Manifest.permission.WRITE_EXTERNAL_STORAGE),
                FILE_CHOOSER_RESULT_CODE)
            return false
        }
        L.e(TAG, "camera permission is ready")
        return true
    }

    override fun onClick(v: View?) {
        when(v) {
            btn1 -> {
                val data = JSONObject()
                data.put("fromNative", "不回调")
                val callback = WebCallBack(help_web)
                val jsObject = JSONObject()
                jsObject.put("handlerName", "testH5Func")
                jsObject.put("data", data)
                callback.apply(jsObject)
            }

            btn2 -> {
                val data = JSONObject()
                data.put("fromNative", "回调")
                val callback = WebCallBack(help_web)
                val jsObject = JSONObject()
                jsObject.put("handlerName", "testH5Func")
                jsObject.put("data", data)
                jsObject.put("callbackId", getCallbackId())
                callback.apply(jsObject)
            }

            btn0 -> this@HelpWebViewActivity.finish()

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
                return true

            if (fileChooserParams.isCaptureEnabled) {
                isCapture = true;
                if (checkCameraPermission()) {
                    PhotoUtils.startCamera(this@HelpWebViewActivity)
                }

            } else {
                isCapture = false;
                if (checkCameraPermission()) {
                    PhotoUtils.startAlbum(this@HelpWebViewActivity)
                }
            }
            return true;
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

    override fun onDestroy() {

        Log.e("XXX", "onDestroy")
        super.onDestroy()
        help_web.clearHistory()
        help_web.clearCache(true)
        help_web.pauseTimers()
        help_web.loadUrl("about:blank")
    }
}

