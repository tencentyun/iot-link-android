package com.tencent.iot.explorer.link.demo.video.preview

import android.view.Surface
import android.view.TextureView
import tv.danmaku.ijk.media.player.IjkMediaPlayer

class DevUrl2Preview {
    var devName = ""
    var url = ""
    var Status = 0  // 0 是不在线
    var channel = 0
    var channel2DevName = ""
    var surfaceTextureListener : TextureView.SurfaceTextureListener? = null
    var player: IjkMediaPlayer? = null
    var lock: Object = Object()
    var keepAliveThreadRuning = true
    var surface: Surface? = null
}