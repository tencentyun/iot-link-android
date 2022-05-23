## android sdk接口使用说明

---------------------------

### 接口说明
1. 初始化xp2p服务接口
> int startService(String id, String product_id, String device_name)

| 参数 | 类型 | 描述 |
|:-|:-|:-|
| id | String | 目标camera在app端的唯一标识符,可以使用产品信息和设备名称组合,如:"$product/$device_name" |
| product_id | String | 产品ID |
| device_name | String | 设备名称 |

| 返回值 | 描述 |
|:-|:-|
| int | 错误码 |


2. 设置云API secret_id、secret_key以及xp2pInfo
> int setParamsForXp2pInfo(String id, String secretId, String secretKey, String xp2pInfo)

| 参数 | 类型 | 描述 |
|:-|:-|:-|
| id | String | 目标camera在app端的唯一标识符,可以使用产品信息和设备名称组合,如:"$product/$device_name" |
| secretId | String | 云API secret_id信息 |
| secretKey | String | 云API seret_key信息 |
| xp2p_info | String | 获取的camera端生成的xp2p信息,由该接口获取则传入空 |

| 返回值 | 描述 |
|:-|:-|
| int | 错误码 |

3. 获取本地请求数据的标准http url,可使用该url请求设备端数据
> String delegateHttpFlv(String id)

> 说明: 该接口与startAvRecvService的使用互斥,不可同时使用二者

| 参数 | 类型 | 描述 |
|:-|:-|:-|
| id | String | 目标camera在app端的唯一标识符,可以使用产品信息和设备名称组合,如:"$product/$device_name" |

| 返回值 | 描述 |
|:-|:-|
| String | 本地代理的url |

4. 退出xp2p并释放对应的资源
> void stopService(String id)

| 参数 | 类型 | 描述 |
|:-|:-|:-|
| id | String | 目标camera在app端的唯一标识符,可以使用产品信息和设备名称组合,如:"$product/$device_name" |

5. 向camera设备发送语音或自定义数据
> void dataSend(String id, byte[] data, int len)

| 参数 | 类型 | 描述 |
|:-|:-|:-|
| id | String | 目标camera在app端的唯一标识符,可以使用产品信息和设备名称组合,如:"$product/$device_name" |
| data | byte[] | 要发送的字节流 |
| len | int | 要发送的数据长度 |


6. 启动向camera设备发送语音或自定义数据服务，异步非阻塞方式
> void runSendService(String id, String cmd, boolean crypto)

| 参数 | 类型 | 描述 |
|:-|:-|:-|
| id | String | 目标camera在app端的唯一标识符,可以使用产品信息和设备名称组合,如:"$product/$device_name" |
| cmd | String | 请求参数采用 key1=value&key2=value2 格式，key不允许以下划线_开头，且key和value中间不能包含&/+=特殊字符 |
| crypto | boolean| 是否开启传输层加密，如果关闭(crypto=false)，则建议用户在应用层加密，否则有安全风险 |


7. 关闭语音发送传输
> void stopSendService(String id, byte[] srvice_id)

| 参数 | 类型 | 描述 |
|:-|:-|:-|
| id | String | 目标camera在app端的唯一标识符,可以使用产品信息和设备名称组合,如:"$product/$device_name" |
| srvice_id | byte[] | 要停止的服务句柄,该版本传入空即可 |


8. 设置java到C++的回调object
> void setCallback(XP2PCallback callback)

| 参数 | 类型 | 描述 |
|:-|:-|:-|
| callback | XP2PCallback | java层回调函数object |


9. 发送信令消息给camera设备并等待回复，同步阻塞方式
> String postCommandRequestSync(String id, byte[] command, long cmd_len, long timeout_us)

| 参数 | 类型 | 描述 |
|:-|:-|:-|
| id | String | 目标camera在app端的唯一标识符,可以使用产品信息和设备名称组合,如:"$product/$device_name" |
| command | byte[] | 可以为任意格式字符或二进制数据(格式必须为`action=user_define&cmd=xxx`,需要传输的数据跟在`cmd=`后面)，长度由cmd_len提供，建议在16KB以内，否则会影响实时性 |
| cmd_len | long | command长度 |
| timeout_us | long | 命令超时时间，单位为微秒，值为0时采用默认超时(7500ms左右) |

| 返回值 | 描述 |
|:-|:-|
| String | 设备端响应请求返回的数据(失败则返回空字符串) |


10. 发送信令消息给camera设备,camera回复的数据由注册的回调函数返回,异步非阻塞方式;使用该方法首先需调用setCallback()注册回调
> int postCommandRequestWithAsync(String id, byte[] command, long cmd_len)

