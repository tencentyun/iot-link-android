# SDK错误码及常见问题说明

## 错误码
| 错误码 | 十进制值 | 含义 |
| :---: | :---: | :---: |
| XP2PERRNONE | 0 | 成功 |
| XP2PERRINITPRM | -1000 | 入参为空 |
| XP2PERRGETXP2PINFO | -1001 | SDK内部请求xp2p info失败 |
| XP2PERRPROXYINIT | -1002 | 本地p2p代理初始化失败 |
| XP2PERRUNINIT | -1003 | 数据接收或发送服务未初始化 |
| XP2PERRENCRYPT | -1004 | 数据加密失败 |
| XP2PERRTIMEOUT | -1005 | 请求超时 |
| XP2PERRERROR | -1006 | 请求错误 |
| XP2PERRVERSION | -1007 | 设备版本过低 |
| XP2PERRAPPLICATION | -1008 | 服务application初始化失败 |
| XP2PERRREQUEST | -1009 | 服务request初始化失败 |
| XP2PERRDETECTNOREADY | -1010 | p2p探测未完成 |
| XP2PERRP2PININED | -1011 | 当前id对应的p2p已完成初始化 |
| XP2PERRP2PUNININ | -1012 | 当前id对应的p2p未初始化 |
| XP2PERRNEWMEMERY | -1013 | 内存申请失败 |
| XP2PERRXP2PINFORULE | -1014 | 获取到的xp2p info格式错误 |
| XP2PERRXP2PINFODECRYPT | -1015 | 获取到的xp2p info解码失败 |
| XP2PERRPROXYLISTEN | -1016 | 本地代理监听端口失败 |
| XP2PERRCLOUDEMOTY | -1017 | 云端返回空数据 |
| XP2PERRJSONPARSE | -1018 | json解析失败 |
| XP2PERRSERVICENOTRUN | -1019 | 当前id对应的服务没有在运行 |
| XP2PERRCLIENTNULL | -1020 | 从map中取出的client为空 |

## 常见问题
### P2P初始化接口失败
* 当前传入的id已经完成了P2P初始化，返回错误码:`XP2PERRP2PININED`，需要先销毁P2P资源或使用新id。log如下
```shell
p2p service is running with id:cam01, please stop it first
```
* 向云端请求xp2p info时云端回复数据为空，返回错误码:`XP2PERRGETXP2PINFO`。需排查设置的设备三元组和云api账号信息是否正确。log如下
```shell
request xp2p_info failed, errmsg:empty reply from cloud
```
* 向云端请求xp2p info时云端回复数据中没有指定json字段，返回错误码:`XP2PERRGETXP2PINFO`。需排查设备是否上报了xp2p info到云端。log如下
```shell
request xp2p_info failed, errmsg:parse reply error
```
* 请求到的设备xp2p info格式错误，返回错误码:`XP2PERRXP2PINFORULE`。一般为设备SDK版本过低所致。log如下
```shell
remote xp2p_info rule wrong:$xp2p_info
```
* 设备SDK版本与APP SDK版本不匹配，返回错误码:`XP2PERRVERSION`。需升级设备SDK。log如下
```shell
The xp2p_device_sdk is low, Please upgrade the device version to at least $version
```
* 解码获取到的设备xp2p info失败，返回错误码:`XP2PERRXP2PINFODECRYPT`。需排查传入的设备三元组信息是否正确。log如下
```shell
decrypt xp2p_info error
```
* 获取到的设备xp2p info信息无效，返回错误码:`XP2PERRPROXYINIT`。需检查设备端网络，确保网络正常。log如下
```shell
remote xp2pinfo is invalid
```
* 本地代理无法监听tcp端口，返回错误码:`XP2PERRPROXYINIT`。需检查APP端网络，确保网络正常。log如下
```shell
proxy listen failed!
```
或
```shell
cannot listen a port
```

### 启动数据传输服务失败
* P2P未成功初始化便启动数据传输服务，返回错误码:`XP2PERRP2PUNININ`。需确保P2P初始化成功后再进行后续操作。log如下
```shell
p2p service is not running with id:cam01, please run it first
```
* P2P探测未完成便启动数据传输服务，返回错误码:`XP2PERRDETECTNOREADY`。需等待`ready`回调触发后再进行后续操作。log如下
```shell
p2p detect is not ready, state:0
```
* 创建Application失败。需检查APP网络环境，确保网络正常。log如下
```shell
create AudioStream application failed
```
* 创建Request失败。需检查APP网络环境，确保网络正常。log如下
```shell
create AudioStream request failed
```

### 语音数据发送失败
* 语音发送服务未启动，返回错误码:`XP2PERRUNINIT`。需先启动语音发送接口。log如下
```shell
connot found request with service:AudioStream
```
* 语音服务已关闭，返回错误码:`XP2PERRUNINIT`。需重新启动语音发送服务或停止语音发送接口调用。log如下
```shell
application is invalid
```

### 无法收到回调消息
* 需要注册回调函数到SDK