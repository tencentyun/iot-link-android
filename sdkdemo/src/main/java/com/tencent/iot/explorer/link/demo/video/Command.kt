package com.tencent.iot.explorer.link.demo.video

// 信令
class Command {
    companion object {
        var QUERY_NVR_DEVS = "action=inner_define&cmd=get_nvr_list" // 查询 NVR 设备列表

        fun getPtzUpCommand(channel: Int): String {
            return "action=user_define&channel=${channel}&cmd=ptz_up"
        }

        fun getPtzDownCommand(channel: Int): String {
            return "action=user_define&channel=${channel}&cmd=ptz_down"
        }

        fun getPtzRightCommand(channel: Int): String {
            return "action=user_define&channel=${channel}&cmd=ptz_right"
        }

        fun getPtzLeftCommand(channel: Int): String {
            return "action=user_define&channel=${channel}&cmd=ptz_left"
        }

        fun getVideoStandardQualityUrlSuffix(channel: Int): String {
            return "ipc.flv?action=live&channel=${channel}&quality=standard"
        }

        fun getVideoHightQualityUrlSuffix(channel: Int): String {
            return "ipc.flv?action=live&channel=${channel}&quality=high"
        }

        fun getVideoSuperQualityUrlSuffix(channel: Int): String {
            return "ipc.flv?action=live&channel=${channel}&quality=super"
        }

        fun getNvrIpcStatus(channel: Int, type: Int): String {
            var typeStr = "live"
            when(type) {
                0 -> typeStr = "live"
                1 -> typeStr = "voice"
            }
            return "action=inner_define&channel=${channel}&cmd=get_device_st&type=${typeStr}&quality=standard"
        }

        fun getTwoWayRadio(channel: Int): String {
            return "channel=${channel}"
        }
    }
}