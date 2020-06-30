package com.tencent.iot.explorer.link.kitlink.webview

import android.net.Uri
import android.text.TextUtils
import android.util.Log
import android.webkit.WebView
import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.JSONObject
import java.lang.reflect.Method

object JSBridgeKt {
    val TAG = this.javaClass.simpleName

    var exposedMethods: MutableMap<String, HashMap<String, Method>> = HashMap()

    fun register(exposedName: String, clazz: Class<BridgeImpl>) {
        Log.d(TAG, "register exposedMethods=" + JSON.toJSONString(exposedMethods))
        if (!exposedMethods.containsKey(exposedName)) {
            exposedMethods.put(exposedName, getAllMethod(clazz))
        }
    }

    private fun getAllMethod(injectedCls: Class<*>): HashMap<String, Method> {
        var methodsMap = HashMap<String, Method>()
        var methods = injectedCls.declaredMethods
        Log.d(TAG, "getAllMethod exposedMethods=" + JSON.toJSONString(exposedMethods))
        Log.d(TAG, "getAllMethod methods=" + JSON.toJSONString(methods))

        for (method in methods) {
            var name = method.getName()
            var parameters = method.parameterTypes

            if (null != parameters && parameters.size == 3 &&
                parameters[0] == WebView::class.java &&
                parameters[1] == JSONObject::class.java &&
                parameters[2] == WebCallBack::class.java) {
                methodsMap.put(name, method)
            }
        }
        return methodsMap
    }

    fun callNative(webView: WebView?, uriString: String): String? {
        var methodName = ""
        var className = ""
        var param = "{}"
        var port = ""

        if (!TextUtils.isEmpty(uriString) && uriString.startsWith("iot-explorer-help-center")) {
            val uri = Uri.parse(uriString)
            className = uri.host.toString()
            param = uri.query.toString()
            port = uri.port.toString() + ""
            val path = uri.path.toString()
            if (!TextUtils.isEmpty(path)) {
                methodName = path!!.replace("/", "")
            }
        }

        Log.d(TAG, "uriString=" + uriString)
        Log.d(TAG, "exposedMethods=" + JSON.toJSONString(exposedMethods))
        Log.d(TAG, "className=" + className)
        if (exposedMethods.containsKey(className)) {
            val methodHashMap = exposedMethods[className]

            if (methodHashMap != null && methodHashMap.size != 0 &&
                methodHashMap.containsKey(methodName)) {

                val constructor = BridgeImpl::class.java.getDeclaredConstructor()
                constructor.isAccessible = true
                val targetActivity = constructor.newInstance()
                val method = methodHashMap[methodName]
                Log.d(TAG, "methodName=" + methodName)

                if (method != null) {
                    try {
                        method.invoke(targetActivity, webView, JSON.parse(param) as JSONObject,
                            WebCallBack(webView!!, port))
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }
        return null
    }
}