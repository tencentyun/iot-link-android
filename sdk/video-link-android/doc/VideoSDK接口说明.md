## android sdk接口使用说明

---------------------------

### 接口说明
public static void startServiceWithXp2pInfo(String id, String product_id, String device_name, String xp2p_info_attr, String xp2p_info);
* 函数说明: 初始化xp2p服务
* 参数说明:
    * id: 目标camera在app端的唯一标识符
    * product_id: 产品ID
    * device_name: 设备名称
    * xp2p_info_attr: xp2p属性，当前版本固定为`_sys_xp2p_info`,若xp2p_info已获取，该参数可传入null
    * xp2p_info: 获取的camera端生成的xp2p信息,由接口获取则传入空
* 返回值:
    * 成功: 0

static native String delegateHttpFlv(String id);
* 函数说明: 获取本地请求数据的标准http url,可使用该url请求设备端数据
* 参数说明:
    * id: 目标camera在app端的唯一标识符
* 返回值: 本地代理的url

public static void stopService(String id);
* 函数说明: 退出xp2p并释放对应的资源
* 参数说明:
    * id: 目标camera在app端的唯一标识符
* 返回值: ⽆返回值

public static void dataSend(String id, byte[] data, int len);
* 函数说明:向camera设备发送语音或自定义数据
* 参数说明:
    * id: 目标camera在app端的唯一标识符
    * data: 要发送的字节流
    * len: 要发送的数据长度
* 返回值: 无返回值

public static void setQcloudApiCred(String id, String key);
* 函数说明: 设置云API secret_id和secret_key
* 参数说明:
    * id: 云API secret_id信息
    * key: 云API seret_key信息
* 返回值: 无返回值

public static void runSendService(String id, String cmd, boolean crypto);
* 函数说明: 启动向camera设备发送语音或自定义数据服务，异步非阻塞方式
* 参数说明:
    * id: 目标camera在app端的唯一标识符
    * cmd: 请求参数采用 key1=value&key2=value2 格式，key不允许以下划线_开头，且key和value中间不能包含&/+=特殊字符
    * crypto: 是否开启传输层加密，如果关闭(crypto=false)，则建议用户在应用层加密，否则有安全风险
* 返回值: 无返回值

public void stopSendService(String id, byte[] srvice_id);
* 函数说明:关闭语音发送传输
* 参数说明:
    * id: 目标camera在app端的唯一标识符
    * srvice_id: 要停止的服务句柄,该版本传入空即可
* 返回值: 无返回值

public static void setCallback(XP2PCallback callback);
* 函数说明: 设置java到C++的回调object
* 参数说明:
    * callback: java层回调函数object
* 返回值: 无返回值

public static String postCommandRequestSync(String id, byte[] command, long cmd_len, long timeout_us);
* 函数说明: 发送信令消息给camera设备并等待回复，同步阻塞方式
* 参数说明:
    * id: 目标camera在app端的唯一标识符
    * command: 可以为任意格式字符或二进制数据(格式必须为`action=user_define&cmd=xxx`)，长度由cmd_len提供，建议在16KB以内，否则会影响实时性
    * cmd_len: command长度
    * timeout: 命令超时时间，单位为微秒，值为0时采用默认超时(7500ms左右)
* 返回值:
    * 成功: 设备端响应请求返回的数据
    * 失败: 空字符串

public static int postCommandRequestWithAsync(String id, byte[] command, long cmd_len);
* 函数说明: 发送信令消息给camera设备,camera回复的数据由注册的回调函数返回,异步非阻塞方式;使用该方法首先需调用setCallback()注册回调
* 参数说明:
    * id: 目标camera在app端的唯一标识符
    * command: 可以为任意格式字符或二进制数据(格式必须为`action=user_define&cmd=xxx`)，长度由cmd_len提供，建议在16KB以内，否则会影响实时性
    * cmd_len: command长度
* 返回值:
    * 成功:0
    * 失败: 错误码

public static void startAvRecvService(String id, String params, boolean crypto);
* 函数说明: 向camera设备请求媒体流,异步回调方式;使用该方法首先需调用setCallback()注册回调
* 参数说明:
    * id: 目标camera在app端的唯一标识符
    * params: 直播(`action=live`)或回放(`action=playback`)参数
    * crypto: 是否开启传输层加密，如果关闭(crypto=false)，则建议用户在应用层加密，否则有安全风险
* 返回值: 无返回值
* 说明: 调用该接口需要在其回调函数中处理接收的数据,且该接口与接口`delegateHttpFlv()`的使用互斥,不可同时使用二者

public static int stopAvRecvService(String id, byte[] data);
* 函数说明: 关闭媒体流传输
* 参数说明:
    * id: 目标camera在app端的唯一标识符
    * data: 启动的服务句柄,当前版本传入空即可
* 返回值:
    * 成功: 0
    * 失败: 错误码


### 废弃接口
public static String getComandRequestWithSync(String cmd, long timeout);
* 函数说明:以阻塞方式向设备端发送请求资源或控制命令
* 参数说明:
    * cmd:命令参数,格式:`action=user_define&cmd=xxx`
    * timeout:超时时间,单位us
* 返回值:
    * 成功:设备端响应请求返回的数据
    * 失败:空字符串

public static void getCommandRequestWithAsync(String cmd);
* 函数说明:以非阻塞方式向设备端发送请求资源或控制命令,使用该方法首先需调用setCallback()注册回调
* 参数说明:
    * cmd:命令参数,格式:`action=user_define&cmd=xxx`
* 返回值:无返回值


### 回调函数说明
override fun avDataRecvHandle(id: String?, data: ByteArray?, len: Int);
* 函数说明: 接收音视频数据回调,收到设备端音视频数据后被调用
* 参数说明:
    * id: 回传`startServiceWithXp2pInfo`接口中的`id`
    * data: 接收到的音视频数据
    * len: 接收到的音视频数据长度
* 注意: 该回调中不可做耗时操作

override fun avDataCloseHandle(id: String?, msg: String?, code: Int);
* 函数说明: p2p通道关闭回调,设备端关闭p2p通道后被调用
* 参数说明:
    * id: 回传`startServiceWithXp2pInfo`接口中的`id`
    * msg: 附加描述信息,当前版本可不用处理
    * code: 附加状态信息,当前版本可不用处理
* 注意: 该回调中不可做耗时操作

override fun commandRequest(id: String?, msg: String?, len: Int);
* 函数说明: 以非阻塞方式发送命令回调,收到设备端回应后被调用
* 参数说明:
    * id: 回传`startServiceWithXp2pInfo`接口中的`id`
    * msg: 设备端回应的消息,json数组格式
    * len: 设备端回应的消息长度
* 注意: 该回调中不可做耗时操作

override fun xp2pLinkError(id: String?, msg: String?);
* 函数说明: p2p通道关闭通知回调
* 参数说明:
    * id: 回传`startServiceWithXp2pInfo`接口中的`id`
    * msg: 附加说明,json格式
* 注意: 该回调中不可做耗时操作


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