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
  /**
  * @brief 设置回调函数
  *
  * @param recv_handle: 音视频数据回调
  * @param msg_handle: 控制类消息回调
  * @return 无返回值
  */
  void setUserCallbackToXp2p(av_recv_handle_t recv_handle, msg_handle_t msg_handle);

  /**
  * @brief 初始化xp2p服务
  *
  * @param id: 目标camera在app端的唯一标识符
  * @param product_id: 产品ID
  * @param device_name: 设备名称
  * @param xp2p_info_attr: xp2p属性，当前版本固定为`_sys_xp2p_info`，若xp2p_info已获取，该参数可传入null
  * @param xp2p_info: xp2p信息
  * @return 0 为成功
  */
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
  /**
  * @brief 向camera设备请求媒体流，异步回调方式
  *
  * @param id: 目标camera在app端的唯一标识符
  * @param params: 直播(`action=live`)或回放(`action=playback`)参数
  * @param crypto: 是否开启传输层加密，如果关闭(crypto=false)，则建议用户在应用层加密，否则有安全风险
  * @return 请求句柄
  */
  void *startAvRecvService(const char *id, const char *params, bool crypto); //启动接收数据服务, 使用该方法首先需调用setUserCallbackToXp2p()注册回调

  /**
  * @brief 接收设备媒体流回调
  *
  * @param id: 回传`startServiceWithXp2pInfo`接口中的`id`
  * @param data: 接收到的数据
  * @param len: 接收到的数据长度
  * @return 请求句柄
  */
  void _av_data_recv(const char *id, uint8_t *data, size_t len);  //裸数据回调接口(具体以自己设置的为准)

  /**
  * @brief 关闭媒体流传输
  *
  * @param id: 目标camera在app端的唯一标识符
  * @param req: 接口`startAvRecvService`的返回值，当前版本可传入null
  * @return 0 为成功
  */
  int stopAvRecvService(const char *id, void *req);
  ```

  代码示例:
  ```
  ...
  setUserCallbackToXp2p(_av_data_recv, _msg_notify);
  void *req = startAvRecvService($id, "action=live", true);
  void _av_data_recv(const char *id, uint8_t *data, size_t len) {
    //具体数据处理
    //回调中应避免耗时操作
    //多路p2p传输场景需根据回传的`id`判断对应的p2p通道,以做相应处理
  }
  stopAvRecvService(id, req);
  ```

2.1.2.2 接收FLV音视频流，使用ijkplayer播放

  函数声明:
  ```
  /**
  * @brief 获取本地代理url
  *
  * @param id: 目标camera在app端的唯一标识符
  * @return 本地代理url
  */
  const char *delegateHttpFlv(const char *id); // 获取本地请求数据的标准http url,可使用该url请求设备端数据
  ```
  播放器调用示例:
  ```
  ...
  ```

2.1.3 发送语音对讲数据

  函数声明:
  ```
  /**
  * @brief 启动向camera设备发送语音或自定义数据服务，异步非阻塞方式
  *
  * @param id: 目标camera在app端的唯一标识符
  * @param params: 请求参数采用 key1=value&key2=value2 格式，key不允许以下划线_开头，且key和value中间不能包含&/+=特殊字符
  * @param crypto: 是否开启传输层加密，如果关闭(crypto=false)，则建议用户在应用层加密，否则有安全风险
  * @return 请求句柄
  */
  void *runSendService(const char *id, const char *params, bool crypto); //启动p2p数据发送服务

  /**
  * @brief 向camera设备发送语音或自定义数据
  *
  * @param id: 目标camera在app端的唯一标识符
  * @param data: 要发送的数据内容
  * @param len: 要发送的数据长度
  * @return 0 为成功
  */
  int dataSend(const char *id, uint8_t *data, size_t len);  //语音数据发送接口

  /**
  * @brief 关闭语音发送传输
  *
  * @param id: 目标camera在app端的唯一标识符
  * @param req: 接口`runSendService`的返回值，当前版本可传入null
  * @return 请求句柄
  */
  int stopSendService(const char *id, void *req);
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
  /**
  * @brief 发送信令消息给camera设备并等待回复，同步阻塞方式
  *
  * @param id: 目标camera在app端的唯一标识符
  * @param command: 可以为任意格式字符或二进制数据，长度由cmd_len提供，建议在16KB以内，否则会影响实时性
  * @param cmd_len: command长度
  * @param recv_buf: 用于存放camera回复的数据，内存由接口内部申请外部释放，实际数据长度根据recv_len获取
  * @param recv_len: camera回复的数据长度
  * @param timeout_us: 命令超时时间，单位为微秒，值为0时采用默认超时(7500ms左右)
  * @return 0 为成功
  */
  int postCommandRequestSync(const char *id, const unsigned char *command, size_t cmd_len, unsigned char **recv_buf, size_t *recv_len, uint64_t timeout_us);  //同步发送

  /**
  * @brief 发送信令消息给camera设备，camera回复的数据由注册的回调函数返回，异步非阻塞方式
  *
  * @param id: 目标camera在app端的唯一标识符
  * @param command: 可以为任意格式字符或二进制数据，长度由cmd_len提供，建议在16KB以内，否则会影响实时性
  * @param cmd_len: command长度
  * @return 0 为成功
  */
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
  unsigned char *buf = NULL;
  size_t len = 0;
  int rc = postCommandRequestSync($id, "action=user_define&cmd=xxx", sizeof(action=user_define&cmd=custom_cmd), &buf, &len, 2*1000*1000);  //接收的数据填充在buf中
  if (rc != 0) {
    printf("post command request with async failed:%d\n", rc);
  }

  delete buf;
  ```

2.1.3.2 接收自定义数据

  函数声明:
  ```
  enum XP2PType {
    XP2PTypeClose   = 1000, //数据传输完成
    XP2PTypeLog     = 1001, //日志输出
    XP2PTypeCmd     = 1002, //command json
    XP2PTypeDisconnect  = 1003, //p2p链路断开
    XP2PTypeSaveFileOn  = 8000, //获取保存音视频流开关状态
    XP2PTypeSaveFileUrl = 8001 //获取音视频流保存路径
  };

  /**
  * @brief 控制类消息回调
  *
  * @param id: 回传`startServiceWithXp2pInfo`接口中的`id`
  * @param type: 消息类型,自定义数据类型为`XP2PTypeCmd`
  * @param msg: 接收到的消息
  * @return type为`XP2PTypeSaveFileOn`和`XP2PTypeSaveFileUrl`时需返回具体字符串,详见`native-lib.cpp`
  */
  char* _msg_notify(const char *id, XP2PType type, const char* msg);  //只有异步发送的才会在该回调返回接收的数据
  ```
  代码示例:
  ```
  char* _msg_notify(const char *id, XP2PType type, const char* msg) {
      if (type == XP2PTypeCmd) {
        //处理返回结果
        //回调中应避免耗时操作
        //多路p2p传输场景需根据回传的`id`判断对应的p2p通道,以做相应处理
      }
  }
  ```
2.1.4 主动关闭P2P通道

  函数声明:
  ```
  /**
  * @brief 停止xp2p服务
  *
  * @param id: 目标camera在app端的唯一标识符
  * @return 无返回值
  */
  void stopService(const char *id);
  ```
  代码示例:
  ```
  stopService(id);
  ```
2.1.5 P2P通道关闭回调

  函数声明:
  ```
  /**
  * @brief 控制类消息回调
  *
  * @param id: 回传`startServiceWithXp2pInfo`接口中的`id`
  * @param type: 消息类型,p2p通道关闭通知类型为`XP2PTypeClose`
  * @param msg: 接收到的消息
  * @return type为`XP2PTypeSaveFileOn`和`XP2PTypeSaveFileUrl`时需返回具体字符串,详见`native-lib.cpp`
  */
  char* _msg_notify(const char *id, XP2PType type, const char* msg);
  ```
  代码示例:
  ```
  char* _msg_notify(const char *id, XP2PType type, const char* msg) {
      if (type == XP2PTypeClose) {
        //p2p通道正常关闭
        //回调中应避免耗时操作
        //多路p2p传输场景需根据回传的`id`判断对应的p2p通道,以做相应处理
      }
  }
  ```
2.1.6 P2P通道错误断开回调

  函数声明:
  ```
  /**
  * @brief 控制类消息回调
  *
  * @param id: 回传`startServiceWithXp2pInfo`接口中的`id`
  * @param type: 消息类型,p2p通道错误断开类型为`XP2PTypeDisconnect`
  * @param msg: 接收到的消息
  * @return type为`XP2PTypeSaveFileOn`和`XP2PTypeSaveFileUrl`时需返回具体字符串,详见`native-lib.cpp`
  */
  char* _msg_notify(const char *id, XP2PType type, const char* msg);
  ```
  代码示例:
  ```
  char* _msg_notify(const char *id, XP2PType type, const char* msg) {
      if (type == XP2PTypeDisconnect) {
        //p2p通道错误断开
        //回调中应避免耗时操作
        //多路p2p传输场景需根据回传的`id`判断对应的p2p通道,以做相应处理
      }
  }
  ```

#### 2.2 使用Android aar库
接口详细说明可参考：[VideoSDK接口说明](https://github.com/tencentyun/iot-link-android/blob/master/sdk/video-link-android/doc/VideoSDK接口说明.md)
)

2.2.1 P2P通道初始化

  函数声明：
  ```
  /**
  * @brief 初始化xp2p服务
  *
  * @param id: 目标camera在app端的唯一标识符
  * @param product_id: 产品ID
  * @param device_name: 设备名称
  * @param xp2p_info_attr: xp2p属性，当前版本固定为`_sys_xp2p_info`，若xp2p_info已获取，该参数可传入null
  * @param xp2p_info: xp2p信息
  * @return 0 为成功
  */
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
  /**
  * @brief 向camera设备请求媒体流，异步回调方式
  *
  * @param id: 目标camera在app端的唯一标识符
  * @param params: 直播(`action=live`)或回放(`action=playback`)参数
  * @param crypto: 是否开启传输层加密，如果关闭(crypto=false)，则建议用户在应用层加密，否则有安全风险
  * @return 请求句柄
  */
  public static void startAvRecvService(String id, String params, boolean crypto) // 启动接收数据服务, 使用该方法首先需调用setCallback()注册回调

  /**
  * @brief 媒体流接收回调
  *
  * @param id: 回传`startServiceWithXp2pInfo`接口中的`id`
  * @param data: 接收到的媒体流数据
  * @param Int: 接收到的媒体流数据长度
  */
  override fun avDataRecvHandle(id: String?, data: ByteArray?, len: Int) //裸数据回调接口
  ```
  代码示例：
  ```
  ...
  XP2P.setCallback(this)
  XP2P.startAvRecvService($id, "action=live", true)
  override fun avDataRecvHandle(id: String?, data: ByteArray?, len: Int) {
    //裸流数据处理
    //回调中应避免耗时操作
    //多路p2p传输场景需根据回传的`id`判断对应的p2p通道,以做相应处理
  }
  ```
2.2.2.2 接收FLV音视频流，使用ijkplayer播放

  函数声明：
  ```
  /**
  * @brief 获取本地代理url
  *
  * @param id: 目标camera在app端的唯一标识符
  * @return 本地代理url
  */
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
  /**
  * @brief 启动向camera设备发送语音或自定义数据服务，异步非阻塞方式
  *
  * @param id: 目标camera在app端的唯一标识符
  * @param cmd: 请求参数采用 key1=value&key2=value2 格式，key不允许以下划线_开头，且key和value中间不能包含&/+=特殊字符
  * @param crypto: 是否开启传输层加密，如果关闭(crypto=false)，则建议用户在应用层加密，否则有安全风险
  * @return 无返回值
  */
  public static void runSendService(String id, String cmd, boolean crypto) //启动p2p数据发送服务

  /**
  * @brief 向camera设备发送语音或自定义数据
  *
  * @param id: 目标camera在app端的唯一标识符
  * @param data: 要发送的数据内容
  * @param len: 要发送的数据长度
  * @return 无返回值
  */
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
  /**
  * @brief 发送信令消息给camera设备，camera回复的数据由注册的回调函数返回，异步非阻塞方式
  *
  * @param id: 目标camera在app端的唯一标识符
  * @param command: 可以为任意格式字符或二进制数据，长度由cmd_len提供，建议在16KB以内，否则会影响实时性
  * @param cmd_len: command长度
  * @return 0 为成功
  */
  public static int postCommandRequestWithAsync(String id, byte[] command, long cmd_len) // 异步

  /**
  * @brief 发送信令消息给camera设备并等待回复，同步阻塞方式
  *
  * @param id: 目标camera在app端的唯一标识符
  * @param command: 可以为任意格式字符或二进制数据，长度由cmd_len提供，建议在16KB以内，否则会影响实时性
  * @param cmd_len: command长度
  * @param timeout_us: 命令超时时间，单位为微秒，值为0时采用默认超时(7500ms左右)
  * @return 设备端的回复内容
  */
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
  /**
  * @brief 控制类消息回调
  *
  * @param id: 回传`startServiceWithXp2pInfo`接口中的`id`
  * @param msg: 控制类消息
  * @param len: 消息长度
  * @return 0 为成功
  */
  override fun commandRequest(id: String?, msg: String?, len: Int) // 设备端回调App
  ```
  代码示例:
  ```
  override fun commandRequest(id: String?, msg: String?, len: Int) {
    //接收到的自定义数据后，添加业务逻辑
    //回调中应避免耗时操作
    //多路p2p传输场景需根据回传的`id`判断对应的p2p通道,以做相应处理
  }
  ```
2.2.4 主动关闭P2P通道

  函数声明:
  ```
  /**
  * @brief 停止xp2p服务
  *
  * @param id: 目标camera在app端的唯一标识符
  * @return 无返回值
  */
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
  /**
  * @brief p2p通道正常关闭通知回调
  *
  * @param id: 回传`startServiceWithXp2pInfo`接口中的`id`
  * @param msg: 附加说明消息,可忽略
  * @param len: 消息长度
  */
  override fun avDataCloseHandle(id: String?, msg: String?, errorCode: Int)  //通道关闭后回调
  ```
  代码示例:
  ```
  override fun avDataCloseHandle(id: String?, msg: String?, errorCode: Int) {
    //处理通道关闭后的事务
    //回调中应避免耗时操作
    //多路p2p传输场景需根据回传的`id`判断对应的p2p通道,以做相应处理
  }
  ```
2.2.6 P2P通道错误断开回调

  函数声明:
  ```
  /**
  * @brief p2p通道正常关闭通知回调
  *
  * @param id: 回传`startServiceWithXp2pInfo`接口中的`id`
  * @param msg: 附加说明消息,可忽略
  */
  override fun xp2pLinkError(id: String?, msg: String?)  //通道错误断开后回调
  ```
  代码示例:
  ```
  override fun xp2pLinkError(id: String?, msg: String?) {
    //处理通道错误断开后的事务
    //回调中应避免耗时操作
    //多路p2p传输场景需根据回传的`id`判断对应的p2p通道,以做相应处理
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