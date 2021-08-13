package com.tencent.iot.explorer.link.mvp.model

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.content.Context
import android.location.Location
import android.text.TextUtils
import android.util.Log
import com.alibaba.fastjson.JSON
import com.espressif.iot.esptouch.IEsptouchResult
import com.tencent.iot.explorer.link.App
import com.tencent.iot.explorer.link.R
import com.tencent.iot.explorer.link.core.auth.http.ConnectionListener
import com.tencent.iot.explorer.link.core.auth.http.Reconnect
import com.tencent.iot.explorer.link.core.auth.response.DeviceBindTokenStateResponse
import com.tencent.iot.explorer.link.core.link.service.SmartConfigService
import com.tencent.iot.explorer.link.core.link.service.SoftAPService
import com.tencent.iot.explorer.link.core.link.entity.*
import com.tencent.iot.explorer.link.core.link.exception.TCLinkException
import com.tencent.iot.explorer.link.core.link.listener.SmartConfigListener
import com.tencent.iot.explorer.link.core.link.listener.SoftAPListener
import com.tencent.iot.explorer.link.core.log.L
import com.tencent.iot.explorer.link.kitlink.util.*
import com.tencent.iot.explorer.link.mvp.ParentModel
import com.tencent.iot.explorer.link.mvp.view.ConnectView
import com.tencent.iot.explorer.link.T
import com.tencent.iot.explorer.link.core.auth.callback.MyCallback
import com.tencent.iot.explorer.link.core.auth.response.BaseResponse
import com.tencent.iot.explorer.link.core.link.listener.BleDeviceConnectionListener
import com.tencent.iot.explorer.link.core.link.service.BleConfigService
import com.tencent.iot.explorer.link.kitlink.entity.ConfigType
import kotlinx.coroutines.*
import java.lang.Runnable

/**
 * 配网进度、绑定设备
 */
class ConnectModel(view: ConnectView) : ParentModel<ConnectView>(view), MyCallback, CoroutineScope by MainScope(){

    private val TAG = this.javaClass.simpleName
    private val maxTime = 100   // 执行最大时间间隔
    private val interval = 2    // 每次执行的时间间隔
    private val unKnowError = 99  // 每次执行的时间间隔

    var smartConfig: SmartConfigService? = null
    var softAP: SoftAPService? = null
    var ssid = ""
    var bssid = ""
    var password = ""
    @Volatile
    var checkDeviceBindTokenStateStarted = false
    var deviceInfo: DeviceInfo? = null
    var type = ConfigType.SmartConfig.id
    @Volatile
    var bluetoothGatt: BluetoothGatt? = null
    @Volatile
    var job: Job? = null
    var bleDevice: BleDevice? = null

    fun initService(type: Int, context: Context) {
        this.type = type
        if (type == ConfigType.SmartConfig.id) {
            smartConfig = SmartConfigService(context.applicationContext)
        } else if (type == ConfigType.SoftAp.id) {
            softAP = SoftAPService(context.applicationContext)
        } else {} // 蓝牙服务时单例，在 app 中已经初始化过，无需再处理
    }

    /**
     *  智能配网监听
     */
    private val smartConfigListener = object : SmartConfigListener {
        override fun onSuccess(deviceInfo: DeviceInfo) {
            this@ConnectModel.deviceInfo = deviceInfo
            this.onStep(SmartConfigStep.STEP_DEVICE_BOUND)
            checkDeviceBindTokenState()
        }

        override fun deviceConnectToWifi(result: IEsptouchResult) {}

        override fun onStep(step: SmartConfigStep) {
            view.connectStep(step.ordinal)
        }

        override fun deviceConnectToWifiFail() {
            view.deviceConnectToWifiFail()
        }

        override fun onFail(exception: TCLinkException) {
            view.connectFail(exception.errorCode, exception.errorMessage)
        }
    }

    fun startBleConfigNet() {
        // 先关闭当前的扫描
        BleConfigService.get().stopScanBluetoothDevices()
        bleDevice?:let { // 没有解析出内容，直接返回
            bleFailed("param error")
            return
        }

        deviceInfo = DeviceInfo(bleDevice?.productId, bleDevice?.devName)
        BleConfigService.get().connetionListener = getBleListener(bleDevice!!)

        // 快速连接，避免多次的扫描设备带来的耗时
        bleDevice?.blueDev?.let {
            this@ConnectModel.view?.connectStep(BleConfigStep.STEP_CONNECT_BLE_DEV.ordinal)
            bluetoothGatt = BleConfigService.get().connectBleDevice(bleDevice)
            L.d(TAG, "do not need scan device ${bluetoothGatt}")
        }

        // 无法快速连接的，走全流程连接
        bleDevice?.blueDev?:let {
            var ret = BleConfigService.get().startScanBluetoothDevices()
            L.d(TAG, "startScanBluetoothDevices $ret")
        }

        Thread {
            job = GlobalScope.launch(Dispatchers.Default) {
                delay(240000) // 超时
                bleFailed("time out")
            }
        }.start()
    }

