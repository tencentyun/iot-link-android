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
import com.tencent.iot.explorer.link.T
import com.tencent.iot.explorer.link.core.auth.callback.MyCallback
import com.tencent.iot.explorer.link.core.auth.response.BaseResponse
import com.tencent.iot.explorer.link.core.log.L
import com.tencent.iot.explorer.link.core.utils.FileUtils
import com.tencent.iot.explorer.link.core.utils.PhotoUtils
import com.tencent.iot.explorer.link.core.utils.Utils
import com.tencent.iot.explorer.link.customview.dialog.PermissionDialog
import com.tencent.iot.explorer.link.kitlink.consts.CommonField
import com.tencent.iot.explorer.link.kitlink.util.HttpRequest
import com.tencent.iot.explorer.link.kitlink.util.safe
import com.tencent.iot.explorer.link.kitlink.webview.BridgeImpl
import com.tencent.iot.explorer.link.kitlink.webview.JSBridgeKt
import com.tencent.iot.explorer.link.kitlink.webview.WebCallBack
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
    private val FILE_CHOOSER_CAMERA_RESULT_CODE = 9998

    private var configQuestionList = false
    private var feedbackDevice = false
    private var feedbackCategory = ""
    private var permissionDialog: PermissionDialog? = null

    override fun getContentView(): Int {
        return  R.layout.activity_help_feedback
    }

    override fun initView() {
        webStatus = 0
        if (intent.hasExtra(CommonField.CONFIG_QUESTION_LIST)) {
            configQuestionList = intent.getBooleanExtra(CommonField.CONFIG_QUESTION_LIST, false)
        }
        if (intent.hasExtra(CommonField.FEEDBACK_DEVICE)) {
            feedbackDevice = intent.getBooleanExtra(CommonField.FEEDBACK_DEVICE, false)
        }
        if (intent.hasExtra(CommonField.FEEDBACK_CATEGORY)) {
            feedbackCategory = intent.getStringExtra(CommonField.FEEDBACK_CATEGORY).safe()
        }
        initWebView()
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            help_web.settings.mixedContentMode = WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE
        }
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
//            if (checkCameraPermission(false)) {
                //拍照并确定
                val bitmap = BitmapFactory.decodeFile(PhotoUtils.PATH_PHOTO)
                val file = FileUtils.compressImage(this, bitmap)
                uploadMessageAboveL?.onReceiveValue(arrayOf(Uri.fromFile(file)))
