package com.tencent.iot.explorer.link.kitlink.webview

import android.content.Context
import android.net.Uri
import android.text.TextUtils
import android.util.Log
import android.webkit.WebView
import org.json.JSONObject
import java.lang.reflect.Method

 object JSBridgeKt {

         var exposedMethods: MutableMap<String?, HashMap<String, Method>> =
            HashMap()

     fun register(
            exposedName: String?,
            clazz: Class<BridgeImpl>
        ) {
            Log.i("js_bridge_uri:---", exposedMethods.toString()+ "")
            if (!exposedMethods.containsKey(exposedName)) {
                try {
                    Log.i("js_bridge_uri:---111", exposedMethods.toString()+ "")
                    exposedMethods.put(exposedName,getAllMethod(clazz))
                    Log.i("js_bridge_uri:---222", exposedMethods.toString()+ "")
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

        private fun getAllMethod(injectedCls: Class<*>): HashMap<String, Method> {
            var mMethodsMap =
                HashMap<String, Method>()
            var methods = injectedCls.declaredMethods

            Log.i("js_bridge_uri:---3333", methods.toString()+ "")
            for (m in methods) {
                // Throw NoClassDefFoundError if types cannot be resolved.
                m.returnType
                m.parameterTypes
                Log.i("js_bridge_uri:---4444",   "----m.parameterTypes==="+m.parameterTypes+"--m.parameterTypes==="+m.parameterTypes)
            }
            for (method in methods) {
                Log.i("js_bridge_uri:---4444", exposedMethods.toString()+ "")
                var name = method.getName()

                Log.i("js_bridge_uri:---66666", exposedMethods.toString()+ "")
                var parameters = method.parameterTypes
                if (null != parameters && parameters.size == 3) {
                    Log.i("js_bridge_uri:---999", exposedMethods.toString()+ "")

                    if (parameters[0] == WebView::class.java && parameters[1] == JSONObject::class.java && parameters[2] == WebCallBack::class.java
                    ) {
                        mMethodsMap.put(name, method)
                        Log.i("js_bridge_uri:---55555", exposedMethods.toString()+ "")
                    }
                }
            }
            return mMethodsMap
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
            Log.i("js_bridge_uri:---", uriString + "")
            if (exposedMethods.containsKey(className)) {
                val methodHashMap =
                    exposedMethods[className]
                if (methodHashMap != null && methodHashMap.size != 0 && methodHashMap.containsKey(
                        methodName
                    )
                ) {
                    val constructor = BridgeImpl::class.java.getDeclaredConstructor()
                    constructor.isAccessible=true
                    val targetActivity = constructor?.newInstance()
                    val method = methodHashMap[methodName]
                    if (method != null) {
                        try {
                            method.invoke(
                                targetActivity,
                                webView,
                                JSONObject(param),
                                WebCallBack(webView, port)
                            )
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
            }
            return null
        }

}