    private fun bleFailed(msg: String?) {
        launch (Dispatchers.Main){
            job?.cancel()  // 结束超时任务
            BleConfigService.get().connetionListener = null
            L.e(TAG, msg?:"")
            view?.connectFail("bind bluetooth device", msg?:"")
            bluetoothGatt?.close()
        }
    }

    private fun getBleListener(dev: BleDevice): BleDeviceConnectionListener {
        return object: BleDeviceConnectionListener {
            override fun onBleDeviceFounded(bleDevice: BleDevice) {
                L.d(TAG, "onBleDeviceFounded ${bleDevice.devName} ${bleDevice.productId} ${bleDevice.indexWithDevname}")
                L.d(TAG, "tagBleDev ${dev.devName} ${dev.productId} ${dev.indexWithDevname}")

                if (dev == bleDevice) {
                    launch {
                        delay(2000)
                        BleConfigService.get().stopScanBluetoothDevices()
                        this@ConnectModel.view?.connectStep(BleConfigStep.STEP_CONNECT_BLE_DEV.ordinal)
                        bluetoothGatt = BleConfigService.get().connectBleDevice(bleDevice)
                        L.d(TAG, "bluetoothGatt ${bluetoothGatt}")
                    }
                }
            }
            override fun onBleDeviceDisconnected(exception: TCLinkException) {
                bleFailed(exception.errorMessage)
            }
            override fun onBleDeviceInfo(bleDeviceInfo: BleDeviceInfo) {}

            override fun onBleDeviceConnected() {
                L.d(TAG, "onBleDeviceConnected")
                launch {
                    delay(2000)
                    bluetoothGatt?.let {
                        var mtuRet = BleConfigService.get().setMtuSize(it, App.data.bindDeviceToken.length * 2 + 20)
                        L.d(TAG, "mtuRet ${mtuRet}")
                    }
                    delay(1000)
                    bluetoothGatt?.let {
                        if (BleConfigService.get().setWifiMode(it, BleDeviceWifiMode.STA)) {
                            return@launch // 设置成功则直接退出
                        }
                    }
                    bleFailed("connect ble dev failed")
                }
            }

            override fun onBleSetWifiModeResult(success: Boolean) {
                L.d(TAG, "onBleSetWifiModeResult ${success}")
                if (!success) {
                    bleFailed("set wifi mode failed")
                    return
                }

                launch {
                    delay(1000)
                    bluetoothGatt?.let {
                        var bleDeviceWifiInfo = BleDeviceWifiInfo(ssid, password)
                        L.d(TAG, "bleDeviceWifiInfo ${JSON.toJSONString(bleDeviceWifiInfo)}")
                        this@ConnectModel.view?.connectStep(BleConfigStep.STEP_SEND_WIFI_INFO.ordinal)
                        if (BleConfigService.get().sendWifiInfo(it, bleDeviceWifiInfo)) return@launch
                        bleFailed("send wifi info failed")
                    }
                }
            }

            override fun onBleSendWifiInfoResult(success: Boolean) {
                L.d(TAG, "onBleSendWifiInfoResult ${success}")
                if (!success) {
                    bleFailed("send wifi info failed")
                    return
                }

                launch {
                    delay(1000)
                    bluetoothGatt?.let {
                        if (BleConfigService.get().requestConnectWifi(it)) return@launch
                        bleFailed("connect wifi failed")
                    }
                }
            }

            override fun onBleWifiConnectedInfo(wifiConnectInfo: BleWifiConnectInfo) {
                L.d(TAG, "onBleWifiConnectedInfo ${wifiConnectInfo.connected}")
                if (!wifiConnectInfo.connected) {
                    bleFailed("connect wifi failed")
                    return
                }

                launch {
                    delay(1000)
                    bluetoothGatt?.let {
                        this@ConnectModel.view?.connectStep(BleConfigStep.STEP_SEND_TOKEN.ordinal)
                        //L.d(TAG, "send token ${App.data.bindDeviceToken}")
                        if (BleConfigService.get().configToken(it, App.data.bindDeviceToken)) return@launch
                        bleFailed("send token failed")
                    }
                }
            }

            override fun onBlePushTokenResult(success: Boolean) {
                L.d(TAG, "onBlePushTokenResult ${success}")
                if (success) {
                    checkDeviceBindTokenState()
                } else {
                    bleFailed("send wifi info failed")
                }
            }

        }
    }

    /**
     * 开始智能配网
     */
    fun startSmartConnect() {
        smartConfig?.let {
            val location = Location("temp")
            location.longitude = 0.0
            location.latitude = 0.0
            val task = LinkTask()
            task.mSsid = ssid
            task.mBssid = bssid
            task.mPassword = password
            task.mAccessToken = App.data.bindDeviceToken
            task.mLocation = location
            L.e("ssid:$ssid")
            it.startConnect(task, smartConfigListener)
        }
        deviceInfo = null
    }

    private val connectionListener = object : ConnectionListener {
        override fun onConnected() {
            checkDeviceBindTokenState()
        }
    }

