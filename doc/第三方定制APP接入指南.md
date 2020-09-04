## APP 接入指南详情

- 接入前注意事项请参考 [接入指南](https://github.com/tencentyun/iot-link-android/blob/master/doc/%E7%AC%AC%E4%B8%89%E6%96%B9%E5%AE%9A%E5%88%B6APP%E6%8E%A5%E5%85%A5%E6%8C%87%E5%8D%97.md)   

- Android 版本 可通过 [HttpRequest](https://github.com/tencentyun/iot-link-android/blob/master/app/src/main/java/com/tencent/iot/explorer/link/kitlink/util/HttpRequest.kt) 中设置API 。
   **登录前所使用的 API URL** 在 **OEM_APP_API** 配置，**请务必替换成自建的后台服务地址。**   
      
   ```
        /**
         * 接口请求文件
         */
   class HttpRequest private constructor() {
           companion object {
                 
           // 公版&开源体验版使用  当在 app-config.json 中配置 TencentIotLinkAppkey TencentIotLinkAppSecret 后，将自动切换为 OEM 版本。
           const val STUDIO_BASE_URL = "https://iot.cloud.tencent.com/api/studioapp"
           const val STUDIO_BASE_URL_FOR_LOGINED = "https://iot.cloud.tencent.com/api/studioapp/tokenapi"

           // OEM App 使用
           const val OEM_APP_API = "https://iot.cloud.tencent.com/api/exploreropen/appapi" // 需要替换为自建后台服务地址
           const val OEM_TOKEN_API = "https://iot.cloud.tencent.com/api/exploreropen/tokenapi"  // 可安全在设备端调用。

           const val APP_COS_AUTH = "https://iot.cloud.tencent.com/api/studioapp/AppCosAuth"
           const val BUSI_APP = "studioapp"
           const val BUSI_OPENSOURCE = "studioappOpensource"
           }
      }   
  ```

