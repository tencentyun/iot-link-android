package com.tencent.iot.explorer.link.kitlink.webview

import android.app.Activity
import android.bluetooth.*
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.ParcelUuid
import android.text.TextUtils
import android.util.Log
import android.webkit.WebView
import android.widget.Toast
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
import com.tencent.iot.explorer.link.kitlink.webview.BlueToothStateReceiver.OnBlueToothStateListener
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.collections.ArrayList
import kotlin.collections.HashSet
import kotlin.experimental.and


object BridgeImpl {
    val TAG = this.javaClass.simpleName
    val buleToothDevs = ArrayList<BuleToothDev>()
    val buleToothDevSet = HashSet<String>()
    var connectionHandler = ConcurrentHashMap<String, BluetoothGatt>()
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
        jsObject.put(CommonField.HANDLER_NAME, "callResult")
        var dataJson = JSONObject()
        dataJson.put("result", true)
        dataJson.put(CommonField.CALLBACK_ID, callbackId)
        if (dataDataJson != null) {
            dataJson.put(CommonField.DATA_JSON, dataDataJson)
        }
        jsObject.put(CommonField.DATA_JSON, dataJson)
        return jsObject
    }

    fun generateFailedData(callbackId: String?, errCode: Int, errReson: String): JSONObject {
        val jsObject = JSONObject()
        jsObject.put(CommonField.HANDLER_NAME, "callResult")
        var dataJson = JSONObject()
        dataJson.put("result", false)
        dataJson.put(CommonField.CALLBACK_ID, callbackId)
        var reasonData = JSONObject()
        reasonData.put("errCode", errCode)
        reasonData.put("errMsg", errReson)
        dataJson.put(CommonField.DATA_JSON, reasonData)
        jsObject.put(CommonField.DATA_JSON, dataJson)
        return jsObject
    }

    fun openBluetoothAdapter(webView: WebView, param: JSONObject, callback: WebCallBack) {
        var callbackId = param.getString(CommonField.CALLBACK_ID)
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
        registerBlueToothStateReceiver(webView.context, webView)
    }

    fun getBluetoothAdapterState(webView: WebView, param: JSONObject, callback: WebCallBack) {
        var callbackId = param.getString(CommonField.CALLBACK_ID)
        var bluetoothmanger = webView.context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        var bluetoothadapter = bluetoothmanger.getAdapter()
        var dataJson = JSONObject()
        if (bluetoothadapter == null || !bluetoothadapter.isEnabled()) {
            dataJson.put(CommonField.AVAILABLE, false)
            dataJson.put(CommonField.DISCOVERING, false)
        } else {
            dataJson.put(CommonField.AVAILABLE, true)
            dataJson.put(CommonField.DISCOVERING, bluetoothadapter.isDiscovering)
        }
        var jsObject = generateSuccessedData(callbackId, dataJson)
        callback.apply(jsObject)
    }

    fun startBluetoothDevicesDiscovery(webView: WebView, param: JSONObject, callback: WebCallBack) {
        buleToothDevs.clear()
        buleToothDevSet.clear()
        var bluetoothDevicesRequired = JSONObject.parseObject(param.toJSONString(), BluetoothDevicesRequired::class.java)
        var bluetoothmanger = webView.context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        var bluetoothadapter = bluetoothmanger.getAdapter()
        var callbackId = param.getString(CommonField.CALLBACK_ID)

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
            scanCallback = getScanCallback(webView, bluetoothDevicesRequired.allowDuplicatesKey)
            bluetoothadapter.bluetoothLeScanner.startScan(filters, scanSettings, scanCallback)
        }

        callback.apply(jsObject)
        bluetoothAdapterStateChange(webView)
    }

    private fun getScanCallback(webView: WebView, allowDuplicatesKey: Boolean): ScanCallback {
        return object : ScanCallback() {
            override fun onScanResult(callbackType: Int, result: ScanResult?) {
                super.onScanResult(callbackType, result)
                if (result == null) return

                var duleToothDev = BuleToothDev()
                duleToothDev.RSSI = result.rssi

                if (result.device != null) {
                    if (result.device.name != null) {
                        duleToothDev.name = result.device.name
                        duleToothDev.localName = duleToothDev.name
                    }
                    duleToothDev.deviceId = result.device.address
                }

                if (result.scanRecord != null) {
                    if (result.scanRecord!!.serviceUuids != null) {
                        var uuidsArr = JSONArray()
                        for (ele in result.scanRecord!!.serviceUuids) {
                            uuidsArr.add(ele.uuid.toString().toUpperCase())
                        }
                        duleToothDev.advertisServiceUUIDs = uuidsArr
                    }

                    if (result.scanRecord!!.serviceData != null) {
                        var json = JSONObject()
                        for (item in result.scanRecord!!.serviceData.entries) {
                            json.put(item.key.toString().toUpperCase(), Arrays.toString(item.value))
                        }
                        duleToothDev.serviceData = json
                    }

                    if (result.scanRecord!!.manufacturerSpecificData != null) {
                        var arr = JSONArray()
                        for (i in 0 .. result.scanRecord!!.manufacturerSpecificData.size()) {
                            var index = result.scanRecord!!.manufacturerSpecificData.keyAt(i)
                            var value = result.scanRecord!!.manufacturerSpecificData.get(index)
                            if (value != null) {
                                for (byteEle in value) {
                                    val hex = Integer.toHexString(0xFF and byteEle.toInt())
                                    if (hex.length < 2) {
                                        arr.add(("0" + hex).toUpperCase())
                                    } else {
                                        arr.add(hex.toUpperCase())
                                    }
                                }
                            }
                        }
                        duleToothDev.advertisData = arr
                    }
                }
                duleToothDev.dev = result.device
                if (TextUtils.isEmpty(duleToothDev.deviceId)) return
                // 发现已包含该设备，不添加，不回调
                if (!allowDuplicatesKey && buleToothDevSet.contains(duleToothDev.deviceId)) return
                if (TextUtils.isEmpty(duleToothDev.name)) return

                bluetoothDeviceFound(webView)
                buleToothDevSet.add(duleToothDev.deviceId)
                buleToothDevs.add(duleToothDev)
                Log.e("XXX", "---" + JSON.toJSONString(buleToothDevs))
            }
        }
    }

    fun bluetoothAdapterStateChange(webView: WebView) {
        var bluetoothmanger = webView.context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        var bluetoothadapter = bluetoothmanger.getAdapter()
        var dataJson = JSONObject()
        if (bluetoothadapter == null || !bluetoothadapter.isEnabled()) {
            dataJson.put(CommonField.AVAILABLE, false)
            dataJson.put(CommonField.DISCOVERING, false)
        } else {
            dataJson.put(CommonField.AVAILABLE, true)
            dataJson.put(CommonField.DISCOVERING, bluetoothadapter.isDiscovering)
        }

        var callback = WebCallBack(webView)
        val jsObject = JSONObject()
        jsObject.put(CommonField.NAME, "bluetoothAdapterStateChange")
        jsObject.put(CommonField.PAYLOAD, dataJson)

        var ret = JSONObject()
        ret.put(CommonField.HANDLER_NAME, CommonField.EMIT_EVENT)
        ret.put(CommonField.DATA_JSON, jsObject)
        callback.apply(ret)
    }

    fun bluetoothDeviceFound(webView: WebView) {
        var dataDataJson = JSONObject()
        var devsJsonArr = JSONArray()
        for (dev in buleToothDevs) {
            devsJsonArr.add(dev)
        }
        dataDataJson.put(CommonField.DEVS, devsJsonArr)

        var callback = WebCallBack(webView)
        val jsObject = JSONObject()
        jsObject.put(CommonField.NAME, "bluetoothDeviceFound")
        jsObject.put(CommonField.PAYLOAD, dataDataJson)
        var ret = JSONObject()
        ret.put(CommonField.HANDLER_NAME, CommonField.EMIT_EVENT)
        ret.put(CommonField.DATA_JSON, jsObject)
        Log.e("XXX", "json " + ret.toJSONString())
        callback.apply(ret)
    }

    fun stopBluetoothDevicesDiscovery(webView: WebView, param: JSONObject, callback: WebCallBack) {
        var bluetoothmanger = webView.context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        var bluetoothadapter = bluetoothmanger.getAdapter()
        var callbackId = param.getString(CommonField.CALLBACK_ID)

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

        bluetoothadapter.bluetoothLeScanner.stopScan(scanCallback)
        scanCallback = null
        callback.apply(jsObject)
        bluetoothAdapterStateChange(webView)
    }

    fun getBluetoothDevices(webView: WebView, param: JSONObject, callback: WebCallBack) {
        var dataDataJson = JSONObject()
        var devsJsonArr = JSONArray.toJSON(buleToothDevs) as JSONArray
        dataDataJson.put(CommonField.DEVS, devsJsonArr)
        var callbackId = param.getString(CommonField.CALLBACK_ID)
        var jsObject = generateSuccessedData(callbackId, dataDataJson)
        Log.e("XXX", "js " + jsObject.toJSONString())
        callback.apply(jsObject)
    }

    fun getConnectedBluetoothDevices(webView: WebView, param: JSONObject, callback: WebCallBack) {
        var bluetoothmanger = webView.context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        var bluetoothadapter = bluetoothmanger.getAdapter()

        val devs = ArrayList<BuleToothDev>()
        var servicesJsonArr = param.getJSONArray(CommonField.SERVICES)
        var services = ArrayString()
        for (i in 0 until servicesJsonArr.size) {
            services.addValue(servicesJsonArr.getString(i))
        }

        for (dev in buleToothDevs) {
            var gatt = connectionHandler.get(dev.deviceId)
            if(dev.dev == null || dev.dev!!.bondState != BluetoothDevice.BOND_BONDED) continue

            for (k in 0 until services.size()) {
                var svs = gatt?.getService(UUID.fromString(services.getValue(k)))
                if (svs != null && svs.type == BluetoothGattService.SERVICE_TYPE_PRIMARY) {
                    var item = BuleToothDev()
                    item.name = dev.name
                    item.deviceId = dev.deviceId
                    devs.add(item)
                    break
                }
            }
        }

        var dataDataJson = JSONObject()
        var devsJsonArr = JSONArray.toJSON(devs) as JSONArray
        dataDataJson.put(CommonField.DEVS, devsJsonArr)
        var callbackId = param.getString(CommonField.CALLBACK_ID)
        var jsObject = generateSuccessedData(callbackId, dataDataJson)
        callback.apply(jsObject)
    }

    fun getBLEDeviceRSSI(webView: WebView, param: JSONObject, callback: WebCallBack) {
        var rssi = 0
        var devId = param.getString(CommonField.DEV_ID)
        var i = buleToothDevs.size - 1
        while(i >= 0) {
            if (buleToothDevs.get(i).deviceId == devId) {
                rssi = buleToothDevs.get(i).RSSI
                break
            }
            i--
        }
        var dataDataJson = JSONObject()
        dataDataJson.put(CommonField.RSSI, rssi)
        var callbackId = param.getString(CommonField.CALLBACK_ID)
        var jsObject = generateSuccessedData(callbackId, dataDataJson)
        callback.apply(jsObject)
    }


    fun createBLEConnection(webView: WebView, param: JSONObject, callback: WebCallBack) {
        var devId = param.getString(CommonField.DEV_ID)
        var i = buleToothDevs.size - 1
        var bluetoothDevice: BluetoothDevice? = null
        while(i >= 0) {
            if (buleToothDevs.get(i).deviceId == devId) {
                bluetoothDevice = buleToothDevs.get(i).dev
                break
            }
            i--
        }

        var ret = JSONObject()
        var callbackId = param.getString(CommonField.CALLBACK_ID)
        if (bluetoothDevice == null) {
            ret = generateFailedData(callbackId, 10001, "")
            callback.apply(ret)
            return
        }

        bluetoothDevice.connectGatt(webView.context, false,
            object: BluetoothGattCallback() {
                override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
                    if (newState == BluetoothProfile.STATE_CONNECTED) {
                        if (gatt!!.discoverServices()) {
                            bleConnectionStateChange(webView, devId, true)
                        } else {
                            if (newState == BluetoothProfile.STATE_DISCONNECTED) gatt?.connect()
                            bleConnectionStateChange(webView, devId, false)
                        }

                    } else {
                        bleConnectionStateChange(webView, devId, false)
                    }
                }

                override fun onCharacteristicChanged(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?) {
                    bleCharacteristicValueChange(webView, gatt, characteristic)
                }

                override fun onCharacteristicWrite(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?, status: Int) {
                }

                override fun onCharacteristicRead(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?, status: Int) {
                    bleCharacteristicValueChange(webView, gatt, characteristic)
                }

                override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
                    connectionHandler.put(devId, gatt!!)
                }

                override fun onMtuChanged(gatt: BluetoothGatt?, mtu: Int, status: Int) {
                }

                override fun onDescriptorWrite(gatt: BluetoothGatt?, descriptor: BluetoothGattDescriptor?, status: Int) {
                    if (status == BluetoothGatt.GATT_SUCCESS) {
                        //开启监听成功，可以向设备写入命令了
                    }

                }
            })
        ret = generateSuccessedData(callbackId)
        callback.apply(ret)
    }

    fun bleCharacteristicValueChange(webView: WebView, gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?) {
        if (characteristic == null || gatt == null) return

        var callback = WebCallBack(webView)
        val jsObject = JSONObject()
        jsObject.put(CommonField.NAME, "bleCharacteristicValueChange")

        var dataDataJson = JSONObject()
        dataDataJson.put(CommonField.DEV_ID, gatt.device.address)
        for (service in gatt.services) {
            if (service.getCharacteristic(characteristic.uuid) != null) {
                dataDataJson.put(CommonField.SERVICE_ID, service.uuid.toString().toUpperCase())
                break
            }
        }

        dataDataJson.put(CommonField.CHARACTERISTIC_ID, characteristic.uuid.toString().toUpperCase())

        if (characteristic.value != null) {
            var values = JSONArray()
            for (byteEle in characteristic.value) {
                val hex = Integer.toHexString(0xFF and byteEle.toInt())
                if (hex.length < 2) {
                    values.add(("0" + hex).toUpperCase())
                } else {
                    values.add(hex.toUpperCase())
                }
            }
            dataDataJson.put("value", values)
        }
        jsObject.put(CommonField.PAYLOAD, dataDataJson)
        var ret = JSONObject()
        ret.put(CommonField.HANDLER_NAME, CommonField.EMIT_EVENT)
        ret.put(CommonField.DATA_JSON, jsObject)
        callback.apply(ret)
    }

    fun bleConnectionStateChange(webView: WebView, devId: String, connected: Boolean) {
        var dataDataJson = JSONObject()
        dataDataJson.put(CommonField.DEV_ID, devId)
        dataDataJson.put(CommonField.CONNECTED, connected)

        var callback = WebCallBack(webView)
        val jsObject = JSONObject()
        jsObject.put(CommonField.NAME, "bleConnectionStateChange")
        jsObject.put(CommonField.PAYLOAD, dataDataJson)
        var ret = JSONObject()
        ret.put(CommonField.HANDLER_NAME, CommonField.EMIT_EVENT)
        ret.put(CommonField.DATA_JSON, jsObject)
        callback.apply(ret)
    }

    fun closeBLEConnection(webView: WebView, param: JSONObject, callback: WebCallBack) {
        var devId = param.getString(CommonField.DEV_ID)
        var gatt = connectionHandler.get(devId)
        var ret = JSONObject()
        var callbackId = param.getString(CommonField.CALLBACK_ID)
        if (gatt == null) {
            ret = generateFailedData(callbackId, 10001, "")
            callback.apply(ret)
            return
        }

        gatt.disconnect()
        connectionHandler.remove(devId)
        ret = generateSuccessedData(callbackId)
        callback.apply(ret)
        return
    }

    fun getBLEDeviceServices(webView: WebView, param: JSONObject, callback: WebCallBack) {
        var devId = param.getString(CommonField.DEV_ID)
        var gatt = connectionHandler.get(devId)
        var ret = JSONObject()
        var callbackId = param.getString(CommonField.CALLBACK_ID)
        if (gatt == null) {
            ret = generateFailedData(callbackId, 10001, "")
            callback.apply(ret)
            return
        }

        var dataJson = JSONObject()
        dataJson.put(CommonField.DEV_ID, devId)
        var jsonArr = JSONArray()
        if (gatt.services != null) {
            for (service in gatt.services) {
                var eleJson = JSONObject()
                eleJson.put(CommonField.UUID, service.uuid?.toString()?.toUpperCase())
                eleJson.put("isPrimary", service.type == BluetoothGattService.SERVICE_TYPE_PRIMARY)
                jsonArr.add(eleJson)
            }
        }
        dataJson.put(CommonField.SERVICES, jsonArr)
        ret = generateSuccessedData(callbackId, dataJson)
        callback.apply(ret)
    }

    fun getBLEDeviceCharacteristics(webView: WebView, param: JSONObject, callback: WebCallBack) {
        var devId = param.getString(CommonField.DEV_ID)
        var serviceId = param.getString(CommonField.SERVICE_ID)
        var gatt = connectionHandler.get(devId)
        var ret = JSONObject()
        var callbackId = param.getString(CommonField.CALLBACK_ID)
        if (gatt == null) {
            ret = generateFailedData(callbackId, 10001, "")
            callback.apply(ret)
            return
        }

        var dataJson = JSONObject()
        dataJson.put(CommonField.DEV_ID, devId)
        dataJson.put(CommonField.SERVICE_ID, serviceId)
        var jsonArr = JSONArray()
        if (gatt.services != null) {
            var serviceTmp: BluetoothGattService? = null
            for (service in gatt.services) {
                if (service.uuid.toString().toUpperCase() == serviceId) {
                    serviceTmp = service
                    break
                }
            }

            if (serviceTmp != null && serviceTmp.characteristics != null) {
                for (char in serviceTmp.characteristics) {
                    var item = JSONObject()
                    item.put(CommonField.UUID, char.uuid.toString().toUpperCase())
                    var propertiesJson = JSONObject()
                    propertiesJson.put("notify", (char.properties and
                            BluetoothGattCharacteristic.PROPERTY_NOTIFY == BluetoothGattCharacteristic.PROPERTY_NOTIFY))
                    propertiesJson.put("write", (char.properties and
                            BluetoothGattCharacteristic.PROPERTY_WRITE == BluetoothGattCharacteristic.PROPERTY_WRITE))
                    propertiesJson.put("indicate", (char.properties and
                            BluetoothGattCharacteristic.PROPERTY_INDICATE == BluetoothGattCharacteristic.PROPERTY_INDICATE))
                    propertiesJson.put("read", (char.properties and
                            BluetoothGattCharacteristic.PROPERTY_READ == BluetoothGattCharacteristic.PROPERTY_READ))
                    item.put("properties", propertiesJson)
                    jsonArr.add(item)
                }
            }
        }
        dataJson.put("characteristics", jsonArr)
        ret = generateSuccessedData(callbackId, dataJson)
        callback.apply(ret)
    }

    fun readBLECharacteristicValue(webView: WebView, param: JSONObject, callback: WebCallBack) {
        var devId = param.getString(CommonField.DEV_ID)
        var serviceId = param.getString(CommonField.SERVICE_ID)
        var characteristicId = param.getString(CommonField.CHARACTERISTIC_ID)
        var gatt = connectionHandler.get(devId)
        var ret = JSONObject()
        var callbackId = param.getString(CommonField.CALLBACK_ID)
        if (gatt == null) {
            ret = generateFailedData(callbackId, 10001, "")
            callback.apply(ret)
            return
        }

        var serviceTmp = gatt.getService(UUID.fromString(serviceId))
        if (serviceTmp == null) {
            ret = generateFailedData(callbackId, 10001, "")
            callback.apply(ret)
            return
        }

        var charact = serviceTmp.getCharacteristic(UUID.fromString(characteristicId))
        if (charact == null) {
            ret = generateFailedData(callbackId, 10001, "")
            callback.apply(ret)
            return
        }

        if (gatt.readCharacteristic(charact)) {
            ret = generateSuccessedData(callbackId)
            callback.apply(ret)
        }
    }

    fun writeBLECharacteristicValue(webView: WebView, param: JSONObject, callback: WebCallBack) {
        var devId = param.getString(CommonField.DEV_ID)
        var serviceId = param.getString(CommonField.SERVICE_ID)
        var characteristicId = param.getString(CommonField.CHARACTERISTIC_ID)
        var value = param.getString("value")
        var gatt = connectionHandler.get(devId)
        var ret = JSONObject()
        var callbackId = param.getString(CommonField.CALLBACK_ID)
        if (gatt == null) {
            ret = generateFailedData(callbackId, 10001, "")
            callback.apply(ret)
            return
        }

        var serviceTmp = gatt.getService(UUID.fromString(serviceId))
        if (serviceTmp == null) {
            ret = generateFailedData(callbackId, 10001, "")
            callback.apply(ret)
            return
        }

        var charact = serviceTmp.getCharacteristic(UUID.fromString(characteristicId))
        if (charact == null) {
            ret = generateFailedData(callbackId, 10001, "")
            callback.apply(ret)
            return
        }
        charact.setValue(value)
        if (gatt.writeCharacteristic(charact)) {
            ret = generateSuccessedData(callbackId)
            callback.apply(ret)
        } else {
            ret = generateFailedData(callbackId, 10001, "")
            callback.apply(ret)
        }
    }

    fun notifyBLECharacteristicValueChange(webView: WebView, param: JSONObject, callback: WebCallBack) {
        var devId = param.getString(CommonField.DEV_ID)
        var serviceId = param.getString(CommonField.SERVICE_ID)
        var characteristicId = param.getString(CommonField.CHARACTERISTIC_ID)
        var state = param.getBoolean("state")
        var gatt = connectionHandler.get(devId)
        var ret = JSONObject()
        var callbackId = param.getString(CommonField.CALLBACK_ID)
        if (gatt == null) {
            ret = generateFailedData(callbackId, 10001, "")
            callback.apply(ret)
            return
        }

        var serviceTmp = gatt.getService(UUID.fromString(serviceId))
        if (serviceTmp == null) {
            ret = generateFailedData(callbackId, 10001, "")
            callback.apply(ret)
            return
        }

        var charact = serviceTmp.getCharacteristic(UUID.fromString(characteristicId))
        if (charact == null) {
            ret = generateFailedData(callbackId, 10001, "")
            callback.apply(ret)
            return
        }


        if (gatt.setCharacteristicNotification(charact, state)) {

            val descriptorList: List<BluetoothGattDescriptor> = charact.getDescriptors()
            if (descriptorList != null && descriptorList.size > 0) {
                for (descriptor in descriptorList) {
                    if (state) {
                        descriptor.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                    } else {
                        descriptor.value = BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE
                    }
                    gatt.writeDescriptor(descriptor)
                }
            }
            ret = generateSuccessedData(callbackId)
            callback.apply(ret)
        } else {
            ret = generateFailedData(callbackId, 10001, "")
            callback.apply(ret)
        }
    }

    fun setBLEMTU(webView: WebView, param: JSONObject, callback: WebCallBack) {
        var devId = param.getString(CommonField.DEV_ID)
        var mtu = param.getIntValue("mtu")
        var gatt = connectionHandler.get(devId)
        var ret = JSONObject()
        var callbackId = param.getString(CommonField.CALLBACK_ID)
        if (gatt == null) {
            ret = generateFailedData(callbackId, 10001, "")
            callback.apply(ret)
            return
        }

        if (gatt.requestMtu(mtu)) {
            ret = generateSuccessedData(callbackId)
            callback.apply(ret)
        } else {
            ret = generateFailedData(callbackId, 10001, "")
            callback.apply(ret)
        }
    }

    //注册广播接收器，用于监听蓝牙状态变化
    fun registerBlueToothStateReceiver(context: Context, webView: WebView) {
        //注册广播，蓝牙状态监听
        var blueToothStateReceiver = BlueToothStateReceiver()
        val filter = IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED)
        context.registerReceiver(blueToothStateReceiver, filter)
        blueToothStateReceiver.setOnBlueToothStateListener(object : OnBlueToothStateListener {
            override fun onStateOff() { bluetoothAdapterStateChange(webView) }
            override fun onStateOn() { bluetoothAdapterStateChange(webView) }
            override fun onfinishDiscovery() { bluetoothAdapterStateChange(webView) }
            override fun onStartDiscovery() { bluetoothAdapterStateChange(webView) }
            override fun onStateTurningOn() {}
            override fun onStateTurningOff() {}
        })
    }
}