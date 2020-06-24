package com.tencent.iot.explorer.link.kitlink.activity

import android.annotation.TargetApi
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.webkit.*
import android.widget.AbsoluteLayout
import android.widget.ProgressBar
import com.tencent.iot.explorer.link.kitlink.R
import com.tencent.iot.explorer.link.kitlink.response.BaseResponse
import com.tencent.iot.explorer.link.kitlink.util.HttpRequest
import com.tencent.iot.explorer.link.kitlink.util.MyCallback
import com.tencent.iot.explorer.link.kitlink.webview.BridgeImpl
import com.tencent.iot.explorer.link.kitlink.webview.JSBridgeKt
import com.tencent.iot.explorer.link.kitlink.webview.WebCallBack
import com.tencent.iot.explorer.link.mvp.IPresenter
import kotlinx.android.synthetic.main.activity_help_feedback.*
import kotlinx.android.synthetic.main.menu_back_layout.*
import org.json.JSONException
import org.json.JSONObject

class TestBrowserActivity: PActivity() , MyCallback{
    val TAG = this.javaClass.simpleName
    val EXTRA_BUNDLE: String = "iOTExtraBundle"
    val BUNDLE_KEY_URL: String = "url"
    private val BAR_SHOW = true    // 显示进度条的开关
    private val LOGIN_REQUEST_CODE = 108
    private var web_status:Int=0 //0：是webview 初始化  1：webview页面交互跳转
    private val BITMAP_DATA = "data"
    private val PRE_FIX = "kitlink"
    private val SUF_FIX = ".jpg"
    private val TIME_FOTMAT = "yyyyMMdd_hhmmss"
    private var mWebView: WebView? = null
    private var progressbar: ProgressBar? = null
    override fun getPresenter(): IPresenter? {
        return null
    }

    private var uploadMessage: ValueCallback<Uri>? = null
    private var uploadMessageAboveL: ValueCallback<Array<Uri>>? = null
    private val FILE_CHOOSER_RESULT_CODE = 10000

    override fun getContentView(): Int {
        return  R.layout.activity_help_feedback

    }

    override fun initView() {
        web_status=0
        initWebView()
        iv_back.setColorFilter(resources.getColor(R.color.black_333333))
        getAppGetTokenTicket()
    }


