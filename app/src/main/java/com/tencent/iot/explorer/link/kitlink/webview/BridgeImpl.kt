package com.tencent.iot.explorer.link.kitlink.webview

import android.app.Activity
import android.content.Intent
import android.util.Log
import android.webkit.WebView
import android.widget.Toast
import com.tencent.iot.explorer.link.kitlink.activity.DeviceDetailsActivity
import com.tencent.iot.explorer.link.kitlink.activity.LoginActivity
import com.tencent.iot.explorer.link.kitlink.activity.TestBrowserActivity
import com.tencent.iot.explorer.link.kitlink.util.AppData
import org.json.JSONObject


object BridgeImpl {

    private val LOGIN_REQUEST_CODE = 108
        /*
        h5调用原生方法，不回调
     */
         fun testFormH5(
            webView: WebView,
            param: JSONObject,
            callback: WebCallBack?
        ) {
            Log.d("testFormH5AndBack", "type: 113333")
            val type = param.optString("type")
            val activity = webView.context as TestBrowserActivity
            Log.d("testFormH5", "type: $type")
            when (type) {
                "fromH5" -> Toast.makeText(activity, type, Toast.LENGTH_LONG).show()
            }
        }

        /*
        h5调用原生方法，并回调
     */
          fun testFormH5AndBack(
            webView: WebView,
            param: JSONObject,
            callback: WebCallBack?
        ) {
            Log.d("testFormH5AndBack", "type: 11122333")
            val type = param.optString("type")
            val activity = webView.context as TestBrowserActivity
            Log.d("testFormH5AndBack", "type: $type")
            try {
                val data = JSONObject()
                when (type) {
                    "fromH5AndBack" -> {
                        Toast.makeText(activity, type, Toast.LENGTH_LONG).show()
                        data.put("formNative", "回调成功")
                    }
                }
                if (null != callback) {
                    val jsObject = JSONObject()
                    jsObject.put("responseId", callback.getPort())
                    jsObject.put("responseData", data)
                    callback.apply(jsObject)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        /*
        原生调用h5后回调的原生方法
     */
           fun testH5Func(
            webView: WebView,
            param: JSONObject,
            callback: WebCallBack?
        ) {
            Log.d("testFormH5AndBack", "type: 11122")
            val result = param.optString("result")
            val activity = webView.context as TestBrowserActivity
            Log.d("testH5Func", result + "")
            Toast.makeText(activity, result + "", Toast.LENGTH_LONG).show()
        }


    /*
     h5调用原生方法返回上一页
  */
    fun testFormH5FinishActivity(
        webView: WebView,
        param: JSONObject,
        callback: WebCallBack?
    ) {
        val result = param.optString("result")
        val activity = webView.context as TestBrowserActivity
        Toast.makeText(activity, result + "", Toast.LENGTH_LONG).show()
        activity.finish()
    }

     /*
        h5调用原生方法，并回调
     */
     fun openNativeCamera(
         webView: WebView,
         param: JSONObject,
         callback: WebCallBack?
     ) {
         val type = param.optString("type")
         val activity = webView.context as TestBrowserActivity

         try {
             val data = JSONObject()
             when (type) {
                 "fromH5AndBack" -> {
                     Toast.makeText(activity, type, Toast.LENGTH_LONG).show()

                     data.put("formNative", "1")
                 }
             }
             if (null != callback) {
                 val jsObject = JSONObject()
                 jsObject.put("responseId", callback.getPort())
                 jsObject.put("responseData", data)
                 callback.apply(jsObject)
             }
         } catch (e: Exception) {
             e.printStackTrace()
         }
     }


     /*
      h5调用原生方法，并回调
   */
     fun gotoLogin(
         webView: WebView,
         param: JSONObject,
         callback: WebCallBack?
     ) {
         val type = param.optString("type")
         val activity = webView.context as TestBrowserActivity

         try {
             val data = JSONObject()
             when (type) {
                 "fromH5AndBack" -> {
                     Toast.makeText(activity, type, Toast.LENGTH_LONG).show()
                         if(!AppData.instance.getToken().equals("")){
                            activity.jumpActivity(LoginActivity::class.java)
                         }else{
                             data.put("formNative", "登录状态"+AppData.instance.getToken())
                             if (null != callback) {
                                 val jsObject = JSONObject()
                                 jsObject.put("responseId", callback.getPort())
                                 jsObject.put("responseData", data)
                                 callback.apply(jsObject)
                             }
                         }

                 }
             }

         } catch (e: Exception) {
             e.printStackTrace()
         }
     }

     /*
    h5调用原生登录方法，并回调
 */
     fun LoginApp(
         webView: WebView,
         param: JSONObject,
         callback: WebCallBack?
     ) {
         val type = param.optString("type")
         val activity = webView.context as TestBrowserActivity

         try {
             val data = JSONObject()
             when (type) {
                 "fromH5" -> {
                     if(!AppData.instance.getToken().equals("")){

                         val intent = Intent(activity, LoginActivity::class.java)
                         intent.putExtra("from","fromH5")

                         activity.startActivityForResult(intent, LOGIN_REQUEST_CODE)
                     }else{

                         data.put("ticket", ""+AppData.instance.getToken())
                         if (null != callback) {
                             val jsObject = JSONObject()
                             jsObject.put("handlerName", "LoginResult")
                             jsObject.put("responseId", callback.getPort())
                             jsObject.put("responseData", data)
                             callback.apply(jsObject)
                         }
                     }

                 }
             }

         } catch (e: Exception) {
             e.printStackTrace()
         }
     }


}