## APP 发布前确认事项   

1、请根据实际情况调整 **config.json** 中的内容，config.json 位于项目的根目录，如截图所示位置。

<img src="IMG/image-20200619141407513.png" alt="image-20200619141407513" style="zoom: 67%;" />

config.json 需要配置的内容，如下图所示。

<img src="IMG/image-20200619141716806.png" alt="image-20200619141716806" style="zoom:67%;" />

* **TencentIotLinkAppkey** 和 **TencentIotLinkAppSecrecy** 请使用在物联网平台创建应用时生成的 **APP Key** 和 **APP Secret**。<font color=red>AppKey 和 AppSecret 用于访问应用端 API 时生成签名串，参见[应用端 API 简介](https://cloud.tencent.com/document/product/1081/40773)。签名算法**务必在服务端实现**，腾讯连连 App 开源版的使用方式**仅为演示**，请勿将 AppKey 和 AppSecret 保存在客户端，**避免泄露**</font>。

* **XgAccessId** 和 **XgAccessKey** 请使用在信鸽推送平台申请获得的 **AccessID** 和 **AccessKey**，详情可见 https://cloud.tencent.com/product/tpns/getting-started。

* **WXAccessAppId** 请使用在微信开放平台申请并获得的 **AppID**；可通过在微信开放平台注册开发者帐号，创建移动应用，审核通过后，即可获得相应的 AppID 和 AppSecret，详情可见 https://developers.weixin.qq.com/doc/oplatform/Mobile_App/WeChat_Login/Development_Guide.html。

  使用微信授权登录还需：

  - 将 *opensource_keystore.jks* 文件替换成自己的签名文件并给应用签字

  - 前往[微信开放平台](https://developers.weixin.qq.com/doc/oplatform/Downloads/Android_Resource.html)下载签名生成工具，使用该工具生成应用的数字签名(需要将该工具和应用同时安装到手机上，打开签名生成工具输入应用包名即可生成数字签名，如下图所示)

    <img src="IMG/image-20200619162858817.png" alt="image-20200619162858817" style="zoom: 33%;" />

  - 将该数字签名和应用包名登记到微信开放平台，否则微信授权登录将不可用

* **TencentMapSDKValue** 请使用腾讯地图开放平台申请并获得的 **key**，详情可见 https://lbs.qq.com/mobile/androidLocationSDK/androidGeoGuide/androidGeoCreat。



2、项目配置了 **Firebase** 插件

* 若用户确认使用 Firebase 功能，需通过 Firebase 官网创建应用并获取 **google-services.json**；将 google-services.json 文件放置在 app 目录下，如截图所示位置。

  <img src="IMG/image-20200619150459211.png" alt="image-20200619150459211" style="zoom:67%;" />

* 若不使用 Firebase 功能，请注释截图中标注的内容即可

  <img src="IMG/image-20200619150752594.png" alt="image-20200619150752594" style="zoom: 50%;" />

  <img src="IMG/image-20200619151433681.png" alt="image-20200619151433681" style="zoom: 50%;" />

  <img src="IMG/image-20200619151507503.png" alt="image-20200619151507503" style="zoom: 50%;" />