    private val softAPListener = object : SoftAPListener {
        override fun onSuccess(deviceInfo: DeviceInfo) {
            this@ConnectModel.deviceInfo = deviceInfo
            this.onStep(SoftAPStep.STEP_DEVICE_BOUND)
            L.e("开始绑定设备")
        }

        override fun reconnectedSuccess(deviceInfo: DeviceInfo) {
            Reconnect.instance.start(connectionListener)
        }

        override fun reconnectedFail(deviceInfo: DeviceInfo, ssid: String) {
            view.softApConnectToWifiFail(ssid)
            Reconnect.instance.start(connectionListener)
        }

        override fun onStep(step: SoftAPStep) {
            view.connectStep(step.ordinal)
        }

        override fun onFail(code: String, msg: String) {
            view.connectFail(code, msg)
        }
    }

    /**
     * 开始自助配网
     */
    fun startSoftAppConnect() {

        softAP?.let {
            val location = Location("temp")
            location.longitude = 0.0
            location.latitude = 0.0
            val task = LinkTask()
            task.mSsid = ssid
            task.mBssid = bssid
            task.mPassword = password
            task.mAccessToken = App.data.bindDeviceToken
            task.mLocation = location
            task.mRegion = App.data.region
            L.d("ssid:$ssid, password:$password")
            it.startConnect(task, softAPListener)
        }
        deviceInfo = null
    }

    private fun checkDeviceBindTokenState() {
        if (checkDeviceBindTokenStateStarted) return

        var maxTimes2Try = maxTime / interval
        var currentNo = 0

        // 开启线程做网络请求
        Thread(Runnable {
            checkDeviceBindTokenState(currentNo, maxTimes2Try, interval)
        }).start()
        checkDeviceBindTokenStateStarted = true
    }

    private fun checkDeviceBindTokenState(currentNo: Int, maxTimes: Int, interval: Int) {
        // 结束递归的条件，避免失败后的无限递归
        if (currentNo >= maxTimes || currentNo < 0) {
            Reconnect.instance.stop(connectionListener)
            checkDeviceBindTokenStateStarted = false
            softAPListener.onFail(unKnowError.toString(), T.getContext().getString(R.string.get_bind_state_failed)) //"获取设备与 token 的绑定状态失败"
            return
        }

        val nextNo = currentNo + 1
        HttpRequest.instance.checkDeviceBindTokenState(object: MyCallback{
            override fun fail(msg: String?, reqCode: Int) {
                // 失败进行到下一次的递归
                Thread.sleep(interval.toLong() * 1000)
                checkDeviceBindTokenState(nextNo, maxTimes, interval)
            }

            override fun success(response: BaseResponse, reqCode: Int) {
                // 只要有一次成功，就回调成功
                response.parse(DeviceBindTokenStateResponse::class.java)?.State.let {
                    when(it) {
                        2 -> {
                            Reconnect.instance.stop(connectionListener)
                            checkDeviceBindTokenStateStarted = false
                            Thread(Runnable {
                                wifiBindDevice(deviceInfo!!)
                            }).start()
                        } else -> {
                            // 主线程回调，子线程开启新的网络请求，避免阻塞主线程
                            Thread(Runnable{
                                Thread.sleep(interval.toLong() * 1000)
                                checkDeviceBindTokenState(nextNo, maxTimes, interval)
                            }).start()
                        }
                    }
                }
            }
        })
    }

    /**
     *  绑定设备
     */
    private fun wifiBindDevice(data: DeviceInfo) {
        HttpRequest.instance.wifiBindDevice(App.data.getCurrentFamily().FamilyId, data, this)
    }

    override fun fail(msg: String?, reqCode: Int) {
        L.e(msg ?: "")
        softAPListener.onFail(unKnowError.toString(), T.getContext().getString(R.string.get_family_bind_state_failed)) //"获取家庭与设备绑定关系失败"
    }

    override fun success(response: BaseResponse, reqCode: Int) {
        if (response.isSuccess()) {
            when(type) {
                ConfigType.SmartConfig.id -> smartConfigListener.onStep(SmartConfigStep.STEP_LINK_SUCCESS)
                ConfigType.SoftAp.id -> softAPListener.onStep(SoftAPStep.STEP_LINK_SUCCESS)
                ConfigType.BleConfig.id -> {
                    this@ConnectModel.view?.connectStep(BleConfigStep.STEP_LINK_SUCCESS.ordinal)
                    launch(Dispatchers.Main) {  bluetoothGatt?.close() }
                }
            }
            view?.connectSuccess()
        } else {
            view?.connectFail("bind_fail", response.msg)
        }
        //绑定操作响应后，不论结果如何，一律停止监听。
        Reconnect.instance.stop(connectionListener)
    }

    override fun onDestroy() {
        smartConfig?.stopConnect()
        softAP?.stopConnect()
        cancel()
        bluetoothGatt?.close()
        Reconnect.instance.stop(connectionListener)
        L.e("停止配网")
        super.onDestroy()
    }

}