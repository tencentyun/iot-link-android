package com.tencent.iot.explorer.link.core.demo.activity

import android.Manifest
import android.os.Handler
import android.os.Looper
import android.view.SurfaceHolder
import android.view.View
import com.tencent.iot.explorer.link.core.demo.R
import com.tencent.iot.explorer.link.core.demo.util.audio.AudioRecordUtil
import com.tencent.iot.video.link.XP2P
import com.tencent.iot.video.link.consts.VideoConst
import kotlinx.android.synthetic.main.activity_video.*
import tv.danmaku.ijk.media.player.IjkMediaPlayer

class VideoActivity : BaseActivity(), View.OnClickListener, SurfaceHolder.Callback {

    private lateinit var secretId: String
    private lateinit var secretKey: String
    private lateinit var productId: String
    private lateinit var deviceName: String
    private var playback: Boolean = false

    private lateinit var mPlayer: IjkMediaPlayer
    private val mHandler = Handler(Looper.getMainLooper())

    private var permissions = arrayOf(
        Manifest.permission.RECORD_AUDIO
    )

    override fun getContentView(): Int {
        return R.layout.activity_video
    }

    override fun initView() {
        requestPermission(permissions)
        val bundle = this.intent.extras
        secretId = bundle?.get(VideoConst.VIDEO_SECRET_ID) as String
        secretKey = bundle.get(VideoConst.VIDEO_SECRET_KEY) as String
        productId = bundle.get(VideoConst.VIDEO_PRODUCT_ID) as String
        deviceName = bundle.get(VideoConst.VIDEO_DEVICE_NAME) as String
        val playbackStr = bundle.get(VideoConst.VIDEO_PLAYBACK)

        if (playbackStr != null && playbackStr == VideoConst.VIDEO_PLAYBACK) {
            playback = true
            start_speak.visibility = View.GONE
            stop_speak.visibility = View.GONE
        }

        video_view.holder.addCallback(this)
        mPlayer = IjkMediaPlayer()
        mPlayer.setOnPreparedListener {
            mHandler.post {
                val viewWidth = video_view.width
                val videoWidth = mPlayer.videoWidth
                val videoHeight = mPlayer.videoHeight
                val lp = video_view.layoutParams
                lp.width = viewWidth
                lp.height = (videoHeight.toFloat() * viewWidth.toFloat() / videoWidth.toFloat()).toInt()
                video_view.layoutParams = lp
            }
        }
        mPlayer.dataSource = "http://zhibo.hkstv.tv/livestream/mutfysrq.flv"
        mPlayer.prepareAsync()
        mPlayer.start()
    }

    override fun setListener() {
        start_speak.setOnClickListener(this)
        stop_speak.setOnClickListener(this)
        stop_watch_monitor.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when(v) {
            start_speak -> {
//                val id = "E6L6GWVSQD"
//                val name = "llynne_001"
//
//                val api_id = "AKIDA8jCQOJcsteLr8p0cu1mEQO6by1os4QW"
//                val api_key = "1qTqyqb7MNCsISLDm58yf4CA4lFZhaDa"

                openP2PChannel(productId, deviceName, secretId, secretKey)
                XP2P.runSendService()
                try {
                    Thread.sleep(500)
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }
                AudioRecordUtil.getInstance().start()
            }

            stop_speak -> {
                AudioRecordUtil.getInstance().stop()
            }

            stop_watch_monitor -> {
                mPlayer.stop()
            }
        }
    }

    override fun surfaceChanged(holder: SurfaceHolder?, format: Int, width: Int, height: Int) { }

    override fun surfaceDestroyed(holder: SurfaceHolder?) { }

    override fun surfaceCreated(holder: SurfaceHolder?) {
        mPlayer.setDisplay(holder)
    }

    fun openP2PChannel(productId: String, deviceName: String, secretId: String, secretKey: String) {
        XP2P.setDeviceInfo(productId, deviceName)
        XP2P.setQcloudApiCred(secretId, secretKey)
        XP2P.startServiceWithPeername("")
        Thread.sleep(500)
    }

    override fun onDestroy() {
        super.onDestroy()
        mPlayer.release()
    }
}