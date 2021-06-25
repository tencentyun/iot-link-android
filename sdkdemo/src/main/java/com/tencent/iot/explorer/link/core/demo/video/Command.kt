package com.tencent.iot.explorer.link.core.demo.video

// 信令
class Command {
    companion object {
        var QUERY_NVR_DEVS = "action=inner_define&cmd=get_nvr_list" // 查询 NVR 设备列表
        var PTZ_UP = "action=user_define&cmd=ptz_top" // 云台向上
        var PTZ_DOWN = "action=user_define&cmd=ptz_bottom" // 云台向下
        var PTZ_RIGHT = "action=user_define&cmd=ptz_right" // 云台向右
        var PTZ_LEFT = "action=user_define&cmd=ptz_left" // 云台向左
        var VIDEO_STANDARD_QUALITY_URL_SUFFIX = "ipc.flv?action=live&quality=standard"
        var VIDEO_HIGH_QUALITY_URL_SUFFIX = "ipc.flv?action=live&quality=high"
        var VIDEO_SUPER_QUALITY_URL_SUFFIX = "ipc.flv?action=live&quality=super"
    }
}