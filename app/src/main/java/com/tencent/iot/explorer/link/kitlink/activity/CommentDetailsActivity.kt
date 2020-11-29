package com.tencent.iot.explorer.link.kitlink.activity

import android.net.Uri
import android.os.Build
import android.text.TextUtils
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import android.webkit.*
import android.widget.LinearLayout
import android.widget.ProgressBar
import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.JSONObject
import com.tencent.iot.explorer.link.R
import com.tencent.iot.explorer.link.T
import com.tencent.iot.explorer.link.core.auth.response.BaseResponse
import com.tencent.iot.explorer.link.core.utils.Utils
import com.tencent.iot.explorer.link.customview.dialog.ShareOptionDialog
import com.tencent.iot.explorer.link.kitlink.consts.CommonField
import com.tencent.iot.explorer.link.kitlink.util.HttpRequest
import com.tencent.iot.explorer.link.core.auth.callback.MyCallback
import com.tencent.iot.explorer.link.kitlink.util.WeChatLogin
import com.tencent.iot.explorer.link.kitlink.webview.JSBridgeKt
import kotlinx.android.synthetic.main.activity_comment_detail.*
import kotlinx.android.synthetic.main.menu_back_layout.*


class CommentDetailsActivity: BaseActivity(), View.OnClickListener, MyCallback {
    val TAG = this.javaClass.simpleName

    private var progressbar: ProgressBar? = null
    private var dialog: ShareOptionDialog? = null
    @Volatile
    private var pathUrl: String? = null
    @Volatile
    private var wechatPath = ""
    @Volatile
    private var wechatShareUrl = ""
    @Volatile
    private var webShareUrl = ""
    @Volatile
    private var shareImg = ""

    override fun getContentView(): Int {
        return  R.layout.activity_comment_detail
    }

    override fun initView() {
        initWebView()
        iv_back.setColorFilter(R.color.black_333333)
        tv_title.setText("")
    }

    //构建 webView 的数据内容
    private fun initWebView() {
        val drawable = resources.getDrawable(R.drawable.progress_bar_states, null)
        progressbar = ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal)
        var params = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 5)
        progressbar!!.layoutParams = LinearLayout.LayoutParams(params)
        progressbar!!.progressDrawable = drawable
        comment_detail_web!!.addView(progressbar)

        comment_detail_web.settings.javaScriptEnabled = true  // 设置js支持
        comment_detail_web.settings.domStorageEnabled = true  // 开启 DOM storage API 功能
        comment_detail_web.settings.textZoom = 100            // 设置网页字体不跟随系统字体发生改变
        comment_detail_web.settings.useWideViewPort = true    // 缩放至屏幕的大小
        comment_detail_web.settings.loadWithOverviewMode = true
        // 是否支持缩放
        comment_detail_web.settings.setSupportZoom(true)
        comment_detail_web.settings.builtInZoomControls = true
        comment_detail_web.settings.displayZoomControls = false
        comment_detail_web.settings.cacheMode = WebSettings.LOAD_NO_CACHE     // 不缓存
        comment_detail_web.settings.allowContentAccess = true

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            comment_detail_web.settings.setMixedContentMode(WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE);
        }

        comment_detail_web.settings.setBlockNetworkImage(false);

        dialog = ShareOptionDialog(this@CommentDetailsActivity)
        dialog?.setOnDismisListener(onDismisListener)
        loadContent()
    }

    private var onDismisListener = object : ShareOptionDialog.OnDismisListener {
        override fun onShareWechatClicked() {
            if (TextUtils.isEmpty(wechatPath) || TextUtils.isEmpty(wechatShareUrl)) {
                T.show(getString(R.string.unknown_error))
                return
            }

            WeChatLogin.getInstance().shareMiniProgram(this@CommentDetailsActivity, wechatShareUrl, wechatPath, shareImg)
        }

        override fun onCopyLinkClicked() {
            Utils.copy(this@CommentDetailsActivity, webShareUrl)
            T.show(getString(R.string.copyed))
        }

    }

    private fun getAppGetTokenTicket() {
        HttpRequest.instance.getOneTimeTokenTicket(this)
    }

    override fun fail(msg: String?, reqCode: Int) {
        T.show(msg)
    }

    override fun success(response: BaseResponse, reqCode: Int) {
        if (response.code !== 0) {
            T.show(response.msg)
            return
        }
        var js = JSON.parse(response.data.toString()) as JSONObject
        var weburl = CommonField.H5_BASE_URL + "?uin=${Utils.getAndroidID(this)}#" + pathUrl + "&ticket=${js[CommonField.TOKEN_TICKET]}"
        comment_detail_web.loadUrl(weburl)
    }

    private fun loadContent() {
        var extraInfo = intent.getStringExtra(CommonField.EXTRA_INFO)

        var uri = Uri.parse(extraInfo)
        if (uri == null) return

        var json = JSON.parseObject(uri.query)
        if (json != null && json.containsKey(CommonField.KEY_URL) &&
            !TextUtils.isEmpty(json.getString(CommonField.KEY_URL))) {
            comment_detail_web.webViewClient = webViewClient
            comment_detail_web.webChromeClient = webChromeClient

            pathUrl = json.getString(CommonField.KEY_URL)
            getAppGetTokenTicket()
        }

    }

    override fun setListener() {
        iv_back.setOnClickListener(this)
    }

    // 拦截当前窗口的返回按钮的点击事件，保证网页之间的跳转
    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (comment_detail_web.canGoBack()) {
                comment_detail_web.goBack() //返回上一页面
                return true
            } else { // 关闭当前窗口
                this@CommentDetailsActivity.finish()
            }
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onClick(v: View?) {
        when(v) {
            iv_back -> {
                if (comment_detail_web.canGoBack()) {
                    comment_detail_web.goBack() //返回上一页面
                } else { // 关闭当前窗口
                    this@CommentDetailsActivity.finish()
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        comment_detail_web.clearHistory()
        comment_detail_web.clearCache(true)
        comment_detail_web.loadUrl("about:blank")
    }

    val webChromeClient = object: WebChromeClient() {

        override fun onShowFileChooser (webView: WebView, filePathCallback: ValueCallback<Array<Uri>>, fileChooserParams: FileChooserParams?): Boolean {
            return true
        }

        override fun onReceivedTitle(view: WebView?, title: String?) {
            super.onReceivedTitle(view, title)
            tv_title.setText(title)
        }

        override fun onReceivedTouchIconUrl(view: WebView?, url: String?, precomposed: Boolean) {}

        override fun onJsPrompt(view: WebView, url: String, message: String, defaultValue: String, result: JsPromptResult): Boolean {
            result.confirm(JSBridgeKt.callNative(view, message))
            return true
        }
    }

    var webViewClient = object: WebViewClient() {
        override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
            if (url.contains("onArticleShare?")) {
                makeShortUrl(url)
                dialog?.show()
                return true
            } else {
                return true
            }
        }
    }

    private fun makeShortUrl (url: String) {
        var uri = Uri.parse(url)
        if (TextUtils.isEmpty(uri.encodedQuery)) return

        var json = JSONObject.parseObject(uri.encodedQuery)

        if (json.containsKey("webShareUrl")) {
            webShareUrl = json.getString("webShareUrl")
        }

        if (json.containsKey("wechatShareUrl")) {
            wechatShareUrl = json.getString("wechatShareUrl")
        }

        if (json.containsKey("wechatPagePath")) {
            wechatPath = json.getString("wechatPagePath")
        }

        if (json.containsKey("shareImg")) {
            shareImg = json.getString("shareImg")
        }
    }
}

