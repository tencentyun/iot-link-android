package com.tencent.iot.explorer.link.kitlink.activity

import android.content.Intent
import android.net.Uri
import android.view.View
import android.view.ViewGroup
import android.webkit.*
import com.tencent.iot.explorer.link.R
import com.tencent.iot.explorer.link.kitlink.consts.CommonField
import com.tencent.iot.explorer.link.kitlink.consts.CommonField.THIRD_SDK_URL_US_EN
import com.tencent.iot.explorer.link.kitlink.consts.CommonField.THIRD_SDK_URL_US_ZH
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
        val mWebViewClient = object : WebViewClient(){
            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                if (request?.url.toString().contains(THIRD_SDK_URL_US_ZH)) {
                    val intent = Intent(this@WebActivity, WebActivity::class.java)
                    intent.putExtra(CommonField.EXTRA_TITLE, getString(R.string.rule_content_list))
                    var url = THIRD_SDK_URL_US_ZH
                    intent.putExtra(CommonField.EXTRA_TEXT, url)
                    startActivity(intent)
                    return true
                } else if (request?.url.toString().contains(THIRD_SDK_URL_US_EN)) {
                    OpensourceLicenseActivity.startWebWithExtra(this@WebActivity, getString(R.string.rule_content_list), THIRD_SDK_URL_US_EN)
                    return true
                } else {
                    return super.shouldOverrideUrlLoading(view, request)
                }
            }
        }
        wv_web.webViewClient = mWebViewClient
        wv_web.webChromeClient = WebChromeClient()
        wv_web.visibility = View.VISIBLE
        sv_help.visibility = View.GONE
        wv_web.loadUrl(url)
        wv_web.setDownloadListener(myDownloadListener)
    }
    private val myDownloadListener = DownloadListener { url, _, _, _, _ ->
        val uri: Uri = Uri.parse(url)
        val intent = Intent(Intent.ACTION_VIEW, uri)
        startActivity(intent)
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