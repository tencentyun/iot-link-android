package com.tencent.iot.explorer.link.kitlink.webview

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.content.Intent
import android.os.ParcelUuid
import android.text.TextUtils
import android.util.Log
import android.webkit.WebView
import android.widget.Toast
import androidx.core.util.keyIterator
import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.JSONArray
import com.alibaba.fastjson.JSONObject
import com.tencent.iot.explorer.link.App
import com.tencent.iot.explorer.link.AppData
import com.tencent.iot.explorer.link.T
import com.tencent.iot.explorer.link.core.auth.message.upload.ArrayString
import com.tencent.iot.explorer.link.core.log.L
import com.tencent.iot.explorer.link.kitlink.activity.HelpWebViewActivity
import com.tencent.iot.explorer.link.kitlink.activity.LoginActivity
import com.tencent.iot.explorer.link.kitlink.consts.CommonField
import com.tencent.iot.explorer.link.kitlink.entity.BluetoothDevicesRequired
import com.tencent.iot.explorer.link.kitlink.entity.BuleToothDev
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashSet

object BridgeImpl {
    val TAG = this.javaClass.simpleName
    val buleToothDevs = ArrayList<BuleToothDev>()
    val buleToothDevSet = HashSet<String>()
    private var scanCallback: ScanCallback? = null

    // h5调用原生方法，不回调
    fun testFormH5(webView: WebView, param: JSONObject, callback: WebCallBack) {
        L.d(TAG, "testFormH5")
        val type = param.getString("type")
        L.d(TAG, "type: $type")

        when (type) {
            CommonField.WAY_SOURCE -> T.show(type)
        }
    }

