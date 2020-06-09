package com.kitlink.activity

import android.view.View
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import com.kitlink.R
import com.mvp.IPresenter
import com.kitlink.activity.BaseActivity
import kotlinx.android.synthetic.main.activity_web.*
import kotlinx.android.synthetic.main.menu_back_layout.*

class WebActivity : BaseActivity() {

    override fun getContentView(): Int {
        return R.layout.activity_web
    }

    override fun initView() {
        intent.getStringExtra("title")?.let {
            tv_title.text = it
        }
        intent.getStringExtra("text")?.let {
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