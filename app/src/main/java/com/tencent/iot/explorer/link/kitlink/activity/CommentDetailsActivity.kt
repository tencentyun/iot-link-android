package com.tencent.iot.explorer.link.kitlink.activity

import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import android.webkit.WebSettings
import android.widget.LinearLayout
import android.widget.ProgressBar
import com.tencent.iot.explorer.link.R
import com.tencent.iot.explorer.link.customview.dialog.ShareOptionDialog
import kotlinx.android.synthetic.main.activity_comment_detail.*
import kotlinx.android.synthetic.main.activity_help_feedback.*
import kotlinx.android.synthetic.main.menu_back_layout.*

class CommentDetailsActivity: BaseActivity(), View.OnClickListener {
    val TAG = this.javaClass.simpleName

    private var progressbar: ProgressBar? = null
    private var dialog: ShareOptionDialog? = null

    override fun getContentView(): Int {
        return  R.layout.activity_comment_detail
    }

    override fun initView() {
        initWebView()
        iv_back.setColorFilter(R.color.black_333333)
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
        comment_detail_web.loadUrl("www.baidu.com")

        dialog = ShareOptionDialog(this@CommentDetailsActivity, "测试链接")
    }

    override fun setListener() {
        iv_back.setOnClickListener(this)
        test_btn.setOnClickListener(this)
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

            test_btn -> {
                dialog?.show()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        comment_detail_web.clearHistory()
        comment_detail_web.clearCache(true)
        comment_detail_web.loadUrl("about:blank")
    }
}