//            } else {
//                // 查看请求album权限的时间是否大于48小时
//                if (requestPermissionIsIn48Hours(CommonField.PERMISSION_ALBUM)) {
//                    T.show(resources.getString(R.string.permission_of_album_refuse))
//                }
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//                    requestPermissions(permissions, FILE_CHOOSER_RESULT_CODE)
//                }
//                permissionDialog = PermissionDialog(this@HelpWebViewActivity, R.mipmap.permission_album ,getString(R.string.permission_album_lips), getString(R.string.permission_storage_help_center))
//                permissionDialog!!.show()
//
//                // 记录请求album权限的时间
//                savePermission(CommonField.PERMISSION_ALBUM)
//            }


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
                url += "&uin=${App.uuid}"
                if (!App.isOEMApp()) {
                    url += "&appID=" + T.getContext().applicationInfo.packageName
                }
                url += "&lang=${Utils.getLang()}"
                if (configQuestionList) {
                    url += "/#/pages/Functional/HelpCenter/QnAList/QnAList?genCateID=config7"
                }
                if (feedbackDevice) {
                    url += "#/pages/User/Feedback/Feedback?cate=" + feedbackCategory
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
    private fun checkCameraPermission(isCamera: Boolean): Boolean {

        if (isCamera) {//相机
            // android M(6.0) 以上检查存储权限以及相机权限
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                (ContextCompat.checkSelfPermission(this@HelpWebViewActivity,
                    Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)) {
                return false
            }
            return true
        } else {//相册
            // android M(6.0) 以上检查存储权限
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && ContextCompat.checkSelfPermission(this@HelpWebViewActivity, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                return false
            }
            return true
        }
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

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        permissionDialog?.dismiss()
        permissionDialog = null
        if (requestCode == FILE_CHOOSER_RESULT_CODE) {
            if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //拍照并确定
                val bitmap = BitmapFactory.decodeFile(PhotoUtils.PATH_PHOTO)
                val file = FileUtils.compressImage(this, bitmap)
                uploadMessageAboveL?.onReceiveValue(arrayOf(Uri.fromFile(file)))
            }
        }
        for (i in permissions.indices) {
            if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                when (requestCode) {
                    FILE_CHOOSER_RESULT_CODE -> {
                        T.show(resources.getString(R.string.permission_of_album_refuse))
                    }
                    FILE_CAMERA_RESULT_CODE -> {
                        T.show(resources.getString(R.string.permission_of_camera_refuse))
                    }
                    FILE_CHOOSER_CAMERA_RESULT_CODE -> {
                        T.show(resources.getString(R.string.permission_of_camera_refuse))
                    }
                }
                return
            }
        }
    }

    //查看请求 permissionName 权限的时间是否大于48小时
    private fun requestPermissionIsIn48Hours(permissionName: String) :Boolean {
        // 查看请求camera权限的时间是否大于48小时
        var cameraJsonString = Utils.getStringValueFromXml(T.getContext(), permissionName, permissionName)
        var cameraJson: JSONObject? = JSONObject.parse(cameraJsonString) as JSONObject?
        val lasttime = cameraJson?.getLong(permissionName)
        return (lasttime != null && lasttime > 0 && System.currentTimeMillis() / 1000 - lasttime < 48*60*60)
    }
    //记录请求 permissionName 权限的时间
    private fun savePermission(permissionName: String) {
        //
        var json = JSONObject()
        json.put(permissionName, System.currentTimeMillis() / 1000)
        Utils.setXmlStringValue(T.getContext(), permissionName, permissionName, json.toJSONString())
    }

    val webChromeClient = object: WebChromeClient() {

        override fun onShowFileChooser (webView: WebView, filePathCallback: ValueCallback<Array<Uri>>, fileChooserParams: FileChooserParams?): Boolean {
            uploadMessageAboveL = filePathCallback
            if (fileChooserParams == null)
                return false

            if (fileChooserParams.isCaptureEnabled) {
                isCapture = true;
                if (checkCameraPermission(true)) {
                    PhotoUtils.startCamera(this@HelpWebViewActivity)
                } else {

                    // 检查相机权限
                    if (ContextCompat.checkSelfPermission(this@HelpWebViewActivity, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                        // 查看请求camera权限的时间是否大于48小时
                        if (requestPermissionIsIn48Hours(CommonField.PERMISSION_CAMERA)) {
                            T.show(resources.getString(R.string.permission_of_camera_refuse))
                            return false
                        }
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            requestPermissions(arrayOf(Manifest.permission.CAMERA), FILE_CAMERA_RESULT_CODE)
                        }
                        permissionDialog = PermissionDialog(this@HelpWebViewActivity, R.mipmap.permission_camera ,getString(R.string.permission_camera_lips), getString(R.string.permission_camera_help_center))
                        permissionDialog!!.show()

                        // 记录请求camera权限的时间
                        savePermission(CommonField.PERMISSION_CAMERA)
                    }
                    return false
                }

            } else {
                isCapture = false;
                PhotoUtils.startAlbum(this@HelpWebViewActivity)
//                if (checkCameraPermission(false)) {
//                    PhotoUtils.startAlbum(this@HelpWebViewActivity)
//                } else {
//                    // 查看请求album权限的时间是否大于48小时
//                    if (requestPermissionIsIn48Hours(CommonField.PERMISSION_ALBUM)) {
//                        T.show(resources.getString(R.string.permission_of_album_refuse))
//                        return false
//                    }
//                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//                        requestPermissions(permissions, FILE_CHOOSER_RESULT_CODE)
//                    }
//                    permissionDialog = PermissionDialog(this@HelpWebViewActivity, R.mipmap.permission_album ,getString(R.string.permission_album_lips), getString(R.string.permission_storage_help_center))
//                    permissionDialog!!.show()
//
//                    // 记录请求album权限的时间
//                    savePermission(CommonField.PERMISSION_ALBUM)
//                    return false
//                }
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

