## VideoSDK 接入使用说明

---------------------------

### 1.快速开始
#### 1.1 使用动态库so
1.1.1 下载路径

   (1) [so下载地址](https://oss.sonatype.org/#welcome)

   (2) 路径：Repositories --> Snapshots --> 在path look up 输入框中输入com/tencent/iot/thirdparty/android --> xp2p-sdk -->版本号(1.0.0-SNAPSHOT) --> 选择最新的aar右键下载

1.1.2 工程如何引用：

   (1). 解压上一步骤下载下来的aar，目录结构如下：
   ```
   ├── assets
   │   └── appWrapper.h （头文件）
   ├── jni
   │   ├── arm64-v8a
   │   │   └── libxnet-android.so
   │   └── armeabi-v7a
   │       └── libxnet-android.so
   ```
   (2). 将头文件和so动态库放在自己工程目录下，确保CMakeList.txt可以找到对应的路径即可

   (3). 使用样例：
   ```
   ├── cpp
   │   ├── CMakeLists.txt
   │   ├── include
   │   │   └── appWrapper.h
   │   ├── libs
   │   │   ├── arm64-v8a
   │   │   │   └── libxnet-android.so
   │   │   └── armeabi-v7a
   │   │       └── libxnet-android.so
   │   └── native-lib.cpp
   ```
   在CMakeLists.txt中加上以下代码即可:
   ```
   add_library(test-lib SHARED IMPORTED)
   set_target_properties(test-lib PROPERTIES IMPORTED_LOCATION ${PROJECT_SOURCE_DIR}/../jniLibs/${ANDROID_ABI}/libxnet-android.so)
   include_directories(${PROJECT_SOURCE_DIR}/include)
   target_link_libraries( native-lib test-lib ${log-lib})
   ```
#### 1.2 使用Android aar库
1.2.1 工程如何引用：
在工程的build.gradle中配置仓库url
```
allprojects {
    repositories {
        google()
        jcenter()
        maven {
            url "https://oss.sonatype.org/content/repositories/snapshots"
        }
    }
}
```
在应用模块的build.gradle中配置
```
dependencies {
    implementation 'com.tencent.iot.video:video-link-android:1.4.0-SNAPSHOT'
}
```

### 2.示例代码
#### 2.1 使用使用动态库so
2.1.1 P2P通道初始化

  函数声明:

  ```
  int startServiceWithXp2pInfo(const char* id, const char *product_id, const char *device_name, const char *xp2p_info_attr,  const char* xp2p_info);
  ```

  代码示例:
  ```
  const char* xp2p_info = getXP2PInfo(...); // 从自建后台获取xp2p info
  setUserCallbackToXp2p(_av_data_recv, _msg_notify);  //设置回调函数
  startServiceWithXp2pInfo($id, $product_id, $device_name, NULL, xp2p_info);
  ```
2.1.2 P2P通道传输音视频流

2.1.2.1 接收裸数据

  函数声明:
  ```
  void *startAvRecvService(const char *id, const char *params, bool crypto); //启动接收数据服务, 使用该方法首先需调用setUserCallbackToXp2p()注册回调
  void _av_data_recv(const char *id, uint8_t *data, size_t len);  //裸数据回调接口(具体以自己设置的为准)
  ```

  代码示例:
  ```
  ...
  setUserCallbackToXp2p(_av_data_recv, _msg_notify);
  void *req = startAvRecvService($id, "action=live", true);
  void _av_data_recv(const char *id, uint8_t *data, size_t len) {
      //具体数据处理
  }
  stopAvRecvService(id, req);
  ```

2.1.2.2 接收FLV音视频流，使用ijkplayer播放

  函数声明:
  ```
  const char *delegateHttpFlv(const char *id); // 获取本地请求数据的标准http url,可使用该url请求设备端数据
  ```
  播放器调用示例:
  ```
  ...
  ```

2.1.3 发送语音对讲数据

  函数声明:
  ```
  void *runSendService(const char *id, const char *params, bool crypto); //启动p2p数据发送服务
  int dataSend(const char *id, uint8_t *data, size_t len);  //语音数据发送接口
  ```

  代码示例:
  ```
  void *req = runSendService($id, NULL, true);
  while (1) {
      dataSend($id, audio_data, data_len);
      usleep(100 * 1000);
  }
  stopSendService(id, req);  //停止发送服务
  ```
2.1.3 P2P通道传输自定义数据

2.1.3.1 发送自定义数据

  函数声明:
  ```
  int postCommandRequestSync(const char *id, const unsigned char *command, size_t cmd_len, unsigned char **recv_buf, size_t *recv_len, uint64_t timeout_us);  //同步发送
  int postCommandRequestWithAsync(const char *id, const unsigned char *command, size_t cmd_len);  //异步发送
  ```
  代码示例:
  ```
  异步方式:
  setUserCallbackToXp2p(_av_data_recv, _msg_notify);  //设置回调
  int rc = postCommandRequestWithAsync($id, "action=user_define&cmd=xxx", sizeof(action=user_define&cmd=custom_cmd));
  if (rc != 0) {
    printf("post command request with sync failed:%d\n", rc);
  }
  同步方式:
  char *buf = NULL;
  int len = 0;
  int rc = postCommandRequestSync($id, "action=user_define&cmd=xxx", sizeof(action=user_define&cmd=custom_cmd), &buf, &len, 2*1000*1000);  //接收的数据填充在buf中
  if (rc != 0) {
    printf("post command request with async failed:%d\n", rc);
  }
  ```

2.1.3.2 接收自定义数据

  函数声明:
  ```
  char* _msg_notify(const char *id, XP2PType type, const char* msg);  //只有异步发送的才会在该回调返回接收的数据
  ```
  代码示例:
  ```
  char* _msg_notify(const char *id, XP2PType type, const char* msg) {
      if (type == XP2PTypeCmd) {
          // 处理返回结果
      }
  }
  ```
2.1.4 主动关闭P2P通道

  函数声明:
  ```
  void stopService(const char *id);
  ```
  代码示例:
  ```
  stopService(id);
  ```
2.1.5 P2P通道关闭回调

  函数声明:
  ```
  char* _msg_notify(const char *id, XP2PType type, const char* msg);
  ```
  代码示例:
  ```
  char* _msg_notify(const char *id, XP2PType type, const char* msg) {
      if (type == XP2PTypeClose) {
          //p2p通道正常关闭
      }
  }
  ```
2.1.6 P2P通道错误断开回调

  函数声明:
  ```
  char* _msg_notify(const char *id, XP2PType type, const char* msg);
  ```
  代码示例:
  ```
  char* _msg_notify(const char *id, XP2PType type, const char* msg) {
      if (type == XP2PTypeDisconnect) {
          //p2p通道错误断开
      }
  }
  ```

#### 2.2 使用Android aar库
接口详细说明可参考：[VideoSDK接口说明](https://github.com/tencentyun/iot-link-android/blob/master/sdk/video-link-android/doc/VideoSDK接口说明.md)
)

2.2.1 P2P通道初始化

  函数声明：
  ```
  public static void startServiceWithXp2pInfo(String id, String product_id, String device_name, String xp2p_info_attr, String xp2p_info)
  ```
  代码示例：
  ```
  String xp2p_info = getXP2PInfo(...) // 从自建后台获取xp2p info
  XP2P.setCallback(this)
  XP2P.startServiceWithXp2pInfo($id, $product_id, $device_name, "", xp2p_info)
  ```

2.2.2 P2P通道传输音视频流

2.2.2.1 接收裸数据

  函数声明：
  ```
  public static void startAvRecvService(String id, String params, boolean crypto) // 启动接收数据服务, 使用该方法首先需调用setCallback()注册回调
  override fun avDataRecvHandle(id: String?, data: ByteArray?, len: Int) //裸数据回调接口
  ```
  代码示例：
  ```
  ...
  XP2P.setCallback(this)
  XP2P.startAvRecvService($id, "action=live", true)
  override fun avDataRecvHandle(id: String?, data: ByteArray?, len: Int) {
      // 裸流数据处理操作可以放在这里
  }
  ```
2.2.2.2 接收FLV音视频流，使用ijkplayer播放

  函数声明：
  ```
  static native String delegateHttpFlv(String id) // 获取本地请求数据的标准http url,可使用该url请求设备端数据
  ```
  播放器调用示例:
  ```
  val url = XP2P.delegateHttpFlv($id) + "ipc.flv?action=live" //加密方式观看直播(action=live)，回放(action=playback)
  val url = XP2P.delegateHttpFlv($id) + "ipc.flv?action=live&crypto=false" //非加密方式观看直播(action=live)，回放(action=playback)
  mPlayer.dataSource = url
  mPlayer.prepareAsync()
  mPlayer.start()
  ```
2.2.2.3 发送语音对讲数据

  函数声明:
  ```
  public static void runSendService(String id, String cmd, boolean crypto) //启动p2p数据发送服务
  public static void dataSend(String id, byte[] data, int len)
  ```
  代码示例:
  ```
  XP2P.runSendService($id, "", true)
  audioRecordUtil.start() // 采集音频并发送，内部调用了dataSend接口
  XP2P.dataSend($id, flvData, flvData.length);
  ```
2.2.3 P2P通道传输自定义数据

2.2.3.1 发送自定义数据

  函数声明:
  ```
  public static int postCommandRequestWithAsync(String id, byte[] command, long cmd_len) // 异步
  public static String postCommandRequestSync(String id, byte[] command, long cmd_len, long timeout_us) //同步
  ```
  代码示例:
  ```
  XP2P.postCommandRequestWithAsync($id, "action=user_define&cmd=xxx", sizeof("action=user_define&cmd=xxx"))  //异步
  XP2P.postCommandRequestSync($id, "action=user_define&cmd=xxx", sizeof("action=user_define&cmd=xxx"), 2000 * 1000)  //同步
  ```
2.2.3.2 接收自定义数据

  函数声明:
  ```
  override fun commandRequest(id: String?, msg: String?, len: Int) // 设备端回调App
  ```
  代码示例:
  ```
  override fun commandRequest(id: String?, msg: String?, len: Int) {
      Log.d(msg) //接收到的自定义数据后，添加业务逻辑
  }
  ```
2.2.4 主动关闭P2P通道

  函数声明:
  ```
  void stopService(String id)
  ```
  代码示例:
  ```
  override fun onDestroy() {
      super.onDestroy()
      mPlayer.release()
      XP2P.stopService($id)
  }
  ```
2.2.5 P2P通道关闭回调'

  函数声明:
  ```
  override fun avDataCloseHandle(id: String?, msg: String?, errorCode: Int)  //通道关闭后回调
  ```
  代码示例:
  ```
  override fun avDataCloseHandle(id: String?, msg: String?, errorCode: Int) {
  	//处理通道关闭后的事务
  }
  ```
2.2.6 P2P通道错误断开回调

  函数声明:
  ```
  override fun xp2pLinkError(id: String?, msg: String?)  //通道错误断开后回调
  ```
  代码示例:
  ```
  override fun xp2pLinkError(id: String?, msg: String?) {
  	//处理通道错误断开后的事务
  }
  ```


### APP接入SDK说明
第三方App在接入Video SDK时，建议将`secretId`和`secretKey`保存到自建后台，不推荐将这两个信息保存至App端; 而SDK需要的xp2p info需要App侧从自己的业务后台获取；获取到xp2p info后，可以通过上述的`startServiceWithXp2pInfo`接口将该info传给SDK，示例代码如下：
```
...
String xp2p_info = getXP2PInfo(...) // 从自建后台获取xp2p info
XP2P.setCallback(this)
XP2P.startServiceWithXp2pInfo(id, product_id, device_name, "", xp2p_info)
```