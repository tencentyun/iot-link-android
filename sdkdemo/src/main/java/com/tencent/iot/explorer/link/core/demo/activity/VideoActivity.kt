package com.tencent.iot.explorer.link.core.demo.activity

import android.os.Handler
import android.os.Looper
import android.view.SurfaceHolder
import android.view.View
import com.tencent.iot.explorer.link.core.demo.R
import com.tencent.iot.explorer.link.core.demo.activity.BaseActivity
import com.tencent.iot.explorer.link.core.demo.log.L
import com.tencent.iot.video.ffmpegplayer.EasyPlayer
import com.tencent.iot.video.link.XP2P
import kotlinx.android.synthetic.main.activity_video.*

class VideoActivity : BaseActivity(), View.OnClickListener, SurfaceHolder.Callback {

    private val easyPlayer: EasyPlayer = EasyPlayer()
    private val mainHandler = Handler(Looper.getMainLooper())
    private val xp2p = XP2P()

    override fun getContentView(): Int {
        return R.layout.activity_video
    }

    override fun initView() { }

    override fun setListener() {
        realtime_monitor.setOnClickListener(this)
        replay.setOnClickListener(this)
        speak.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when(v) {
            realtime_monitor -> { // 实时监控
//                xp2p.openP2PChannel("25QWpIISwKyJ8JNIFc")
//                Thread.sleep(500)
//                easyPlayer.setDataSource(xp2p.httpFlvUrl + "ipc.flv")
                easyPlayer.setDataSource("http://zhibo.hkstv.tv/livestream/mutfysrq.flv")
                video_view.holder.addCallback(this@VideoActivity)
                easyPlayer.setEventCallback {
                    L.d("onPrepared")
                    mainHandler.post {
                        val viewWidth = video_view.width
                        val videoWidth = easyPlayer.videoWidth
                        val videoHeight = easyPlayer.videoHeight
                        val lp = video_view.layoutParams
                        lp.width = viewWidth
                        lp.height = (videoHeight.toFloat() / videoWidth.toFloat() * viewWidth.toFloat()).toInt()
                        video_view.layoutParams = lp
                    }
                    easyPlayer.start()
                }
                this@VideoActivity.surfaceCreated(video_view.holder)
            }
            speak -> {
                // 对讲
            }
            replay -> {
                // 回放
            }
        }
    }

    override fun surfaceChanged(holder: SurfaceHolder?, format: Int, width: Int, height: Int) {
    }

    override fun surfaceDestroyed(holder: SurfaceHolder?) {
    }

    override fun surfaceCreated(holder: SurfaceHolder?) {
        easyPlayer.setSurface(holder!!.surface)
        easyPlayer.prepareAsync()
    }
}