## VideoSDK 接入使用说明
---------------------------

### 1.快速开始
#### 1.1 使用动态库so
* 下载路径

    稳定版：
    * (1) [so下载地址](https://search.maven.org/search?q=xp2p)
    * (2) 路径：点击版本号(例如: 1.0.0, 具体版本号可以点击上面链接查看) --> Downloads(右上角) --> aar

    SNAPSHOT版：
    * (1) [so下载地址](https://oss.sonatype.org/#welcome)
    * (2) 路径：Repositories --> Snapshots --> 在path look up 输入框中输入com/tencent/iot/thirdparty/android --> xp2p-sdk -->版本号(例如：1.0.0-SNAPSHOT) --> 选择最新的aar右键下载

    **注：建议使用稳定版本，SNAPSHOT版仅供开发自测使用**

* 工程如何引用：

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
* 工程如何引用：

##### 1.2.1 引用稳定版：

在应用模块的build.gradle中配置
```
dependencies {
    implementation 'com.tencent.iot.video:video-link-android:x.x.x'
}
```
具体版本号可参考[版本号列表](https://search.maven.org/search?q=video-link-android)

##### 1.2.2 引用SNAPSHOT版：

(1). 在工程的build.gradle中配置仓库url
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
(2). 在应用模块的build.gradle中配置
```
dependencies {
    implementation 'com.tencent.iot.video:video-link-android:x.x.x-SNAPSHOT'
}
```

**注：建议使用稳定版本，SNAPSHOT版仅供开发自测使用**

### 2.示例代码
#### 2.1 使用使用动态库so
##### 2.1.0 用户回调
###### 2.1.0.0 媒体流数据接收回调
* 接口描述:
接收设备媒体流数据回调。用于裸流方式观看直播时，SDK回调该接口向用户传输接收的设备端媒体流数据。
```
typedef void (*av_recv_handle_t)(const char *id, uint8_t* recv_buf, size_t recv_len);
```
* 参数说明:
| 参数 | 类型 | 描述 | 输入/输出 |
|:-:|:-:|:-:|:-:|
| id | const char * | 目标camera在app端的唯一标识符 | 输出 |
| recv_buf | uint8_t * | 接收到的媒体流数据 | 输出 |
| recv_len | size_t | 接收到的媒体流数据长度 | 输出 |

* 返回值
| 返回值 | 描述 |
|:-:|:-:|
| void | - |

###### 2.1.0.1 控制类消息通知回调
* 接口描述:
接收控制类消息回调。用于向设备请求非媒体流数据时，SDK回调该接口向用户传输接收的设备端消息，也用于通知用户p2p连接、数据传输等状态改变。
```
enum XP2PType {
  XP2PTypeClose   = 1000, //数据传输完成
  XP2PTypeLog     = 1001, //日志输出
  XP2PTypeCmd     = 1002, //command json
  XP2PTypeDisconnect  = 1003, //p2p链路断开
  XP2PTypeSaveFileOn  = 8000, //获取保存音视频流开关状态
  XP2PTypeSaveFileUrl = 8001 //获取音视频流保存路径
};
typedef const char* (*msg_handle_t)(const char *id, XP2PType type, const char* msg);
```
* 参数说明:
| 参数 | 类型 | 描述 | 输入/输出 |
|:-:|:-:|:-:|:-:|
| id | const char * | 目标camera在app端的唯一标识符 | 输出 |
| type | XP2PType | 通知类型 | 输出 |
| msg | const char * | 接收到的消息 | 输出 |

* 返回值
| 返回值 | 描述 |
|:-:|:-:|
| char * | 根据type不同有不同返回值 |

##### 2.1.1 设置用户回调
* 接口描述:
设置用户回调函数。媒体流数据和控制类消息通过设置的回调函数返回。
```
void setUserCallbackToXp2p(av_recv_handle_t recv_handle, msg_handle_t msg_handle);
```
* 参数说明:
| 参数 | 类型 | 描述 | 输入/输出 |
|:-:|:-:|:-:|:-:|
| recv_handle | av_recv_handle_t | 媒体流数据回调|输入 |
| msg_handle | msg_handle_t | 控制类消息回调|输入 |
  
* 返回值
| 返回值 | 描述 |
|:-:|:-:|
| void  |- |

* 代码示例:
```
/* av_recv_handle_t回调 */
void _av_data_recv(const char *id, uint8_t *data, size_t len)
{
	printf("this is _av_data_recv\n");
}
/* msg_handle_t回调 */
const char* _msg_notify(const char *id, XP2PType type, const char* msg)
{
	printf("this is _msg_notify\n");
}

setUserCallbackToXp2p(_av_data_recv, _msg_notify);
```

##### 2.1.2 P2P通道初始化
* 接口描述:
初始化xp2p服务。
```
int startServiceWithXp2pInfo(const char* id, const char *product_id, const char *device_name, const char* xp2p_info);
```
* 参数说明:
| 参数 | 类型 | 描述 | 输入/输出 |
|:-:|:-:|:-:|:-:|
| id | const char * | 目标camera在app端的唯一标识符 | 输入 |
| product_id | const char * | 目标camera产品信息 | 输入 |
| device_name | const char * | 目标camera设备名称 | 输入 |
| xp2p_info | const char * | xp2p信息 | 输入 |

* 返回值
| 返回值 | 描述 |
|:-:|:-:|
| XP2PERRNONE | 成功 |
| XP2PERR* | 失败，对应错误码 |

* 代码示例:
```
/* 伪代码：从自建后台获取xp2p info */
const char* xp2p_info = getXP2PInfo(...);
/* 设置回调函数 */
setUserCallbackToXp2p(_av_data_recv, _msg_notify);
/* 初始化p2p */
startServiceWithXp2pInfo($id, $product_id, $device_name, xp2p_info);
```

##### 2.1.3 P2P通道传输音视频裸流
###### 2.1.3.0 启动裸流接收服务
* 接口描述:
向camera设备请求媒体流，异步回调方式。
```
void *startAvRecvService(const char *id, const char *params, bool crypto);
```
* 参数说明:
| 参数 | 类型 | 描述 | 输入/输出 |
|:-:|:-:|:-:|:-:|
| id | const char * | 目标camera在app端的唯一标识符 | 输入 |
| params | const char * | 直播(`action=live`)或回放(`action=playback`)参数 | 输入 |
| crypto | bool | 是否开启传输层加密 | 输入 |

* 返回值
| 返回值 | 描述 |
|:-:|:-:|
| 服务句柄 | 成功 |
| NULL | 失败 |

###### 2.1.3.1 停止接收服务
* 接口描述:
停止裸流接收，并关闭接收服务。
```
int stopAvRecvService(const char *id, void *req);
```
* 参数说明:
| 参数 | 类型 | 描述 | 输入/输出 |
|:-:|:-:|:-:|:-:|
| id | const char * | 目标camera在app端的唯一标识符 | 输入 |
| req | void * | 服务句柄 | 输入 |

* 返回值
| 返回值 | 描述 |
|:-:|:-:|
| XP2PERRNONE | 成功 |
| XP2PERR* | 失败，对应错误码 |

* 代码示例:
```
/* 设置回调函数 */
setUserCallbackToXp2p(_av_data_recv, _msg_notify);
/* 开始请求数据 */
void *req = startAvRecvService($id, "action=live", true);
/* 接收到数据后回调被触发 */
void _av_data_recv(const char *id, uint8_t *data, size_t len)
{
	//具体数据处理
	//回调中应避免耗时操作
	//多路p2p传输场景需根据回传的`id`判断对应的p2p通道,以做相应处理
}
/* 停止接收 */
stopAvRecvService($id, req);
```

##### 2.1.4 接收FLV音视频流，使用ijkplayer播放
* 接口描述:
获取本地代理url。用于播放器直接通过url获取数据进行播放。
```
const char *delegateHttpFlv(const char *id);
```
* 参数说明:
| 参数 | 类型 | 描述 | 输入/输出 |
|:-:|:-:|:-:|:-:|
| id | const char * | 目标camera在app端的唯一标识符 | 输入 |

* 返回值
| 返回值 | 描述 |
|:-:|:-:|
| 本地代理url | 成功 |
| NULL | 失败 |

* 代码示例:
```
char url[128] = { 0 };
/* 组合请求url */
snprintf(url, sizeof(url), "%s%s", delegateHttpFlv($id), "ipc.flv?action=live");
/* 设置url到播放器 */
setUrl2Player(url);
```

##### 2.1.4 发送语音对讲数据
###### 2.1.4.0 启动语音发送服务
* 接口描述:
启动向camera设备发送语音或自定义数据服务。异步非阻塞方式。
```
void *runSendService(const char *id, const char *params, bool crypto);
```
* 参数说明:
| 参数 | 类型 | 描述 | 输入/输出 |
|:-:|:-:|:-:|:-:|
| id | const char * | 目标camera在app端的唯一标识符 | 输入 |
| params | const char * | 请求参数采用`key1=value&key2=value2`格式，key不允许以下划线_开头，且key和value中间不能包含&/+=特殊字符 | 输入 |
| crypto | bool | 否开启传输层加密 | 输入 |

* 返回值
| 返回值 | 描述 |
|:-:|:-:|
| 服务句柄 | 成功 |
| NULL | 失败 |

###### 2.1.4.1 发送数据
* 接口描述:
向camera设备发送语音或自定义数据。
```
int dataSend(const char *id, uint8_t *data, size_t len);
```
* 参数说明:
| 参数 | 类型 | 描述 | 输入/输出 |
|:-:|:-:|:-:|:-:|
| id | const char * | 目标camera在app端的唯一标识符 | 输入 |
| data | uint8_t * | 要发送的数据内容 | 输入 |
| len | size_t | 要发送的数据长度 | 输入 |

* 返回值
| 返回值 | 描述 |
|:-:|:-:|
| XP2PERRNONE | 成功 |
| XP2PERR* | 失败，对应错误码 |

###### 2.1.4.2 关闭语音发送服务
* 接口描述:
停止发送语音，并关闭发送服务。
```
int stopSendService(const char *id, void *req);
```
* 参数说明:
| 参数 | 类型 | 描述 | 输入/输出 |
|:-:|:-:|:-:|:-:|
| id | const char * | 目标camera在app端的唯一标识符 | 输入 |
| req | void * | 服务句柄，可传入NULL | 输入 |

* 返回值
| 返回值 | 描述 |
|:-:|:-:|
| XP2PERRNONE | 成功 |
| XP2PERR* | 失败，对应错误码 |

* 代码示例:
```
/* 启动语音发送服务 */
void *req = runSendService($id, NULL, true);
/* 循环发送 */
while (1) {
	dataSend($id, audio_data, data_len);
	usleep(100 * 1000);
}
/* 停止发送服务 */
stopSendService(id, req);
```

##### 2.1.5 P2P通道传输自定义数据
###### 2.1.5.0 同步方式发送自定义数据
* 接口描述:
发送信令消息给camera设备并等待回复。同步阻塞方式。
```
int postCommandRequestSync(const char *id, const unsigned char *command, size_t cmd_len, unsigned char **recv_buf, size_t *recv_len, uint64_t timeout_us);
```
* 参数说明:
| 参数 | 类型 | 描述 | 输入/输出 |
|:-:|:-:|:-:|:-:|
| id | const char * | 目标camera在app端的唯一标识符 | 输入 |
| command | const unsigned char * | 可以为任意格式字符或二进制数据 | 输入 |
| cmd_len | size_t | `command`参数长度 | 输入 |
| recv_buf | unsigned char ** | 用于存放camera回复的数据 | 输出 |
| recv_len | size_t * | camera回复的数据长度 | 输出 |
| timeout_us | uint64_t | 命令超时时间，单位为微秒，值为0时采用默认超时(7500ms左右) | 输出 |

* 返回值
| 返回值 | 描述 |
|:-:|:-:|
| XP2PERRNONE | 成功 |
| XP2PERR* | 失败，对应错误码 |

* 代码示例:
```
unsigned char *buf = NULL;
size_t len = 0;
/* 接收的数据填充在buf中，buf内存由SDK内部申请外部释放 */
int rc = postCommandRequestSync($id, "action=user_define&cmd=xxx", 
sizeof(action=user_define&cmd=custom_cmd), &buf, &len, 2*1000*1000);
if (rc != 0) {
  printf("post command request with async failed:%d\n", rc);
}
/* 释放内存 */
delete buf;
```
###### 2.1.5.1 异步方式发送自定义数据
* 接口描述:
发送信令消息给camera设备，不用等待回复。异步非阻塞方式。
```
int postCommandRequestWithAsync(const char *id, const unsigned char *command, size_t cmd_len);
```
* 参数说明:
| 参数 | 类型 | 描述 | 输入/输出 |
|:-:|:-:|:-:|:-:|
| id | const char * | 目标camera在app端的唯一标识符 | 输入 |
| command | const unsigned char * | 可以为任意格式字符或二进制数据 | 输入 |
| cmd_len | size_t | `command`参数长度 | 输入 |

* 返回值
| 返回值 | 描述 |
|:-:|:-:|
| XP2PERRNONE | 成功 |
| XP2PERR* | 失败，对应错误码 |

* 代码示例:
```
/* 设置消息接收回调 */
setUserCallbackToXp2p(_av_data_recv, _msg_notify);
int rc = postCommandRequestWithAsync($id, "action=user_define&cmd=xxx", sizeof(action=user_define&cmd=custom_cmd));
if (rc != 0) {
  printf("post command request with sync failed:%d\n", rc);
}

/* SDK接收到消息后调用注册的回调 */
char* _msg_notify(const char *id, XP2PType type, const char* msg) {
    if (type == XP2PTypeCmd) {
      //处理返回结果
      //回调中应避免耗时操作
      //多路p2p传输场景需根据回传的`id`判断对应的p2p通道,以做相应处理
    }
}
```

##### 2.1.6 主动关闭P2P通道
* 接口描述:
停止xp2p服务。
```
void stopService(const char *id);
```
* 参数说明:
| 参数 | 类型 | 描述 | 输入/输出 |
|:-:|:-:|:-:|:-:|
| id | const char * | 目标camera在app端的唯一标识符 | 输入 |

* 返回值
| 返回值 | 描述 |
|:-:|:-:|
| void | - |

* 代码示例:
  ```
  stopService($id);
  ```

##### 2.1.7 控制类消息回调
###### 2.1.7.0 P2P通道关闭回调
* 接口描述:
详见`2.1.0.1 控制类消息通知回调`

* 参数说明:
详见`2.1.0.1 控制类消息通知回调`

* 返回值:
详见`2.1.0.1 控制类消息通知回调`

* 代码示例:
```
char* _msg_notify(const char *id, XP2PType type, const char* msg) {
    if (type == XP2PTypeClose) {
      //p2p通道正常关闭
      //回调中应避免耗时操作
      //多路p2p传输场景需根据回传的`id`判断对应的p2p通道,以做相应处理
    }
}
```
###### 2.1.7.1 P2P通道错误断开回调
* 接口描述:
详见`2.1.0.1 控制类消息通知回调`

* 参数说明:
详见`2.1.0.1 控制类消息通知回调`

* 返回值:
详见`2.1.0.1 控制类消息通知回调`

* 代码示例:
```
char* _msg_notify(const char *id, XP2PType type, const char* msg) {
    if (type == XP2PTypeDisconnect) {
      //p2p通道错误断开
      //回调中应避免耗时操作
      //多路p2p传输场景需根据回传的`id`判断对应的p2p通道,以做相应处理
    }
}
```
###### 2.1.7.2 日志保存
* 接口描述:
详见`2.1.0.1 控制类消息通知回调`

* 参数说明:
详见`2.1.0.1 控制类消息通知回调`

* 返回值:
详见`2.1.0.1 控制类消息通知回调`

* 代码示例:
```
char* _msg_notify(const char *id, XP2PType type, const char* msg) {
    if (type == XP2PTypeLog) {
      //save or print log
      //多路p2p传输场景需根据回传的`id`判断对应的p2p通道,以做相应处理
    }
}
```
#### 2.2 使用Android aar库
接口详细说明可参考：[VideoSDK接口说明](https://github.com/tencentyun/iot-link-android/blob/master/sdk/video-link-android/doc/VideoSDK接口说明.md)

##### 2.2.0 用户回调
###### 2.2.0.0 媒体流数据接收回调
* 接口描述:
媒体流数据通知。该回调用于返回以裸流方式传输的媒体流数据。
```
fun avDataRecvHandle(id: String?, data: ByteArray?, len: Int)
{
	//媒体流数据接收
	//回调中应避免耗时操作
	//多路p2p传输场景需根据回传的`id`判断对应的p2p通道,以做相应处理
}
```
* 参数说明:
| 参数 | 类型 | 描述 | 输入/输出 |
|:-:|:-:|:-:|:-:|
| id | String | 目标camera在app端的唯一标识符 | 输出 |
| data | ByteArray | 接收到的媒体流数据 | 输出 |
| len | Int | 接收到的媒体流数据长度 | 输出 |

* 返回值
| 返回值 | 描述 |
|:-:|:-:|
| void | - |

###### 2.2.0.1 信令消息通知回调
* 接口描述:
信令消息通知。该回调用于返回以异步方式发送的信令请求结果。
```
fun commandRequest(id: String?, msg: String?)
{
	//信令消息通知
	//回调中应避免耗时操作
	//多路p2p传输场景需根据回传的`id`判断对应的p2p通道,以做相应处理
}
```
* 参数说明:
| 参数 | 类型 | 描述 | 输入/输出 |
|:-:|:-:|:-:|:-:|
| id | String | 目标camera在app端的唯一标识符 | 输出 |
| msg | String | 接收到的消息 | 输出 |

* 返回值
| 返回值 | 描述 |
|:-:|:-:|
| void | - |

###### 2.2.0.1 P2P连接异常断开通知回调
* 接口描述:
p2p通道错误断开。该回调用于通知p2p连接异常状况。
```
fun xp2pLinkError(id: String?, msg: String?)
{
	//p2p通道错误断开
	//回调中应避免耗时操作
	//多路p2p传输场景需根据回传的`id`判断对应的p2p通道,以做相应处理
}
```
* 参数说明:
| 参数 | 类型 | 描述 | 输入/输出 |
|:-:|:-:|:-:|:-:|
| id | String | 目标camera在app端的唯一标识符 | 输出 |
| msg | String | 附加消息 | 输出 |

* 返回值
| 返回值 | 描述 |
|:-:|:-:|
| void | - |

###### 2.2.0.2 P2P连接正常关闭通知回调
* 接口描述:
p2p通道正常关闭回调。该回调用于通知媒体流传输完成。
```
fun avDataCloseHandle(id: String?, msg: String?, errorCode: Int)
{
	//p2p通道正常关闭
	//回调中应避免耗时操作
	//多路p2p传输场景需根据回传的`id`判断对应的p2p通道,以做相应处理
}
```
* 参数说明:
| 参数 | 类型 | 描述 | 输入/输出 |
|:-:|:-:|:-:|:-:|
| id | String | 目标camera在app端的唯一标识符 | 输出 |
| msg | String | 附加消息 | 输出 |
| errorCode | Int | 状态码 | 输出 |

* 返回值
| 返回值 | 描述 |
|:-:|:-:|
| void | - |

##### 2.2.1 设置用户回调
* 接口描述:
设置用户回调函数。媒体流数据和控制类消息通过设置的回调函数返回。
```
public static void setCallback(XP2PCallback cb)
```
* 参数说明:
| 参数 | 类型 | 描述 | 输入/输出 |
|:-:|:-:|:-:|:-:|
| cb | XP2PCallback | p2p回调函数类 | 输入 |

* 返回值
| 返回值 | 描述 |
|:-:|:-:|
| void | - |

* 代码示例:
```
class VideoActivity : XP2PCallback {
	...
	XP2P.setCallback(this)
    ...
}
```

##### 2.2.2 P2P通道初始化
* 接口描述:
初始化xp2p服务。
```
public static void startServiceWithXp2pInfo(String id, String product_id, String device_name, String xp2p_info)
```
* 参数说明:
| 参数 | 类型 | 描述 | 输入/输出 |
|:-:|:-:|:-:|:-:|
| id | String | 目标camera在app端的唯一标识符 | 输入 |
| product_id | String | 目标camera产品信息 | 输入 |
| device_name | String | 目标camera设备名称 | 输入 |
| xp2p_info | String | xp2p信息 | 输入 |

* 返回值
| 返回值 | 描述 |
|:-:|:-:|
| void | - |

* 代码示例:
```
/* 从自建后台获取xp2p info */
String xp2p_info = getXP2PInfo(...)
/* 设置回调 */
XP2P.setCallback(this)
/* 初始化p2p */
XP2P.startServiceWithXp2pInfo($id, $product_id, $device_name, xp2p_info)
```

##### 2.2.3 P2P通道传输音视频裸流
###### 2.2.3.0 启动裸流接收服务
* 接口描述:
向camera设备请求媒体流，异步回调方式。
```
public static void startAvRecvService(String id, String params, boolean crypto)
```
* 参数说明:
| 参数 | 类型 | 描述 | 输入/输出 |
|:-:|:-:|:-:|:-:|
| id | String | 目标camera在app端的唯一标识符 | 输入 |
| params | String | 直播( `action=live` )或回放( `action=playback` )参数 | 输入 |
| crypto | boolean | 是否开启传输层加密 | 输入 |

* 返回值
| 返回值 | 描述 |
|:-:|:-:|
| void | - |

###### 2.2.3.1 停止裸流接收服务
* 接口描述:
停止裸流接收，并关闭接收服务。
```
public static int stopAvRecvService(String id, byte[] req)
```
* 参数说明:
| 参数 | 类型 | 描述 | 输入/输出 |
|:-:|:-:|:-:|:-:|
| id | String | 目标camera在app端的唯一标识符 | 输入 |
| req | byte[] | 服务句柄，当前版本传入`null` | 输入 |

* 返回值
| 返回值 | 描述 |
|:-:|:-:|
| 0 | 成功 |
| 其他 | 失败 |

* 代码示例:
```
/* 设置回调函数 */
XP2P.setCallback(this)
/* 开始请求数据 */
XP2P.startAvRecvService($id, "action=live", true)
/* 接收到数据后回调被触发 */
override fun avDataRecvHandle(id: String?, data: ByteArray?, len: Int)
{
	//裸流数据处理
	//回调中应避免耗时操作
	//多路p2p传输场景需根据回传的`id`判断对应的p2p通道,以做相应处理
}
/* 停止接收 */
XP2P.stopAvRecvService($id, null)
```

##### 2.2.4 接收FLV音视频流，使用ijkplayer播放
* 接口描述:
获取本地代理url。用于播放器直接通过url获取数据进行播放。
```
public static String delegateHttpFlv(String id)
```
* 参数说明:
| 参数 | 类型 | 描述 | 输入/输出 |
|:-:|:-:|:-:|:-:|
| id | String | 目标camera在app端的唯一标识符 | 输入 |

* 返回值
| 返回值 | 描述 |
|:-:|:-:|
| 本地代理url | 成功 |
| null | 失败 |

* 代码示例:
```
/* 加密方式观看直播(action=live)，回放(action=playback) */
val url = XP2P.delegateHttpFlv($id) + "ipc.flv?action=live"
/* 非加密方式观看直播(action=live)，回放(action=playback) */
val url = XP2P.delegateHttpFlv($id) + "ipc.flv?action=live&crypto=false"

mPlayer.dataSource = url
mPlayer.prepareAsync()
mPlayer.start()
```

##### 2.2.4 发送语音对讲数据
###### 2.2.4.0 启动语音发送服务
* 接口描述:
启动向camera设备发送语音或自定义数据服务。异步非阻塞方式。
```
public static void runSendService(String id, String params, boolean crypto)
```
* 参数说明:
| 参数 | 类型 | 描述 | 输入/输出 |
|:-:|:-:|:-:|:-:|
| id | String | 目标camera在app端的唯一标识符 | 输入 |
| params | String | 请求参数采用 `key1=value&amp;key2=value2` 格式，key不允许以下划线_开头，且key和value中间不能包含&amp;/+=特殊字符 | 输入 |
| crypto | boolean | 否开启传输层加密 | 输入 |

* 返回值
| 返回值 | 描述 |
|:-:|:-:|
| void | - |

###### 2.2.4.1 发送数据
* 接口描述:
向camera设备发送语音或自定义数据。
```
public static int dataSend(String id, byte[] data, int len)
```
* 参数说明:
| 参数 | 类型 | 描述 | 输入/输出 |
|:-:|:-:|:-:|:-:|
| id | String | 目标camera在app端的唯一标识符 | 输入 |
| data | byte[] | 要发送的数据内容 | 输入 |
| len | int | 要发送的数据长度 | 输入 |

* 返回值
| 返回值 | 描述 |
|:-:|:-:|
| void | - |

###### 2.2.4.2 关闭语音发送服务
* 接口描述:
向camera设备发送语音或自定义数据。
```
public static int stopSendService(String id, byte[] req)
```
* 参数说明:
| 参数 | 类型 | 描述 | 输入/输出 |
|:-:|:-:|:-:|:-:|
| id | String | 目标camera在app端的唯一标识符 | 输入 |
| req | byte[] | 服务句柄，当前版本可传入`null` | 输入 |

* 返回值
| 返回值 | 描述 |
|:-:|:-:|
| 0 | 成功 |
| 其他 | 失败 |

* 代码示例:
```
/* 开启语音发送服务 */
XP2P.runSendService($id, "", true)
while(!stop) {
    /* 采集语音数据并发送 */
    byte[] flvData = flvPacker.getFLV(data);
    XP2P.dataSend(deviceId, flvData, flvData.length);
}
/* 发送完成后停止服务 */
XP2P.stopSendService($id, null)
```

##### 2.2.5 P2P通道传输自定义数据
###### 2.2.5.0 同步方式发送自定义数据
* 接口描述:
发送信令消息给camera设备并等待回复。同步阻塞方式。
```
public static String postCommandRequestSync(String id, byte[] command, long cmd_len, long timeout_us)
```
* 参数说明:
| 参数 | 类型 | 描述 | 输入/输出 |
|:-:|:-:|:-:|:-:|
| id | String | 目标camera在app端的唯一标识符 | 输入 |
| command | byte[] | 可以为任意格式字符或二进制数据 | 输入 |
| cmd_len | long | `command` 参数长度 | 输入 |
| timeout_us | long | 命令超时时间，单位为微秒，值为0时采用默认超时(7500ms左右) | 输入 |

* 返回值
| 返回值 | 描述 |
|:-:|:-:|
| camera回复的数据 | 成功 |
| 空值 | 失败 |

* 代码示例:
```
val cmd = "action=inner_define&amp;cmd=xxx".toByteArray()
val ret = XP2P.postCommandRequestSync($id, cmd, cmd.size.toLong(), 2*1000*1000)
L.e("--------ret:----$ret--\n")
```

###### 2.2.5.1 异步方式发送自定义数据
* 接口描述:
发送信令消息给camera设备，不用等待回复。异步非阻塞方式。
```
public static int postCommandRequestWithAsync(String id, byte[] command, long cmd_len)
```
* 参数说明:
| 参数 | 类型 | 描述 | 输入/输出 |
|:-:|:-:|:-:|:-:|
| id | String | 目标camera在app端的唯一标识符 | 输入 |
| command | byte[] | 可以为任意格式字符或二进制数据 | 输入 |
| cmd_len | long | `command` 参数长度 | 输入 |

* 返回值
| 返回值 | 描述 |
|:-:|:-:|
| 0 | 成功 |
| 其他 | 失败 |

* 代码示例:
```
/* 设置回调 */
XP2P.setCallback(this)
val cmd = "action=user_define&amp;cmd=xxx".toByteArray()
XP2P.postCommandRequestWithAsync($id, cmd, cmd.size.toLong())
override fun commandRequest(id: String?, msg: String?, len: Int)
{
	//处理回复消息
}
```

##### 2.2.6 主动关闭P2P通道
* 接口描述:
停止xp2p服务。
```
public static void stopService(String id)
```
* 参数说明:
| 参数 | 类型 | 描述 | 输入/输出 |
|:-:|:-:|:-:|:-:|
| id | String | 目标camera在app端的唯一标识符 | 输入 |

* 返回值
| 返回值 | 描述 |
|:-:|:-:|
| void | - |

* 代码示例:
```
override fun onDestroy() {
      super.onDestroy()
      mPlayer.release()
      XP2P.stopService($id)
}
```

### APP接入SDK说明

第三方App在接入Video SDK时，建议将`secretId`和`secretKey`保存到自建后台，不推荐将这两个信息保存至App端; 而SDK需要的xp2p info需要App侧从自己的业务后台获取；获取到xp2p info后，可以通过上述的`startServiceWithXp2pInfo`接口将该info传给SDK，示例代码如下：
```
...

String xp2p_info = getXP2PInfo(...) // 从自建后台获取xp2p info
XP2P.setCallback(this)
XP2P.startServiceWithXp2pInfo(id, product_id, device_name, xp2p_info)
```
