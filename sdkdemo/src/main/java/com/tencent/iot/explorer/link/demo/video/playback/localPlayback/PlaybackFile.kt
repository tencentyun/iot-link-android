package com.tencent.iot.explorer.link.demo.video.playback.localPlayback

class PlaybackFile {
    var start_time = 0L
    var end_time = 0L
    var file_type = 0 //文件类型，整型值,取值0：视频，1：图片，其他：自定义
    var file_name = ""
    var file_size = 0L
    var extra_info = ""
}