| 参数 | 类型 | 描述 |
|:-|:-|:-|
| id | String | 目标camera在app端的唯一标识符,可以使用产品信息和设备名称组合,如:"$product/$device_name" |
| command | byte[] | 可以为任意格式字符或二进制数据(格式必须为`action=user_define&cmd=xxx`,需要传输的数据跟在`cmd=`后面)，长度由cmd_len提供，建议在16KB以内，否则会影响实时性 |
| cmd_len | long | command长度 |

| 返回值 | 描述 |
|:-|:-|
| int | 成功：0，失败：错误码 |


11. 向camera设备请求媒体流,异步回调方式;使用该方法首先需调用setCallback()注册回调
> void startAvRecvService(String id, String params, boolean crypto)

> 说明: 调用该接口需要在其回调函数中处理接收的数据,且该接口与接口`delegateHttpFlv()`的使用互斥,不可同时使用二者

| 参数 | 类型 | 描述 |
|:-|:-|:-|
| id | String | 目标camera在app端的唯一标识符,可以使用产品信息和设备名称组合,如:"$product/$device_name" |
| params | String | 直播(`action=live`)或回放(`action=playback`)参数 |
| crypto | boolean | 是否开启传输层加密，如果关闭(crypto=false)，则建议用户在应用层加密，否则有安全风险 |


12. 关闭媒体流传输
> int stopAvRecvService(String id, byte[] data);

| 参数 | 类型 | 描述 |
|:-|:-|:-|
| id | String | 目标camera在app端的唯一标识符,可以使用产品信息和设备名称组合,如:"$product/$device_name" |
| data | byte[] | 启动的服务句柄,当前版本传入空即可 |

| 返回值 | 描述 |
|:-|:-|
| int | 成功：0，失败：错误码 |

13. 初始化xp2p局域网服务接口
> int startLanService(String id, String product_id, String device_name, String host, String port);

| 参数 | 类型 | 描述 |
|:-|:-|:-|
| id | String | 目标camera在app端的唯一标识符,可以使用产品信息和设备名称组合,如:"$product/$device_name" |
| product_id | String | 产品ID |
| device_name | String | 设备名称 |
| host | String | 设备在局域网的ip地址 |
| port | String | 设备在局域网的端口号 |

14. 获取本地请求数据的局域网标准http url,可使用该url请求设备端数据
> String getLanUrl(String id);

| 参数 | 类型 | 描述 |
|:-|:-|:-|
| id | String | 目标camera在app端的唯一标识符,可以使用产品信息和设备名称组合,如:"$product/$device_name" |

15. 获取局域网内本地代理的端口号
> int getLanProxyPort(String id);

16. 发送一次探测广播, body 为广播内容

> void startSendBroadcast(WlanDetectBody body);

| 参数 | 类型           | 描述         |
| :--- | :------------- | :----------- |
| body | WlanDetectBody | 探测包体内容 |

WlanDetectBody 定义如下

| 参数        | 类型            | 描述                              |
| :---------- | :-------------- | :-------------------------------- |
| clientToken | String          | 自定义 token 用于验证接收的响应包 |
| productId   | String          | 发送指定产品 ID 的广播包          |
| deviceNames | List < String > | 发送指定设备名的广播包            |

17. 发送多次探测广播, body 为广播内容，times 为尝试次数

> void startSendBroadcast(WlanDetectBody body,  int times);

| 参数  | 类型           | 描述                 |
| :---- | :------------- | :------------------- |
| body  | WlanDetectBody | 探测包体内容         |
| times | int            | 尝试发送探测包的次数 |

WlanDetectBody 定义如下

| 参数        | 类型            | 描述                              |
| :---------- | :-------------- | :-------------------------------- |
| clientToken | String          | 自定义 token 用于验证接收的响应包 |
| productId   | String          | 发送指定产品 ID 的广播包          |
| deviceNames | List < String > | 发送指定设备名的广播包            |

18. 设置广播的端口

> void setPort(int port);

| 参数 | 类型 | 描述   |
| :--- | :--- | :----- |
| port | int  | 端口号 |

19. 中断发送广播，用于中断正在发送指定次数的广播

> void clearAllTask();

20. 设置探测响应包监听器

> void setOnWlanDevicesDetectedCallback(OnWlanDevicesDetectedCallback onWlanDevicesDetectedCallback);

| 参数                          | 类型                          | 描述             |
| :---------------------------- | :---------------------------- | :--------------- |
| onWlanDevicesDetectedCallback | OnWlanDevicesDetectedCallback | 广播响应包监听器 |

21. 设置是否输出P2P日志(默认情况下，控制台日志以及日志文件输出是打开状态)

> void setLogEnable(boolean console, boolean file);

> 注意: 在start service之前调用该接口, 可以和XP2P.setCallback(this)接口一起调用

