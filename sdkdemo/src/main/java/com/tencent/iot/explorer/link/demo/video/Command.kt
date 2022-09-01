package com.tencent.iot.explorer.link.demo.video

import java.util.*

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

        fun getVideoMJPEGUrlSuffix(channel: Int): String {
            return "ipc.flv?action=live-mjpg&channel=${channel}&quality=standard"
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

        fun getMonthDates(channel: Int, time: String): String {
            // yyyymm 年月
            return "action=inner_define&channel=${channel}&cmd=get_month_record&time=${time}"
        }

        fun getDayTimeBlocks(channel: Int, date: Date): String {
            var dateStart = Date(date.time)
            dateStart.hours = 0
            dateStart.minutes = 0
            dateStart.seconds = 0
            var dateEnd = Date(date.time)
            dateEnd.hours = 23
            dateEnd.minutes = 59
            dateEnd.seconds = 59
            return "action=inner_define&channel=${channel}&cmd=get_file_list" +
                    "&start_time=${dateStart.time/1000}&end_time=${dateEnd.time/1000}&file_type=0"
        }

        fun getLocalVideoUrl(channel: Int, startTime: Long, endTime: Long): String {
            return "ipc.flv?action=playback&channel=${channel}&start_time=${startTime}&end_time=${endTime}"
        }

        fun pauseLocalVideoUrl(channel: Int): String {
            return "action=inner_define&channel=${channel}&cmd=playback_pause"
        }

        fun resumeLocalVideoUrl(channel: Int): String {
            return "action=inner_define&channel=${channel}&cmd=playback_resume"
        }

        fun seekLocalVideo(channel: Int, offset: Long): String {
            return "action=inner_define&channel=${channel}&cmd=playback_seek&time=${offset}}"
        }
    }
}