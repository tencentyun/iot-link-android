# SDK信令使用说明

## 信令使用场景
* app需要向ipc设备发送控制(镜头移动等)、查询(录像列表等)等命令以操控、获取ip设备信息等需求可以使用信令来完成

## 接口及命令格式
### SDK信令接口
#### 同步方式
* 接口描述:同步方式发送信令消息给camera设备并等待回复。同步阻塞方式。
```
int postCommandRequestSync(const char *id, const unsigned char *command, size_t cmd_len, unsigned char **recv_buf, size_t *recv_len, uint64_t timeout_us);
```

| 参数 | 类型 | 描述 | 输入/输出 |
|:-|:-|:-|:-|
| id | const char * | 目标camera在app端的唯一标识符 | 输入 |
| command | const unsigned char * | 信令控制或查询命令 | 输入 |
| cmd_len | size_t | `command`参数长度 | 输入 |
| recv_buf | unsigned char ** | 用于存放camera回复的数据 | 输出 |
| recv_len | size_t * | camera回复的数据长度 | 输出 |
| timeout_us | uint64_t | 命令超时时间，单位为微秒，值为0时采用默认超时(7500ms左右) | 输出 |


| 返回值 | 描述 |
|:-|:-|
| XP2PERRNONE | 成功 |
| XP2PERR* | 失败，对应错误码 |


#### 异步方式
* 接口描述:发送信令消息给camera设备，不用等待回复。异步非阻塞方式。
```
int postCommandRequestWithAsync(const char *id, const unsigned char *command, size_t cmd_len);
```

| 参数 | 类型 | 描述 | 输入/输出 |
|:-|:-|:-|:-|
| id | const char * | 目标camera在app端的唯一标识符 | 输入 |
| command | const unsigned char * | 信令控制或查询命令 | 输入 |
| cmd_len | size_t | `command`参数长度 | 输入 |

| 返回值 | 描述 |
|:-|:-|
| XP2PERRNONE | 成功 |
| XP2PERR* | 失败，对应错误码 |

### 使用场景及命令格式
#### 云台控制信令
* 控制ipc左移:`action=user_define&cmd=ptz_left`
* 控制ipc右移:`action=user_define&cmd=ptz_right`
* 控制ipc上移:`action=user_define&cmd=ptz_top`
* 控制ipc下移:`action=user_define&cmd=ptz_bottom`

#### 查询
* 查询nvr设备子设备:`action=inner_define&cmd=get_nvr_list`
* 查询设备本地录像列表:`action=inner_define&cmd=get_record_index`

### 使用示例 
```shell
/* 通过云台控制ipc左移 */
char cmd[] = "action=user_define&cmd=ptz_left";

/* 查询nvr设备子设备 */
char cmd[] = "action=inner_define&cmd=get_nvr_list&nvr=$nvrname";

/* 异步方式 */
postCommandRequestWithAsync($id, cmd, strlen(cmd));

/* 同步方式 */
char recv[128] = { 0 };
int recv_len = 0;
postCommandRequestSync($id, cmd, strlen(cmd), &recv, &recv_len, 2 * 1000 * 1000);
```

