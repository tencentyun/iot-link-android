package com.tencent.iot.explorer.link.kitlink.webview

import android.content.Intent
import android.util.Log
import android.webkit.WebView
import android.widget.Toast
import com.alibaba.fastjson.JSONObject
import com.tencent.iot.explorer.link.App
import com.tencent.iot.explorer.link.kitlink.activity.LoginActivity
import com.tencent.iot.explorer.link.kitlink.activity.HelpWebViewActivity
import com.tencent.iot.explorer.link.kitlink.consts.CommonField
import com.tencent.iot.explorer.link.kitlink.util.AppData
import com.tencent.iot.explorer.link.util.T


object BridgeImpl {
    val TAG = this.javaClass.simpleName

    // h5调用原生方法，不回调
    fun testFormH5(webView: WebView, param: JSONObject, callback: WebCallBack) {
        Log.d(TAG, "testFormH5")
        val type = param.getString("type")
        Log.d(TAG, "type: $type")

        when (type) {
            CommonField.WAY_SOURCE -> T.show(type)
        }
    }

    // h5调用原生方法，并回调
    fun testFormH5AndBack(webView: WebView, param: JSONObject, callback: WebCallBack) {
        Log.d(TAG, "testFormH5AndBack")
        val type = param.getString("type")
        Log.d(TAG, "type: $type")
        val data = JSONObject()

        when (type) {
            "fromH5AndBack" -> {
                T.show(type)
                data.put("formNative", "回调成功")
            }
        }
        if (null != callback) {
            val jsObject = JSONObject()
            jsObject.put("responseId", callback.getPort())
            jsObject.put("responseData", data)
            callback.apply(jsObject)
        }
    }

    // 原生调用h5后回调的原生方法
    fun testH5Func(webView: WebView, param: JSONObject, callback: WebCallBack) {
        Log.d(TAG, "testH5Func")
        val result = param.getString("result")
        Log.d(TAG, "testH5Func result=" + result)
        T.show(result)
    }


    // h5调用原生方法返回上一页
    fun testFormH5FinishActivity(webView: WebView, param: JSONObject, callback: WebCallBack) {
        val result = param.getString("result")
        val activity = webView.context as HelpWebViewActivity
        T.show(result)
        activity.finish()
    }

    // h5调用原生方法，并回调
    fun openNativeCamera(webView: WebView, param: JSONObject, callback: WebCallBack) {
        val type = param.getString("type")
        val data = JSONObject()

        when (type) {
            "fromH5AndBack" -> {
                T.show(type)
                data.put("formNative", "1")
            }
        }

        if (null != callback) {
            val jsObject = JSONObject()
            jsObject.put("responseId", callback.getPort())
            jsObject.put("responseData", data)
            callback.apply(jsObject)
        }
    }


    // h5调用原生方法，并回调
    fun gotoLogin(webView: WebView, param: JSONObject, callback: WebCallBack) {
        val type = param.getString("type")
        val activity = webView.context as HelpWebViewActivity
        val data = JSONObject()

        when (type) {
            "fromH5AndBack" -> {
                Toast.makeText(activity, type, Toast.LENGTH_LONG).show()
                if (!AppData.instance.getToken().equals("")) {
                    activity.jumpActivity(LoginActivity::class.java)

                } else if (null != callback) {
                    data.put("formNative", "登录状态" + AppData.instance.getToken())
                    val jsObject = JSONObject()
                    jsObject.put("responseId", callback.getPort())
                    jsObject.put("responseData", data)
                    callback.apply(jsObject)
                }
            }
        }
    }

    // h5调用原生登录方法，并回调
    fun LoginApp(webView: WebView, param: JSONObject, callback: WebCallBack) {
        val type = param.getString("type")
        val activity = webView.context as HelpWebViewActivity
        val data = JSONObject()

        when (type) {
            CommonField.WAY_SOURCE -> {
                if (!AppData.instance.getToken().isEmpty()) {
                    App.data.clear()
                    val intent = Intent(activity, LoginActivity::class.java)
                    intent.putExtra(CommonField.FROM, CommonField.WAY_SOURCE)
                    activity.startActivityForResult(intent, CommonField.LOGIN_REQUEST_CODE)

                } else if (null != callback) {
                    data.put(CommonField.TICKET, "" + AppData.instance.getToken())
                    val jsObject = JSONObject()
                    jsObject.put(CommonField.HANDLER_NAME, "LoginResult")
                    jsObject.put("responseId", callback.getPort())
                    jsObject.put("responseData", data)
                    callback.apply(jsObject)
                }
            }
        }
    }
}