    // h5调用原生方法，并回调
    fun testFormH5AndBack(webView: WebView, param: JSONObject, callback: WebCallBack) {
        L.d(TAG, "testFormH5AndBack")
        val type = param.getString("type")
        L.d(TAG, "type: $type")
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
        L.d(TAG, "testH5Func")
        val result = param.getString("result")
        L.d(TAG, "testH5Func result=" + result)
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

    fun generateSuccessedData(callbackId: String?): JSONObject {
        return generateSuccessedData(callbackId, null)
    }

    fun generateSuccessedData(callbackId: String?, dataDataJson: JSONObject?): JSONObject {
        val jsObject = JSONObject()
        jsObject.put("handlerName", "callResult")
        var dataJson = JSONObject()
        dataJson.put("result", true)
        dataJson.put("callbackId", callbackId)
        if (dataDataJson != null) {
            dataJson.put("data", dataDataJson)
        }
        jsObject.put("data", dataJson)
        return jsObject
    }

    fun generateFailedData(callbackId: String?, errCode: Int, errReson: String): JSONObject {
        val jsObject = JSONObject()
        jsObject.put("handlerName", "callResult")
        var dataJson = JSONObject()
        dataJson.put("result", false)
        dataJson.put("callbackId", callbackId)
        var reasonData = JSONObject()
        reasonData.put("errCode", errCode)
        reasonData.put("errMsg", errReson)
        dataJson.put("data", reasonData)
        jsObject.put("data", dataJson)
        return jsObject
    }

    fun openBluetoothAdapter(webView: WebView, param: JSONObject, callback: WebCallBack) {
        var callbackId = param.getString("callbackId")
        var bluetoothmanger = webView.context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        var bluetoothadapter = bluetoothmanger.getAdapter();
        if (bluetoothadapter == null && null != callback) {
            var jsObject = generateFailedData(callbackId, 10001, "")
            callback.apply(jsObject)
            return
        }

        if (!bluetoothadapter.isEnabled()) {
            var enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            var ctx = webView.context as Activity
            ctx.startActivityForResult(enableBtIntent, JSBridgeKt.OPEN_BULE_TOOTH_REQ_CODE)
            JSBridgeKt.onEventCallback = object : OnEventCallback {
                override fun onActivityShow() {
                    Log.e("XXX", "onActivityShow")
                    var ret = JSONObject()
                    if (!bluetoothadapter.isEnabled()) {
                        ret = generateFailedData(callbackId, 10001, "")
                    } else {
                        ret = generateSuccessedData(callbackId)
                    }
                    callback.apply(ret)
                }
            }
        } else {
            var ret = generateSuccessedData(callbackId)
            callback.apply(ret)
        }
    }

    fun getBluetoothAdapterState(webView: WebView, param: JSONObject, callback: WebCallBack) {
        Log.e("XXX", "getBluetoothAdapterState param " + JSON.toJSONString(param))
        var callbackId = param.getString("callbackId")
        var bluetoothmanger = webView.context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        var bluetoothadapter = bluetoothmanger.getAdapter()
        var dataJson = JSONObject()
        if (bluetoothadapter == null || !bluetoothadapter.isEnabled()) {
            dataJson.put("available", false)
            dataJson.put("discovering", false)
        } else {
            dataJson.put("available", true)
            dataJson.put("discovering", bluetoothadapter.isDiscovering)
        }
        var jsObject = generateSuccessedData(callbackId, dataJson)
        Log.e("XXX", "jsObject " + JSON.toJSONString(jsObject))
        callback.apply(jsObject)
    }

    fun startBluetoothDevicesDiscovery(webView: WebView, param: JSONObject, callback: WebCallBack) {
        buleToothDevs.clear()
        buleToothDevSet.clear()
        Log.e("XXX", "startBluetoothDevicesDiscovery param " + JSON.toJSONString(param))
        var bluetoothDevicesRequired = JSONObject.parseObject(param.toJSONString(), BluetoothDevicesRequired::class.java)
        var bluetoothmanger = webView.context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        var bluetoothadapter = bluetoothmanger.getAdapter()
        var callbackId = param.getString("callbackId")

        var flag = true
        var jsObject = JSONObject()
        if (bluetoothadapter.isDiscovering()) {
            flag = bluetoothadapter.cancelDiscovery()
            if (!flag) {
                jsObject = generateFailedData(callbackId, 10001, "")
                callback.apply(jsObject)
                return
            }
        }

        flag = bluetoothadapter.startDiscovery()
        if (!flag) {
            jsObject = generateFailedData(callbackId, 10001, "")
            callback.apply(jsObject)
            return
        }

        jsObject = generateSuccessedData(callbackId)
        if (bluetoothDevicesRequired != null) {
            var filters = ArrayList<ScanFilter>()
            if (bluetoothDevicesRequired.services != null
                && bluetoothDevicesRequired.services.size() > 0) {
                for (i in 0 until bluetoothDevicesRequired.services.size()) {
                    var uuid = ParcelUuid.fromString(bluetoothDevicesRequired.services.getValue(i))
                    uuid?.let {
                        var scanFilter = ScanFilter.Builder()
                            .setServiceUuid(it).build()
                        filters.add(scanFilter)
                    }
                }
            }

            var scanMode = ScanSettings.SCAN_MODE_BALANCED
            if (bluetoothDevicesRequired.powerLevel == "low") {
                scanMode = ScanSettings.SCAN_MODE_LOW_POWER
            } else if (bluetoothDevicesRequired.powerLevel == "high") {
                scanMode = ScanSettings.SCAN_MODE_OPPORTUNISTIC
            }
            var scanSettings = ScanSettings.Builder()
                .setReportDelay(bluetoothDevicesRequired.interval.toLong())
                .setScanMode(scanMode)
                .build()
            scanCallback = getScanCallback(bluetoothDevicesRequired.allowDuplicatesKey)
            bluetoothadapter.bluetoothLeScanner.startScan(filters, scanSettings, scanCallback)
        }

        callback.apply(jsObject)
    }

    private fun getScanCallback(allowDuplicatesKey: Boolean): ScanCallback {
        return object : ScanCallback() {
            override fun onScanResult(callbackType: Int, result: ScanResult?) {
                super.onScanResult(callbackType, result)
                if (result == null) return

                var duleToothDev = BuleToothDev()
                duleToothDev.RSSI = result.rssi

                if (result.device != null) {
                    if(result.device.name != null) {
                        duleToothDev.name = result.device.name
                        duleToothDev.localName = result.device.name
                    }
                    duleToothDev.deviceId = result.device.address
                }

                if (result.scanRecord != null) {
                    if (result.scanRecord!!.serviceUuids != null) {
                        var uuidsArr = JSONArray()
                        for (ele in result.scanRecord!!.serviceUuids) {
                            uuidsArr.add(ele.uuid.toString())
                        }
                        duleToothDev.advertisServiceUUIDs = uuidsArr
                    }

                    if (result.scanRecord!!.serviceData != null) {
                        var json = JSONObject()
                        for (item in result.scanRecord!!.serviceData.entries) {
                            json.put(item.key.toString(), Arrays.toString(item.value))
                        }
                        duleToothDev.serviceData = json
                    }

                    if (result.scanRecord!!.manufacturerSpecificData != null) {
                        var arr = JSONArray()
                        for (i in 0 .. result.scanRecord!!.manufacturerSpecificData.size()) {
                            var index = result.scanRecord!!.manufacturerSpecificData.keyAt(i)
                            var value = result.scanRecord!!.manufacturerSpecificData.get(index)

                            var tmp = JSONArray.parseArray(Arrays.toString(value))
                            if (TextUtils.isEmpty(Arrays.toString(value)) || tmp == null) {
                                continue
                            }

                            for (k in tmp) {
                                arr.add(k.toString())
                            }
                        }
                        duleToothDev.advertisData = arr
                    }
                }
                Log.e("XXX", "duleToothDev " + JSON.toJSONString(duleToothDev))
                if (TextUtils.isEmpty(duleToothDev.deviceId)) {
                    return
                }
                // 发现已包含该设备，不添加，不回调
                if (!allowDuplicatesKey && buleToothDevSet.contains(duleToothDev.deviceId)) {
                    return
                }
                buleToothDevSet.add(duleToothDev.deviceId)
                buleToothDevs.add(duleToothDev)
            }
        }
    }

    fun stopBluetoothDevicesDiscovery(webView: WebView, param: JSONObject, callback: WebCallBack) {
        var bluetoothmanger = webView.context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        var bluetoothadapter = bluetoothmanger.getAdapter()
        var callbackId = param.getString("callbackId")

        var flag = true
        var jsObject = JSONObject()

        if (bluetoothadapter.isDiscovering()) {
            flag = bluetoothadapter.cancelDiscovery()
        }
        if (flag) {
            jsObject = generateSuccessedData(callbackId)
        } else {
            jsObject = generateFailedData(callbackId, 10001, "")
        }
        if (scanCallback != null) {
            bluetoothadapter.bluetoothLeScanner.stopScan(scanCallback)
        }
        scanCallback = null
        callback.apply(jsObject)
    }

    fun getBluetoothDevices(webView: WebView, param: JSONObject, callback: WebCallBack) {
        var dataDataJson = JSONObject()
        var devsJsonArr = JSONArray.toJSON(buleToothDevs) as JSONArray
        dataDataJson.put("devices", devsJsonArr)
        var callbackId = param.getString("callbackId")
        var jsObject = generateSuccessedData(callbackId, dataDataJson)
        Log.e("XXX", "jsObject " + JSONObject.toJSONString(jsObject))
        callback.apply(jsObject)
    }

    fun getConnectedBluetoothDevices(webView: WebView, param: JSONObject, callback: WebCallBack) {
        var bluetoothmanger = webView.context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        var bluetoothadapter = bluetoothmanger.getAdapter()

        val devs = ArrayList<BuleToothDev>()
        var servicesJsonArr = param.getJSONArray("services")
        var services = ArrayString()
        for (i in 0 until servicesJsonArr.size) {
            services.addValue(servicesJsonArr.getString(i))
        }
        for (dev in bluetoothadapter.bondedDevices) {
            for (uuid in dev.uuids) {
                if (services.contains(uuid.toString())) {
                    var item = BuleToothDev()
                    item.name = dev.name
                    item.deviceId = dev.address
                    devs.add(item)
                    break
                }
            }
        }

        var dataDataJson = JSONObject()
        var devsJsonArr = JSONArray.toJSON(devs) as JSONArray
        dataDataJson.put("devices", devsJsonArr)
        var callbackId = param.getString("callbackId")
        var jsObject = generateSuccessedData(callbackId, dataDataJson)
        Log.e("XXX", "jsObject " + jsObject.toJSONString())
        callback.apply(jsObject)
    }

    fun getBLEDeviceRSSI(webView: WebView, param: JSONObject, callback: WebCallBack) {
        Log.e("XXX", "getBLEDeviceRSSI param " + param.toJSONString())
        var rssi = 0
        var devId = param.getString("deviceId")
        var i = buleToothDevs.size - 1
        while(i >= 0) {
            Log.e("XXX", "i = " + i)
            if (buleToothDevs.get(i).deviceId == devId) {
                rssi = buleToothDevs.get(i).RSSI
                break
            }
            i--
        }
        var dataDataJson = JSONObject()
        dataDataJson.put("RSSI", rssi)
        var callbackId = param.getString("callbackId")
        var jsObject = generateSuccessedData(callbackId, dataDataJson)
        callback.apply(jsObject)
    }


}