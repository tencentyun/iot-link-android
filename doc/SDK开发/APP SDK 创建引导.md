## APP SDK 创建引导

**使用说明**

- Android
选择 `sdkdemo/src/main/java/com/tencent/iot/explorer/link/core/demo/App.java`，配置 App key。   

```
class App : Application() {

     companion object {
             val data = AppData.instance
     }

     private val APP_KEY = "物联网开发平台申请的 App Key"

     override fun onCreate() {
             super.onCreate()
             L.isLog = true
             //需要打印日志时要在IoTAuth.init(APP_KEY)之前调用
             // 否则看不到"The SDK initialized successfully"的日志
             IoTAuth.openLog(true)
             IoTAuth.init(APP_KEY)
             IoTAuth.addLoginExpiredListener(object : LoginExpiredListener {
                     override fun expired(user: User) {
                             L.d("用户登录过期")
                     }
             })
     }

     override fun onTerminate() {
             IoTAuth.destroy()
             super.onTerminate()
     }
}   
```