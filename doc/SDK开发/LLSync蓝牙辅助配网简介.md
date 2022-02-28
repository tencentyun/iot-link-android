# LLSync蓝牙辅助配网简介

LLSync蓝牙辅助配网主要用于通过 BLE 给同时具有 BLE + Wi-Fi 能力的设备配置网络，通过 BLE 创建指定的 GATT服务，手机连接该 GATT SERVER，利用 BLE 的无线通信能力，将物联网设备连接所需的 SSID、PASSWORD等信息传输给设备端，使设备顺利接入物联网平台，继而完成设备绑定等功能，请参考 [LLSync 协议](https://github.com/tencentyun/qcloud-iot-explorer-BLE-sdk-embedded/blob/master/docs/LLSync%E8%93%9D%E7%89%99%E8%AE%BE%E5%A4%87%E6%8E%A5%E5%85%A5%E5%8D%8F%E8%AE%AE.pdf) 第七章节蓝牙辅助配网 。本文主要描述应用端App如何使用LLSync蓝牙辅助配网功能。

## 登陆物联网开发平台, 创建产品及设备。

创建产品时，选择通信方式为 Wi-Fi + BLE 。
创建产品完成后，需设置配网引导，在 交互开发 -> 配置APP -> 配网引导 进行配置。 芯片方案选择 请设置成 其他， 首选配网方式 请设置成 标准BLE辅助 ，设置好后点击保存。
请在设备调试中，创建好设备，在设备 SDK 中烧录上创建好的设备信息，开始发送LLSync蓝牙辅助配网的广播包，进行配网。

## 使用应用端APP，对设备进行配网操作。

1、开始扫描 BLE设备 广播包

```
    /**
     *  设置蓝牙模块回调
     **/
    BleConfigService.get().connetionListener = object: BleDeviceConnectionListener{
        /**
         *  扫描到发送LLSync对应广播包的设备 回调
         *  @param bleDevice 扫描到发送LLSync对应广播包的设备
         **/
        override fun onBleDeviceFounded(bleDevice: BleDevice){}
    }
    /**
     *  开始扫描 BLE设备 广播包
     **/
    BleConfigService.get().startScanBluetoothDevices()
```

2、停止扫描，开始连接要配网的 BleDevice 设备

```
    /**
     *  设置蓝牙模块回调
     **/
    BleConfigService.get().connetionListener = object: BleDeviceConnectionListener{
        /**
         *  成功连接要配网的 BleDevice 设备回调
         **/
        override fun onBleDeviceConnected() {}
        /**
         *  连接要配网的 BleDevice 设备失败，或已连接的 BleDevice 设备断开连接 的回调
         *  @param exception 失败异常原因
         **/
        override fun onBleDeviceDisconnected(exception: TCLinkException) {}
    }
    /**
     *  停止扫描LLSync对应广播包的设备
     **/
    BleConfigService.get().stopScanBluetoothDevices();
    /**
     *  开始连接要配网的 BleDevice 设备
     *  @param bleDevice 要配网的 BleDevice 设备
     **/
    BleConfigService.get().connectBleDevice(bleDevice)
```

3、设置 已连接的 BleDevice 设备 的 MTU

```
    /**
     *  设置蓝牙模块回调
     **/
    BleConfigService.get().connetionListener = object: BleDeviceConnectionListener{
        /**
         *  设置 已连接的 BleDevice 设备 的 MTU 回调
         *  @param bleDevice mtu – App与设备通信的最新 MTU 大小
         *  @param bleDevice status – 设置 已连接的 BleDevice 设备 的 MTU状态， BluetoothGatt.GATT_SUCCESS if the MTU has been changed successfully
         **/
        override fun onMtuChanged(mtu: Int, status: Int) {}
    }
    /**
     *  设置 已连接的 BleDevice 设备 的 MTU
     *  @param size MTU大小
     **/
    BleConfigService.get().setMtuSize(it, size)
```

4、设备信息获取

```
    /**
     *  设置蓝牙模块回调
     **/
    BleConfigService.get().connetionListener = object: BleDeviceConnectionListener{
        /**
         *  设备信息获取 回调
         *  @param bleDeviceInfo 设备信息
         **/
        override fun onBleDeviceInfo(bleDeviceInfo: BleDeviceInfo) {}
    }
    /**
     *  设备信息获取
     **/
    BleConfigService.get().requestDevInfo(it)
```

5、WIFI 模式设置

```
    /**
     *  设置蓝牙模块回调
     **/
    BleConfigService.get().connetionListener = object: BleDeviceConnectionListener{
        /**
         *  WIFI 模式设置 回调
         *  @param success 设置结果 true 设置成功 
         **/
        override fun onBleSetWifiModeResult(success: Boolean) {}
    }
    /**
     *  WIFI 模式设置
     *  @param BleDeviceWifiMode.STA 当前仅支持 STA 模式
     **/
    BleConfigService.get().setWifiMode(it, BleDeviceWifiMode.STA)
```

6、WIFI 信息下发，把 目标WiFi信息 通过蓝牙发送给设备，让设备准备好连接目标WiFi

```
    /**
     *  设置蓝牙模块回调
     **/
    BleConfigService.get().connetionListener = object: BleDeviceConnectionListener{
        /**
         *  WIFI 信息下发 回调
         *  @param success 下发结果 true 下发成功 
         **/
        override fun onBleSendWifiInfoResult(success: Boolean) {}
    }
    /**
     *  WIFI 信息下发
     *  @param bleDeviceWifiInfo 目标WiFi实例 配置好目标WiFi的ssid和pwd密钥
     **/
    BleConfigService.get().sendWifiInfo(it, bleDeviceWifiInfo)
```

7、请求设备 连接 目标WIFI

```
    /**
     *  设置蓝牙模块回调
     **/
    BleConfigService.get().connetionListener = object: BleDeviceConnectionListener{
        /**
         *  请求设备 连接 目标WIFI 回调
         *  @param wifiConnectInfo 连接 目标WIFI 结果，wifiConnectInfo.wifiMode 当前仅支持 STA  ,wifiConnectInfo.connected 表示 WIFI 是否连接 , wifiConnectInfo.ssid 连接 目标WIFI的 ssid
         **/
        override fun onBleWifiConnectedInfo(wifiConnectInfo: BleWifiConnectInfo) {}
    }
    /**
     *  请求设备 连接 目标WIFI
     **/
    BleConfigService.get().requestConnectWifi(it)
```

8、向设备下发 Token

此处的 Token 需要提前请求 [AppCreateDeviceBindToken](https://cloud.tencent.com/document/product/1081/44044) 进行获取

```
    /**
     *  设置蓝牙模块回调
     **/
    BleConfigService.get().connetionListener = object: BleDeviceConnectionListener{
        /**
         *  向设备下发 Token
         *  @param success 下发结果 true 下发成功
         **/
        override fun onBlePushTokenResult(success: Boolean) {}
    }
    /**
     *  生成 Wi-Fi 设备配网 Token
     **/
    HttpRequest.instance.getBindDevToken(callback)
    /**
     *  向设备下发 Token
     **/
    BleConfigService.get().configToken(it, App.data.bindDeviceToken)

```

9、轮训设备绑定 Token 结果 [AppGetDeviceBindTokenState](https://cloud.tencent.com/document/product/1081/44045)

```
    /**
     *  轮训设备绑定 Token 结果， 响应中返回 State为2时表示设备绑定Token成功。
     **/
    HttpRequest.instance.checkDeviceBindTokenState(callback);
```

10、用户绑定 Wi-Fi 设备 [用户绑定 Wi-Fi 设备](https://cloud.tencent.com/document/product/1081/44046)
```
    /**
     *  用户绑定 Wi-Fi 设备， 响应成功返回无错误码，表示用户绑定 Wi-Fi 设备成功，此时设备已与App用户对应的家庭建立好对应关系，至此配网成功结束，可以断开app与设备的蓝牙连接了。
     **/
    HttpRequest.instance.wifiBindDevice(App.data.getCurrentFamily().FamilyId, data, callback)
    /**
     *  断开app与设备的蓝牙连接。
     **/
    bluetoothGatt?.close()
```

以上配网流程可参考 腾讯连连App中 [ConnectModel](https://github.com/tencentyun/iot-link-android/blob/8634f616fa45a0634b3bb981b12558555bd83c72/app/src/main/java/com/tencent/iot/explorer/link/mvp/model/ConnectModel.kt) 类的实现。