| 参数  | 类型           | 描述                 |
| :---- | :------------- | :------------------- |
| console  | boolean | 控制台日志输出开关         |
| file     | boolean | 日志文件输出开关 |

### 废弃接口

~~public static String getComandRequestWithSync(String cmd, long timeout);~~

* ~~函数说明:以阻塞方式向设备端发送请求资源或控制命令~~
* ~~参数说明:~~
    * ~~cmd:命令参数,格式:`action=user_define&cmd=xxx`~~
    * ~~timeout:超时时间,单位us~~
* ~~返回值:~~
    * ~~成功:设备端响应请求返回的数据~~
    * ~~失败:空字符串~~

~~public static void getCommandRequestWithAsync(String cmd);~~
* ~~函数说明:以非阻塞方式向设备端发送请求资源或控制命令,使用该方法首先需调用setCallback()注册回调~~
* ~~参数说明:~~
    * ~~cmd:命令参数,格式:`action=user_define&cmd=xxx`~~
* ~~返回值:无返回值~~


### 回调函数说明

1. 接收音视频数据回调,收到设备端音视频数据后被调用
> override fun avDataRecvHandle(id: String?, data: ByteArray?, len: Int)

> 注意: 该回调中不可做耗时操作

| 参数 | 类型 | 描述 |
|:-|:-|:-|
| id | String | 回传`startServiceWithXp2pInfo`接口中的`id` |
| data | ByteArray | 接收到的音视频数据 |
| len | Int | 接收到的音视频数据的长度 |


2. p2p通道关闭回调,设备端关闭p2p通道后被调用
> override fun avDataCloseHandle(id: String?, msg: String?, code: Int)

> 注意: 该回调中不可做耗时操作

| 参数 | 类型 | 描述 |
|:-|:-|:-|
| id | String | 回传`startServiceWithXp2pInfo`接口中的`id` |
| msg | String | 附加描述信息,当前版本可不用处理 |
| code | Int | 附加描述信息,当前版本可不用处理 |


3. 以非阻塞方式发送命令回调,收到设备端回应后被调用
> override fun commandRequest(id: String?, msg: String?, len: Int)

> 注意: 该回调中不可做耗时操作

| 参数 | 类型 | 描述 |
|:-|:-|:-|
| id | String | 回传`startServiceWithXp2pInfo`接口中的`id` |
| msg | String | 设备端回应的消息,json数组格式 |
| len | Int | 设备端回应的消息长度 |

4. p2p通道关闭通知回调
> override fun xp2pLinkError(id: String?, msg: String?)

> 注意: 该回调中不可做耗时操作

| 参数 | 类型 | 描述 |
|:-|:-|:-|
| id | String | 回传`startServiceWithXp2pInfo`接口中的`id` |
| msg | String | 附加说明,json格式 |

5. 设备向app发送自定义消息，该回调的返回值表示app向设备端回复的消息

> override fun onDeviceMsgArrived(id: String?, data: ByteArray?, len: Int): String

6. 设备对 app 探测包的响应包

> override fun onMessage(version: String, resp: WlanRespBody): Boolean

| 参数    | 类型         | 描述     |
| :------ | :----------- | :------- |
| version | String       | 版本号   |
| resp    | WlanRespBody | 响应包体 |

WlanRespBody 定义如下

| 参数        | 类型             | 描述                     |
| :---------- | :--------------- | :----------------------- |
| method      | String           | 消息类型                 |
| clientToken | String           | 消息标识，与发送消息一致 |
| timestamp   | long             | 消息发送的时间           |
| params      | DeviceServerInfo | 设备信息                 |
| code        | int              | 状态码，一般是0          |
| status      | String           | 扩展使用                 |

DeviceServerInfo 定义如下

| 参数       | 类型   | 描述           |
| :--------- | :----- | :------------- |
| deviceName | String | 设备的名称     |
| address    | String | 设备的 IP 地址 |
| port       | int    | 设备的端口号   |

### 附带说明

* 函数接口调用顺序:
    * setQcloudApiCred
    * setCallback
    * startServiceWithXp2pInfo
    * runSendService:如果没有发送需求可不调用该接口
    * dataSend:如果没有发送需求可不调用该接口
    * stopSendService:该接口暂时不用调用
    * stopService


### APP接入SDK说明
第三方App在接入Video SDK时，建议将`secretId`和`secretKey`保存到自建后台，不推荐将这两个信息保存至App端; 而SDK需要的xp2p info需要App侧从自己的业务后台获取；获取到xp2p info后，可以通过上述的`startServiceWithXp2pInfo`接口将该info传给SDK，示例代码如下：
```
...
String xp2p_info = getXP2PInfo(...) // 从自建后台获取xp2p info
XP2P.setCallback(this)
XP2P.startServiceWithXp2pInfo(id, product_id, device_name, "", xp2p_info)
```