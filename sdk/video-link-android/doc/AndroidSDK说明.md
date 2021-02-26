## android sdk接口使用说明

---------------------------

### 接口说明
public static void startServiceWithXp2pInfo(String xp2p_info);
* 函数说明:初始化app上的p2p通道
* 参数说明:
    * xp2p_info:获取的camera端生成的xp2p信息
* 返回值:无返回值
* 说明:
    * 该版本暂时传入空即可

static native String delegateHttpFlv();
* 函数说明:获取本地请求数据的标准http url,可使用该url请求设备端数据
* 参数说明:无参数
* 返回值:本地代理的url

public static void stopService();
* 函数说明:退出xp2p并释放对应的资源
* 参数说明:⽆参数
* 返回值:⽆返回值

public static void dataSend(byte[] data, int len);
* 函数说明:通过建立的p2p通道发送数据
* 参数说明:
    * data:要发送的字节流
    * len:要发送的数据长度
* 返回值:无返回值

public static void setQcloudApiCred(String id, String key);
* 函数说明:设置云API secret_id和secret_key
* 参数说明:
    * id:云API secret_id信息
    * key:云API seret_key信息
* 返回值:无返回值

public static void setDeviceInfo(String id, String name);
* 函数说明:设置设备信息
* 参数说明:
    * id:产品id
    * name:设备名称
* 返回值:无返回值

public static void setXp2pInfoAttributes(String attributes);
* 函数说明:设置物模型中xp2p信息属性名称
* 参数说明:
    * attributes:属性名称,该版本传入`_sys_xp2p_info`
* 返回值:无返回值

public static void runSendService();
* 函数说明:启动p2p数据发送服务
* 参数说明:无参数
* 返回值:无返回值

public void stopSendService(byte[] srvice_id);
* 函数说明:停止数据发送服务
* 参数说明:
    * srvice_id:要停止的服务句柄,该版本传入空即可
* 返回值:无返回值

public static void setCallback(XP2PCallback callback);
* 函数说明:设置java到C++的回调object
* 参数说明:
    * callback:java层回调函数object
* 返回值:无返回值

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

public static void startAvRecvService(String cmd);
* 函数说明:启动接收数据服务,使用该方法首先需调用setCallback()注册回调
* 参数说明:
    * cmd:直播`action=live`或回放参数`action=playback`
* 返回值:无返回值
* 说明:调用该接口需要在其回调函数中处理接收的数据,且该接口与接口`delegateHttpFlv()`的使用互斥,不可同时使用二者

public static int stopAvRecvService(byte[] data);
* 函数说明:停止接收数据服务
* 参数说明:
    * data:启动的服务句柄,当前版本传入空即可
* 返回值:
    * 成功:0
    * 失败:错误码

### 回调函数说明
override fun avDataRecvHandle(data: ByteArray?, len: Int);
* 函数说明:接收音视频数据回调,收到设备端音视频数据后被调用
* 参数说明:
    * data:接收到的音视频数据
    * len:接收到的音视频数据长度
* 注意:该回调中不可做耗时操作

override fun avDataCloseHandle(msg: String?, code: Int);
* 函数说明:p2p通道关闭回调,设备端关闭p2p通道后被调用
* 参数说明:
    * msg:附加描述信息,当前版本可不用处理
    * code:附加状态信息,当前版本可不用处理
* 注意:该回调中不可做耗时操作

override fun commandRequest(msg: String?, len: Int);
* 函数说明:以非阻塞方式发送命令回调,收到设备端回应后被调用
* 参数说明:
    * msg:设备端回应的消息,json数组格式
    * len:设备端回应的消息长度
* 注意:该回调中不可做耗时操作


### 附带说明
* 函数接口调用顺序:
    * setQcloudApiCred
    * setDeviceInfo
    * setXp2pInfoAttributes
    * setCallback
    * startServiceWithXp2pInfo
    * runSendService:如果没有发送需求可不调用该接口
    * dataSend:如果没有发送需求可不调用该接口
    * stopSendService:该接口暂时不用调用
    * stopService


### APP接入SDK说明
第三方App在接入Video SDK时，建议将`secretId`和`secretKey`保存到自建后台，不推荐将这两个信息保存至App端，而SDK需要的xp2p info需要App侧从自己的业务后台获取；获取到xp2p info后，可以通过上述的`startServiceWithXp2pInfo`接口将该info传给SDK，示例代码如下：
```
String xp2p_info = getXP2PInfo(...) // 从自建后台获取xp2p info
XP2P.setCallback(this)
XP2P.startServiceWithXp2pInfo(xp2p_info)
```