    //构建 webView 的数据内容
    private fun initWebView() {
        mWebView = findViewById(R.id.help_web) as WebView
        val drawable = resources.getDrawable(R.drawable.progress_bar_states)
        progressbar = ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal)
        progressbar!!.layoutParams = AbsoluteLayout.LayoutParams(
            AbsoluteLayout.LayoutParams.MATCH_PARENT,
            5,
            0,
            0
        )
        progressbar!!.progressDrawable = drawable
        mWebView!!.addView(progressbar)
        val settings = mWebView!!.settings
        // 设置js支持
        settings.javaScriptEnabled = true
        // 开启 DOM storage API 功能
        settings.domStorageEnabled = true
        // 设置网页字体不跟随系统字体发生改变
        settings.textZoom = 100
        // 缩放至屏幕的大小
        settings.useWideViewPort = true
        settings.loadWithOverviewMode = true
        // 是否支持缩放
        settings.setSupportZoom(true)
        settings.builtInZoomControls = true
        settings.displayZoomControls = false
        // 不缓存
        settings.cacheMode = WebSettings.LOAD_NO_CACHE
//        //设置https支持http
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//            settings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
//        }
//        mWebView!!.webViewClient = object : WebViewClient() {
//            override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
//                if (url.contains("iot-explorer-help-center://")) {
//                    //读取到url后通过callJava分析调用
//                    JSBridgeKt.callNative(view,url)
//                } else {
//                    // 当有新连接时，使用当前的 WebView
//                    view.loadUrl(url)
//                }
//                return true
//            }
//        }
//        mWebView!!.setWebChromeClient(object : WebChromeClient() {
//            override fun onJsPrompt(
//                view: WebView,
//                url: String,
//                message: String,
//                defaultValue: String,
//                result: JsPromptResult
//            ): Boolean {
//                result.confirm(JSBridgeKt.callNative(view, message))
//                return true
//            }
//
//            override fun onProgressChanged(view: WebView, newProgress: Int) {
//                if (newProgress == 100) {
//                    progressbar!!.visibility = View.GONE
//                } else {
//                    if (progressbar!!.visibility == View.GONE) {
//                        progressbar!!.visibility = View.VISIBLE
//                    }
//                    progressbar!!.progress = newProgress
//                }
//                super.onProgressChanged(view, newProgress)
//            }
//        })
//        JSBridgeKt.register(
//            "help_center_bridge",
//            BridgeImpl::class.java
//        )
//        mWebView!!.loadUrl("file:///android_asset/index.html")
    }


    override fun setListener() {
        btn1.setOnClickListener(View.OnClickListener {
            val data = JSONObject()
            try {
                data.put("fromNative", "不回调")
                val callback =
                    WebCallBack(mWebView)
                val jsObject = JSONObject()
                jsObject.put("handlerName", "testH5Func")
                jsObject.put("data", data)
                callback.apply(jsObject)
            } catch (e: JSONException) {
                e.printStackTrace()
            }
        })
        btn2.setOnClickListener(View.OnClickListener {
            val data = JSONObject()
            try {
                data.put("fromNative", "回调")
                val callback =
                    WebCallBack(mWebView)
                val jsObject = JSONObject()
                jsObject.put("handlerName", "testH5Func")
                jsObject.put("data", data)
                jsObject.put("callbackId", getCallbackId())
                callback.apply(jsObject)
            } catch (e: JSONException) {
                e.printStackTrace()
            }
        })
        btn0.setOnClickListener(View.OnClickListener {
           finish()
        })
        iv_back.setOnClickListener {

            if (help_web.canGoBack()) {
                help_web.goBack() //返回上一页面
            } else { // 关闭当前窗口
                this@TestBrowserActivity.finish()
            }
        }
    }

    private fun getCallbackId(): Double {
        return Math.floor(Math.random() * (1 shl 30))
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode === LOGIN_REQUEST_CODE && resultCode === 10) {
                web_status=1
            getAppGetTokenTicket()
        }else if(requestCode == FILE_CHOOSER_RESULT_CODE){
            if (null == uploadMessage && null == uploadMessageAboveL) return;
            val result =
                if (data == null || resultCode != -1) null else data.data
            if (uploadMessageAboveL != null) {
                onActivityResultAboveL(requestCode, resultCode, data);
            } else if (uploadMessage != null) {
                uploadMessage!!.onReceiveValue(result);
                uploadMessage = null;
            }
        }

    }

    /**
     * 获取一次性的TokenTicket
     */
    private fun getAppGetTokenTicket() {
        HttpRequest.instance.getShareTicket(this)


    }

    override fun fail(msg: String?, reqCode: Int) {

    }

    override fun success(response: BaseResponse, reqCode: Int) {
        if(web_status==0){
            mWebView!!.webViewClient = object : WebViewClient() {
                override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                    if (url.contains("iot-explorer-help-center://")) {
                        //读取到url后通过callJava分析调用
                        JSBridgeKt.callNative(view,url)
                    } else {
                        // 当有新连接时，使用当前的 WebView
                        view.loadUrl(url)
                    }
                    return true
                }
            }
            mWebView!!.setWebChromeClient(object : WebChromeClient() {

                override fun onShowFileChooser(
                    webView: WebView?,
                    filePathCallback: ValueCallback<Array<Uri>>?,
                    fileChooserParams: FileChooserParams?
                ): Boolean {
                    uploadMessageAboveL = filePathCallback
                    openImageChooserActivity()
                    return true
                }
                override fun onReceivedTitle(view: WebView?, title: String?) {
                    super.onReceivedTitle(view, title)
                    tv_title.text =title
                }
                override fun onJsPrompt(
                    view: WebView,
                    url: String,
                    message: String,
                    defaultValue: String,
                    result: JsPromptResult
                ): Boolean {
                    result.confirm(JSBridgeKt.callNative(view, message))
                    return true
                }

            })
            JSBridgeKt.register(
                "help_center_bridge",
                BridgeImpl::class.java
            )
            var js:JSONObject= JSONObject(response.data.toString())
            var url:String="https://iot.cloud.tencent.com/explorer-h5/help-center/?uin=help_center_h5&api_uin=help_center_h5_api&ticket="+js["TokenTicket"]

            mWebView!!.loadUrl(url)
        }else{
            val data = JSONObject()
            val jsObject = JSONObject()
            jsObject.put("handlerName", "LoginResult")
            val callback =
                WebCallBack(mWebView)

            try {
                var js:JSONObject= JSONObject(response.data.toString())
                data.put("ticket", js["TokenTicket"])
                jsObject.put("data", data)

            } catch (e: JSONException) {
                e.printStackTrace()
            }
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
                this@TestBrowserActivity.finish()
            }
        }
        return super.onKeyDown(keyCode, event)
    }



    private fun openImageChooserActivity() {
        val i = Intent(Intent.ACTION_GET_CONTENT)
        i.addCategory(Intent.CATEGORY_OPENABLE)
        i.type = "image/*"
        startActivityForResult(
            Intent.createChooser(i, "Image Chooser"),
            FILE_CHOOSER_RESULT_CODE
        )
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private fun onActivityResultAboveL(requestCode: Int, resultCode: Int, intent: Intent?) {
        if (requestCode != FILE_CHOOSER_RESULT_CODE || uploadMessageAboveL == null)
            return
        var results: Array<Uri>? = null
        if (resultCode == Activity.RESULT_OK) {
            if (intent != null) {
                val dataString = intent.dataString
                val clipData = intent.clipData
                if (clipData != null) {
                    results = Array(clipData.itemCount){
                            i -> clipData.getItemAt(i).uri
                    }
                }
                if (dataString != null)
                    results = arrayOf(Uri.parse(dataString))
            }
        }
        uploadMessageAboveL!!.onReceiveValue(results)
        uploadMessageAboveL = null
    }


}

