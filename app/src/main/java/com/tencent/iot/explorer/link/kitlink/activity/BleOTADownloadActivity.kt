package com.tencent.iot.explorer.link.kitlink.activity

import android.os.Handler
import android.os.Message
import android.text.TextUtils
import android.view.View
import com.alibaba.fastjson.JSONObject
import com.tencent.iot.explorer.link.R
import com.tencent.iot.explorer.link.core.auth.IoTAuth
import com.tencent.iot.explorer.link.core.auth.callback.MyCallback
import com.tencent.iot.explorer.link.core.auth.entity.DeviceEntity
import com.tencent.iot.explorer.link.core.auth.response.BaseResponse
import com.tencent.iot.explorer.link.core.link.entity.*
import com.tencent.iot.explorer.link.core.link.exception.TCLinkException
import com.tencent.iot.explorer.link.core.link.listener.BleDeviceConnectionListener
import com.tencent.iot.explorer.link.core.link.service.BleConfigService
import com.tencent.iot.explorer.link.core.log.L
import com.tencent.iot.explorer.link.core.utils.CRC32
import com.tencent.iot.explorer.link.mvp.IPresenter
import com.tencent.iot.explorer.link.retrofit.DownloadRequest
import com.tencent.iot.explorer.link.retrofit.DownloadRequest.OnDownloadListener
import kotlinx.android.synthetic.main.activity_ble_otadownload.*
import kotlinx.android.synthetic.main.activity_ble_otadownload.ble_connect_layout
import kotlinx.android.synthetic.main.activity_ble_otadownload.retry_connect
import kotlinx.android.synthetic.main.activity_ble_otadownload.search_ble_dev_layout
import kotlinx.android.synthetic.main.activity_ble_otadownload.search_reault_layout
import kotlinx.android.synthetic.main.menu_back_layout.*
import kotlinx.coroutines.*
import java.io.*


class BleOTADownloadActivity : PActivity(), CoroutineScope by MainScope() {
    val MSG_REFRESH = 0
    val MSG_DOWNLOAD_SUCCESS = 1
    val MSG_DOWNLOAD_FAILED = 2

    private var TAG = BleOTADownloadActivity::class.java.simpleName
    private var deviceEntity: DeviceEntity? = null
    private var connectBleJob: Job? = null

    private var targetVersion: String? = null // 要升级到的ota版本
    @Volatile
    private var count: Int = 0// 进度值
    private var otaFilePath: String? = null // 下载固件时本地的存储路径

    override fun getPresenter(): IPresenter? {
        return null
    }

    override fun onResume() {
        super.onResume()
        BleConfigService.get().connetionListener = bleDeviceConnectionListener
    }

    override fun getContentView(): Int {
        return R.layout.activity_ble_otadownload
    }

    override fun initView() {
        tv_title.text = getString(R.string.firmware_update)
        stopScanBleDev(true)
        deviceEntity = get("device")
        deviceEntity?.run {
            getDeviceType(ProductId, object: OnTypeGeted {
                override fun onType(type: String) {
                    if (type == "ble") {
                        launch (Dispatchers.Main) {
                            startScanBleDev()
                        }
                    }
                }
            })
        }
        downloadDeviceOTAInfo()
    }

    override fun setListener() {

    }


    private fun startScanBleDev() {
        ble_connect_layout.visibility = View.VISIBLE
        BleConfigService.get().startScanBluetoothDevices()
        search_ble_dev_layout.visibility = View.VISIBLE
        search_reault_layout.visibility = View.GONE
    }

    private fun stopScanBleDev(connected: Boolean) {
        launch(Dispatchers.Main) {
            ble_connect_layout.visibility = View.VISIBLE
            BleConfigService.get().stopScanBluetoothDevices()
            search_ble_dev_layout.visibility = View.GONE
            search_reault_layout.visibility = View.VISIBLE
            if (connected) {
                search_reault_layout.setBackgroundResource(R.color.blue_006EFF)
                retry_connect.setTextColor(this@BleOTADownloadActivity.resources.getColor(R.color.white))
                retry_connect.setText(R.string.break_ble_connect)
                retry_connect.setOnClickListener {
                    BleConfigService.get().bluetoothGatt?.let {
                        it?.close()
                        stopScanBleDev(false)
                    }
                }
            } else {
                search_reault_layout.setBackgroundResource(R.color.red_E65A59)
                retry_connect.setTextColor(this@BleOTADownloadActivity.resources.getColor(R.color.white))
                retry_connect.setText(R.string.scanning_retry)
                retry_connect.setOnClickListener { startScanBleDev() }
            }
        }
    }

