package com.tencent.iot.explorer.link.core.demo.activity

import android.Manifest
import android.os.Handler
import android.os.Looper
import android.view.SurfaceHolder
import android.view.View
import android.widget.Toast
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
        if (productId == " " || deviceName == " " || secretId == " " || secretKey == " ") {
            Toast.makeText(this, "设备信息有误，请确保配置文件中的设备信息填写正确", Toast.LENGTH_LONG).show()
        } else {
            openP2PChannel(productId, deviceName, secretId, secretKey)
            var url = XP2P.delegateHttpFlv() + "ipc.flv"
            url += if (playback) {
                "?action=playback"
            } else {
                "?action=live"
            }
            mPlayer.dataSource = url
            mPlayer.prepareAsync()
            mPlayer.start()
        }
    }

    override fun setListener() {
        start_speak.setOnClickListener(this)
        stop_speak.setOnClickListener(this)
        stop_watch_monitor.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when(v) {
            start_speak -> {
                if (checkPermissions(permissions)) {
                    startSpeak()
                } else {
                    requestPermission(permissions)
                }
            }

            stop_speak -> {
                stopSpeak()
            }

            stop_watch_monitor -> {
                mPlayer.stop()
            }
        }
    }

    private fun openP2PChannel(productId: String, deviceName: String, secretId: String, secretKey: String) {
        XP2P.setDeviceInfo(productId, deviceName)
        XP2P.setQcloudApiCred(secretId, secretKey)
        XP2P.setXp2pInfoAttributes("_sys_xp2p_info")
        XP2P.startServiceWithXp2pInfo("")
        Thread.sleep(600)
    }

    private fun startSpeak() {
        XP2P.runSendService()
        Thread.sleep(500)
        AudioRecordUtil.getInstance().start()
    }

    private fun stopSpeak() {
        AudioRecordUtil.getInstance().stop()
    }

    override fun surfaceChanged(holder: SurfaceHolder?, format: Int, width: Int, height: Int) { }

    override fun surfaceDestroyed(holder: SurfaceHolder?) { }

    override fun surfaceCreated(holder: SurfaceHolder?) {
        mPlayer.setDisplay(holder)
    }

    override fun onDestroy() {
        super.onDestroy()
        mPlayer.release()
        XP2P.stopService()
    }
}