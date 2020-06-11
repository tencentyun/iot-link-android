package com.tencent.iot.explorer.link.kitlink.activity

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Build
import android.provider.MediaStore
import android.text.TextUtils
import android.text.format.DateFormat
import android.view.KeyEvent
import android.view.View
import android.webkit.*
import androidx.core.content.ContextCompat
import com.tencent.iot.explorer.link.kitlink.R
import com.tencent.iot.explorer.link.kitlink.util.JsBridge
import com.util.L
import kotlinx.android.synthetic.main.activity_help_feedback.*
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.util.*

/**
 * 帮助反馈
 */
class HelpFeedbackActivity : BaseActivity() {
    val TAG = this.javaClass.simpleName

    val EXTRA_BUNDLE: String = "iOTExtraBundle"
    val BUNDLE_KEY_URL: String = "url"
    private val BAR_SHOW = true    // 显示进度条的开关
    private val CAMERA_REQUEST = 0
    private val BITMAP_DATA = "data"
    private val PRE_FIX = "kitlink"
    private val SUF_FIX = ".jpg"
    private val TIME_FOTMAT = "yyyyMMdd_hhmmss"
    private var jsBridge = JsBridge(this@HelpFeedbackActivity)

    // 从传入的 bundle 中获取需要加载的 url
    private fun getUrlFromBundle(): String {
        var intent = this@HelpFeedbackActivity.intent
        var bundle = intent?.getBundleExtra(EXTRA_BUNDLE)
        var url = bundle?.getString(BUNDLE_KEY_URL)

        url = "file:////android_asset/test.html"

        if (TextUtils.isEmpty(url)) {
            L.e(TAG, "no url to show")
        }
        return url!!
    }

    override fun getContentView(): Int {
        return R.layout.activity_help_feedback
    }

    // 打开相机为异步过程，异步过程会生成临时的图片文件，
    // 图片使用完成以后需要调用 deleteTmpImageFile，删除临时图片文件
    // true: 打开相机成功   false: 权限校验失败，正在申请打开相机需要的相关权限
    fun asynOpenCamera(): Boolean {
        if (!checkCameraPermission()) {
            return false;
        }

        val camera = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        this@HelpFeedbackActivity.startActivityForResult(camera, CAMERA_REQUEST)
        if (jsBridge != null) {
            jsBridge!!.onCameraOpened()
        }
        return true
    }

    fun deleteTempImageFile(path: String) {
        var file2Del = File(path)
        file2Del.deleteOnExit()
    }

    override fun initView() {
        initWebView(getUrlFromBundle(), jsBridge)
        if (BAR_SHOW) {
            initProgressBar()
        }
    }

    //构建 webView 的数据内容
    private fun initWebView(url: String, jsBridge: JsBridge) {
        if (TextUtils.isEmpty(url)) return

        help_web.loadUrl(url)
        help_web.setWebViewClient(object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView, url: String?): Boolean {
                //返回值是 true 的时候控制去 WebView 打开，为 false 调用系统浏览器或第三方浏览器
                view.loadUrl(url)
                return true
            }
        })

        val settings: WebSettings = help_web.getSettings()
        settings.javaScriptEnabled = true // 支持 js 交互
        if (jsBridge != null) {
            help_web.addJavascriptInterface(jsBridge, JsBridge.BRIDGE)
        }
    }

    // 页面加载进度条
    private fun initProgressBar() {
        help_web.visibility = View.VISIBLE
        help_web.setWebChromeClient(object : WebChromeClient() {
            override fun onProgressChanged(view: WebView, progress: Int) {
                if (progress >= 100) {
                    progressBar_webView.setVisibility(View.GONE)
                } else {
                    progressBar_webView.setVisibility(View.VISIBLE)
                    progressBar_webView.setProgress(progress)
                }
            }
        })
    }

    override fun setListener() {

    }

    // 拦截当前窗口的返回按钮的点击事件，保证网页之间的跳转
    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (help_web.canGoBack()) {
                help_web.goBack() //返回上一页面
                return true
            } else { // 关闭当前窗口
                this@HelpFeedbackActivity.finish()
            }
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (jsBridge != null) {
            jsBridge.close()
        }
    }

    // 拍照成功的回调，主线程回调
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == CAMERA_REQUEST &&
            resultCode == Activity.RESULT_OK && data != null
        ) {
            if (jsBridge != null) {
                jsBridge!!.onCameraClosed()
            }
            //获取相机返回的数据，并转换为图片格式
            val bundle = data.extras!!
            var bitmap = bundle[BITMAP_DATA] as Bitmap

            // 构建临时文件
            val fileName = PRE_FIX + DateFormat.format(TIME_FOTMAT,
                Calendar.getInstance(Locale.CHINA)).toString() + SUF_FIX
            val filePath = getFilesDir().getAbsolutePath() + fileName

            // 依赖 try () catch {} 的 autoClose 接口自动关闭文件流
            try {
                FileOutputStream(filePath).use { fout ->
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fout) }
            } catch (e: FileNotFoundException) {
                e.printStackTrace()
            }
            if (jsBridge != null) {
                jsBridge!!.onCaptureSuccess(help_web, filePath)
            }
        }
    }

    // 检查相机权限是否开启
    private fun checkCameraPermission(): Boolean {

        // android M(6.0) 以上检查存储权限以及相机权限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
            (ContextCompat.checkSelfPermission(this@HelpFeedbackActivity,
                Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(this@HelpFeedbackActivity,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
        ) {
            L.e(TAG, "open camera need permission")
            requestPermissions(
                arrayOf(Manifest.permission.CAMERA,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE),
                CAMERA_REQUEST)
            return false
        }
        L.e(TAG, "camera permission is ready")
        return true
    }
}