    private var bleDeviceConnectionListener = object: BleDeviceConnectionListener {
        override fun onBleDeviceFounded(bleDevice: BleDevice) {
            if (bleDevice.productId == deviceEntity?.ProductId && !TextUtils.isEmpty(bleDevice.productId)) {
                //&& bleDevice.devName == deviceEntity?.DeviceName) {
                BleConfigService.get().bluetoothGatt = BleConfigService.get().connectBleDeviceAndGetLocalPsk(bleDevice, deviceEntity?.ProductId, deviceEntity?.DeviceName)
            } else if (!TextUtils.isEmpty(bleDevice.bindTag)) {
                deviceEntity?.let {
                    if (bleDevice.bindTag == BleConfigService.bytesToHex(BleConfigService.getBindTag(it.ProductId, it.DeviceName))) {
                        BleConfigService.get().bluetoothGatt = BleConfigService.get().connectBleDeviceAndGetLocalPsk(bleDevice, deviceEntity?.ProductId, deviceEntity?.DeviceName)
                    }
                }
            }
        }

        override fun onBleDeviceConnected() {
            launch {
                BleConfigService.get().bluetoothGatt?.let {
                    delay(3000)
//                    if (BleConfigService.get().setMtuSize(it, 512)) return@launch
                    launch (Dispatchers.Main) {
                        delay(1000)
                        BleConfigService.get().bluetoothGatt?.let {
                            BleConfigService.get().stopScanBluetoothDevices()
                            if (!BleConfigService.get().connectSubDevice(it)) {
                                stopScanBleDev(false)
                                return@launch
                            } else {
                                connectBleJob = launch (Dispatchers.Main) {
                                    delay(10000)
                                    stopScanBleDev(false)
                                }
                            }
                        }
                    }
                    stopScanBleDev(false)
                }
            }
        }
        override fun onBleDeviceDisconnected(exception: TCLinkException) {

        }
        override fun onBleDeviceInfo(bleDeviceInfo: BleDeviceInfo) {}
        override fun onBleSetWifiModeResult(success: Boolean) {}
        override fun onBleSendWifiInfoResult(success: Boolean) {}
        override fun onBleWifiConnectedInfo(wifiConnectInfo: BleWifiConnectInfo) {}
        override fun onBlePushTokenResult(success: Boolean) {}
        override fun onMtuChanged(mtu: Int, status: Int) {
            L.d(TAG, "onMtuChanged mtu $mtu status $status")
        }
        override fun onBleBindSignInfo(bleDevBindCondition: BleDevBindCondition) {}
        override fun onBleSendSignInfo(bleDevSignResult: BleDevSignResult) {
            stopScanBleDev(true)
            connectBleJob?.cancel()
        }
        override fun onBleUnbindSignInfo(signInfo: String) {}
        override fun onBlePropertyValue(bleDeviceProperty: BleDeviceProperty) {
            L.d(TAG, "onBlePropertyValue $bleDeviceProperty ")
        }
        override fun onBleControlPropertyResult(result: Int) {}
        override fun onBleRequestCurrentProperty() {}
        override fun onBleNeedPushProperty(eventId: Int, bleDeviceProperty: BleDeviceProperty) {}
        override fun onBleReportActionResult(reason: Int, actionId: Int, bleDeviceProperty: BleDeviceProperty) {}
        override fun onBleDeviceFirmwareVersion(firmwareVersion: BleDeviceFirmwareVersion) {
            if (firmwareVersion.mtuFlag == 1) { // 是否设置 mtu 当 mtu flag为 1 时，进行 MTU 设置；当 mtu flag 为 0 时，不设置 MTU
                BleConfigService.get().setMtuSize(BleConfigService.get().bluetoothGatt, firmwareVersion.mtuSize)
            }
        }
        override fun onBleDeviceMtuSize(size: Int) {}
        override fun onBleDeviceTimeOut(timeLong: Int) {}
    }

    private fun downloadDeviceOTAInfo() {
        IoTAuth.deviceImpl.getDeviceOTAInfo("${deviceEntity?.ProductId}/${deviceEntity?.DeviceName}", object: MyCallback {
            override fun fail(msg: String?, reqCode: Int) {

            }

            override fun success(response: BaseResponse, reqCode: Int) {
                val json = response.data as JSONObject
                val firmwareURL = json.getString("FirmwareURL")
                targetVersion = json.getString("TargetVersion")
//                val uploadVersion = json.getString("UploadVersion")
                if (TextUtils.isEmpty(otaFilePath)) {
                    otaFilePath = cacheDir.absolutePath + "/${deviceEntity?.ProductId}_${deviceEntity?.DeviceName}_${targetVersion}"
                }
                downloadOtaFirmware(firmwareURL)
            }
        })
    }

    private fun downloadOtaFirmware(url: String) {
        DownloadRequest.get().download(url, otaFilePath, downloadlistener)
    }


    var downloadlistener: OnDownloadListener = object : OnDownloadListener {
        override fun onDownloadSuccess(requestId: String) {
            handler.sendEmptyMessage(MSG_DOWNLOAD_SUCCESS)
        }

        override fun onDownloading(requestId: String, progress: Int) {
            count = progress
            refreshProgress()
        }

        override fun onDownloadFailed(requestId: String) {
            handler.sendEmptyMessage(MSG_DOWNLOAD_FAILED)
        }
    }


    var handler: Handler = object : Handler() {
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            if (msg.what == MSG_REFRESH) {
                if (progress_download != null) {
                    progress_download.progress = count.toFloat()
                    tv_upgradeing.text = getString(R.string.firmware_upgrading_tip, count) + "%"
                }
                if (count < 100) {
                    L.d(TAG, "count:${count}, progressBar.max:${progress_download?.max}")
//                    onDismisListener.onDownloadProgress(count, progressBar.max as Int)
                }
            } else if (msg.what == MSG_DOWNLOAD_SUCCESS) {
                progress_download?.progress = 100f
                progress_download?.labelText = getString(R.string.download_success)
                L.d(TAG, "onDownloadSuccess:${otaFilePath}")
//                onDismisListener.onDownloadSuccess(otaFilePath)
//                dismiss()
                otaFilePath?.let {
                    targetVersion?.let { it1 ->
                        BleConfigService.get().requestOTAInfo(BleConfigService.get().bluetoothGatt,
                            it, it1
                        )
                    }
                }
            } else if (msg.what == MSG_DOWNLOAD_FAILED) {
                L.d(TAG, "onDownloadFailed:${otaFilePath}")
//                onDismisListener.onDownloadFailed()
//                dismiss()
            }
        }
    }

    private fun refreshProgress() {
        // 进度在 100 以内允许刷新
        if (count <= 100) {
            handler.sendEmptyMessage(MSG_REFRESH)
        }
    }

}