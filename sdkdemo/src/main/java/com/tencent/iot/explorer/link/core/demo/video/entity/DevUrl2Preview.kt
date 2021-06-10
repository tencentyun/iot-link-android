package com.tencent.iot.explorer.link.core.demo.video.entity

import android.view.TextureView
import tv.danmaku.ijk.media.player.IjkMediaPlayer

class DevUrl2Preview {
    var devName = ""
    var url = ""
    var Status = 0  // 0 是不在线
    var surfaceTextureListener : TextureView.SurfaceTextureListener? = null
    var player: IjkMediaPlayer? = null
}