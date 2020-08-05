package com.tencent.iot.explorer.link.kitlink.activity

import android.net.http.SslError
import android.os.Build
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.webkit.*
import androidx.fragment.app.FragmentActivity
import com.tencent.iot.explorer.link.R
import com.tencent.iot.explorer.link.kitlink.consts.CommonField
import kotlinx.android.synthetic.main.activity_web.*
import kotlinx.android.synthetic.main.menu_back_layout.*


class WebActivity : BaseActivity() {

    override fun getContentView(): Int {
        return R.layout.activity_web
    }

    override fun initView() {
        intent.getStringExtra(CommonField.EXTRA_TITLE)?.let {
            tv_title.text = it
        }
        intent.getStringExtra(CommonField.EXTRA_TEXT)?.let {
            when {
                it.endsWith(".html") || it.endsWith(".htm") -> {
                    /*wv_web.settings.javaScriptEnabled = false
                    wv_web.webViewClient = WebViewClient()
                    wv_web.webChromeClient = WebChromeClient()
                    wv_web.visibility = View.VISIBLE
                    sv_help.visibility = View.GONE
                    wv_web.loadUrl("file:///android_asset/$it")*/
                    showUrl("file:///android_asset/$it")
                }
                it.startsWith("https://") -> {
                    Log.e("XXX", "url=" + it)
                    showUrl(it)
                }
                else -> {
                    wv_web.visibility = View.GONE
                    sv_help.visibility = View.VISIBLE
                    tv_text.text = it
                }
            }
        }
    }

    private fun showUrl(url: String) {
        wv_web.settings.javaScriptEnabled = true
        wv_web.settings.domStorageEnabled = true
        wv_web.settings.useWideViewPort = true
        wv_web.settings.loadWithOverviewMode = true
        wv_web.settings.layoutAlgorithm = WebSettings.LayoutAlgorithm.TEXT_AUTOSIZING
        wv_web.webViewClient = WebViewClient()
        wv_web.webChromeClient = WebChromeClient()
        wv_web.visibility = View.VISIBLE
        sv_help.visibility = View.GONE
        wv_web.setWebChromeClient(object : WebChromeClient() {
            override fun onProgressChanged(view: WebView, newProgress: Int) {
                super.onProgressChanged(view, newProgress)
                Log.d("XXX", "newProgress: $newProgress")
            }
        })
        wv_web.setWebViewClient(object : WebViewClient() {
            override fun onReceivedSslError(
                view: WebView?,
                handler: SslErrorHandler,
                error: SslError?
            ) {
                handler.proceed()
                super.onReceivedSslError(view, handler, error)
                Log.d("XXX", "onReceivedSslError: "
                ) //如果是证书问题，会打印出此条log到console
            }
        })
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            wv_web.getSettings().setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        }
        wv_web.getSettings().setDomStorageEnabled(true);
        wv_web.loadUrl(url)
    }

    override fun setListener() {
        iv_back.setOnClickListener { finish() }
    }

    override fun onBackPressed() {
        if (wv_web.canGoBack()) {
            wv_web.goBack()
        } else {
            super.onBackPressed()
        }
    }

    override fun onDestroy() {
        wv_web?.run {
            (parent as? ViewGroup)?.removeView(wv_web)
            stopLoading()
            clearHistory()
            removeAllViews()
            destroy()
        }
        super.onDestroy()
    }
}