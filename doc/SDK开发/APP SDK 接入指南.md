## APP SDK 接入指南

**1、SDK 模块说明**

[详情可见](https://github.com/tencentyun/iot-link-ios/blob/master/doc/%E5%B9%B3%E5%8F%B0%E6%8A%80%E6%9C%AF%E6%96%87%E6%A1%A3/SDK%20%E6%8E%A5%E5%85%A5%E8%AF%B4%E6%98%8E.md)

**2、API 接口说明**

[详情可见](https://github.com/tencentyun/iot-link-ios/blob/master/doc/%E5%B9%B3%E5%8F%B0%E6%8A%80%E6%9C%AF%E6%96%87%E6%A1%A3/SDK%20%E6%8E%A5%E5%85%A5%E8%AF%B4%E6%98%8E.md)

**3、SDK 接入详情**

1. 设备配网

   智能配网（SmartConfigService）

   ```kotlin
   val task = LinkTask()
   var smartConfigService: SmartConfigService = SmartConfigService(context)
   val smartConfigListener = object : SmartConfigListener {
   	override fun onSuccess(deviceInfo: DeviceInfo) {}
   	override fun deviceConnectToWifi(result: IEsptouchResult) {}
   	override fun onStep(step: SmartConfigStep) {
           //回调当前配网状态
       }
   	override fun deviceConnectToWifiFail() {}
   	override fun onFail(exception: TCLinkException) {}
   	}
   
   smartConfigService.startConnect(task, smartConfigListener)
   ```

   自助配网（SoftAPService）

   ```kotlin
   val task = LinkTask()
   var softAPService = SoftAPService(this)
   val softAPListener = object : SoftAPListener {
   	override fun onSuccess(deviceInfo: DeviceInfo) {}
   	override fun reconnectedSuccess(deviceInfo: DeviceInfo) {}
   	override fun reconnectedFail(deviceInfo: DeviceInfo, ssid: String) {}
   	override fun onStep(step: SoftAPStep) {
           //回调当前配网状态
       }
   	override fun onFail(code: String, msg: String) {}
   	}
           
   softAPService.startConnect(task, softAPListener)
   ```

2. 账户登录相关接口（LoginImpl）

   ```kotlin
       /**
        *  手机号登录
        */
       fun loginPhone(countryCode: String, phone: String, pwd: String, callback: LoginCallback)
   
       /**
        *  邮箱登录
        */
       fun loginEmail(email: String, pwd: String, callback: LoginCallback)
   ```

4. 家庭相关接口（RoomImpl）

   ```kotlin
       /**
        * 创建房间
        */
       fun create(familyId: String, roomName: String, callback: MyCallback)
   
       /**
        * 修改房间
        */
       fun modify(familyId: String, roomId: String, roomName: String, callback: MyCallback)
   
       /**
        * 删除房间
        */
       fun delete(familyId: String, roomId: String, callback: MyCallback)
   ```


若接入过程中有其他问题，请参考 [APP SDK开发常见问题](https://github.com/tencentyun/iot-link-ios/blob/master/doc/SDK%E5%BC%80%E5%8F%91/APP%20SDK%E5%BC%80%E5%8F%91%E5%B8%B8%E8%A7%81%E9%97%AE%E9%A2%98.md )

**4、API请求域名配置**
[详情可见](https://github.com/tencentyun/iot-link-android/blob/master/doc/SDK开发/私有化/API请求域名配置指引.md)