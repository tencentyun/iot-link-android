## android sdk接口使用说明

### 版本信息
* 版本:v1.0
* 修改内容:
    * 初始版本
* 修改时间:2021.01.14
---------------------------

### 接口说明
public static void startServiceWithXp2pInfo(String xp2p_info);
* 函数说明:初始化app上的p2p通道
* 参数说明:
    * xp2p_info:获取的camera端生成的xp2p信息
* 返回值:无返回值
* 说明:
    * 该版本暂时传入空即可

public static void stopService();
* 函数说明:退出xp2p并释放对应的资源
* 参数说明:⽆参数
* 返回值:⽆返回值

public static String delegateHttpFlv();
* 函数说明:获取p2p通道建立后提供的本地url
* 参数说明:无参数
* 返回值:
    * 成功:返回构建的url
    * 失败:返回空字符串
* 返回值说明:
    * 返回的url是标准url,使用前需拼接成具体请求的url
    * 如需请求直播数据,则在返回的url后拼接`ipc.flv?action=live`
    * 如需请求本地录像数据,则在返回的url后拼接`ipc.flv?action=playback`

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
* 函数说明:启动接收数据服务
* 参数说明:
    * cmd:直播`action=live`或回放参数`action=playback`
* 返回值:无返回值

public static int stopAvRecvService(byte[] data);
* 函数说明:停止接收数据服务
* 参数说明:
    * data:启动的服务句柄,当前版本传入空即可
* 返回值:
    * 成功:0
    * 失败:错误码

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
