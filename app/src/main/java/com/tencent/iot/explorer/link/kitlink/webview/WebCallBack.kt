package com.tencent.iot.explorer.link.kitlink.webview

import android.os.Handler
import android.os.Looper
import android.webkit.WebView
import com.alibaba.fastjson.JSONObject
import java.lang.ref.WeakReference

class WebCallBack {

    private val CALLBACK_JS_FORMAT = "javascript:JSBridge._handleMessageFromNative('%s');"
    private val mHandler = Handler(Looper.getMainLooper())
    private var mPort: String? = null
    private var mWebViewRef: WeakReference<WebView>? = null

    //原生被调用使用
    constructor(view: WebView, port: String?) {
        mWebViewRef = WeakReference(view)
        mPort = port
    }

    //原生主动调用js使用
    constructor(view: WebView) {
        mWebViewRef = WeakReference(view)
    }

    fun apply(jsonObject: JSONObject) {
        val execJs = String.format(CALLBACK_JS_FORMAT, jsonObject.toString())
        if (mWebViewRef != null && mWebViewRef!!.get() != null) {
            mHandler.post {
                mWebViewRef!!.get()!!.loadUrl(execJs)
            }
        }
    }

    fun getPort(): String? {
        return mPort
    }
}