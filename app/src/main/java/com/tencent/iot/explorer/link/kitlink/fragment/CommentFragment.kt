package com.tencent.iot.explorer.link.kitlink.fragment

import android.content.Intent
import android.net.Uri
import android.view.View
import android.view.ViewGroup
import android.webkit.*
import android.widget.LinearLayout
import android.widget.ProgressBar
import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.JSONObject
import com.tencent.iot.explorer.link.App
import com.tencent.iot.explorer.link.R
import com.tencent.iot.explorer.link.T
import com.tencent.iot.explorer.link.core.auth.response.BaseResponse
import com.tencent.iot.explorer.link.core.utils.Utils
import com.tencent.iot.explorer.link.kitlink.activity.CommentDetailsActivity
import com.tencent.iot.explorer.link.kitlink.consts.CommonField
import com.tencent.iot.explorer.link.kitlink.util.HttpRequest
import com.tencent.iot.explorer.link.kitlink.util.MyCallback
import com.tencent.iot.explorer.link.kitlink.webview.BridgeImpl
import com.tencent.iot.explorer.link.kitlink.webview.JSBridgeKt
import com.tencent.iot.explorer.link.mvp.IPresenter
import kotlinx.android.synthetic.main.fragment_comment.*

/**
 *  评测界面
 */
class CommentFragment : BaseFragment(), View.OnClickListener, MyCallback {
    private var progressbar: ProgressBar? = null

    override fun getContentView(): Int {
        return R.layout.fragment_comment
    }

    override fun getPresenter(): IPresenter? {
        return null
    }

    // 每次窗口返回刷新一次
    override fun onResume() {
        super.onResume()
        initView()
        getAppGetTokenTicket()
    }

    // 每次刷新显示，重新获取一次
    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (!hidden) {
            getAppGetTokenTicket()
        }
    }

    override fun startHere(view: View) {
        setListener()
    }

    private fun setListener() {

    }

    private fun initView() {
        val drawable = resources.getDrawable(R.drawable.progress_bar_states, null)
        progressbar = ProgressBar(context, null, android.R.attr.progressBarStyleHorizontal)
        var params = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 5)
        progressbar!!.layoutParams = LinearLayout.LayoutParams(params)
        progressbar!!.progressDrawable = drawable
        web_comment!!.addView(progressbar)

        web_comment.settings.javaScriptEnabled = true  // 设置js支持
        web_comment.settings.domStorageEnabled = true  // 开启 DOM storage API 功能
        web_comment.settings.textZoom = 100            // 设置网页字体不跟随系统字体发生改变
        web_comment.settings.useWideViewPort = true    // 缩放至屏幕的大小
        web_comment.settings.loadWithOverviewMode = true
        // 是否支持缩放
        web_comment.settings.setSupportZoom(true)
        web_comment.settings.builtInZoomControls = true
        web_comment.settings.displayZoomControls = false
        web_comment.settings.cacheMode = WebSettings.LOAD_NO_CACHE     // 不缓存
    }

    override fun onClick(v: View?) {
        when (v) {

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
        JSBridgeKt.register("help_center_bridge", BridgeImpl::class.java)

        web_comment.webViewClient = webViewClient
        web_comment.webChromeClient = webChromeClient

        if (response.code == 0) {
            var js = JSON.parse(response.data.toString()) as JSONObject
            var url = CommonField.H5_BASE_URL + "?ticket=" + js[CommonField.TOKEN_TICKET]
            url += ("&uin=" + Utils.getAndroidID(context!!))
            if (!App.isOEMApp()) {
                url += "&appID=" + T.getContext().applicationInfo.packageName
            }
            web_comment.loadUrl(url)
        }
    }

    val webChromeClient = object: WebChromeClient() {

        override fun onShowFileChooser (webView: WebView, filePathCallback: ValueCallback<Array<Uri>>, fileChooserParams: FileChooserParams?): Boolean {
            return true
        }

        override fun onReceivedTitle(view: WebView?, title: String?) {
            super.onReceivedTitle(view, title)
        }

        override fun onReceivedTouchIconUrl(view: WebView?, url: String?, precomposed: Boolean) {
        }

        override fun onJsPrompt(view: WebView, url: String, message: String, defaultValue: String, result: JsPromptResult): Boolean {
            result.confirm(JSBridgeKt.callNative(view, message))
            return true
        }
    }

    var webViewClient = object: WebViewClient() {
        override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
            // 跳转到评测详情界面
            if (url.contains("iot-explorer-help-center://") && url.contains("goDetail")) {
                var intent = Intent(context, CommentDetailsActivity::class.java)
                intent.putExtra(CommonField.EXTRA_INFO, url)
                startActivity(intent)
            }
            return true
        }

    }


}