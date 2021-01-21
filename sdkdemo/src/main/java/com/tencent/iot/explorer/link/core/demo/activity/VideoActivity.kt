package com.tencent.iot.explorer.link.core.demo.activity

import android.Manifest
import android.os.Handler
import android.os.Looper
import android.view.SurfaceHolder
import android.view.View
import android.widget.Toast
import com.tencent.iot.explorer.link.core.demo.R
import com.tencent.iot.explorer.link.core.demo.log.L
import com.tencent.iot.explorer.link.core.demo.util.audio.AudioRecordUtil
import com.tencent.iot.video.link.consts.VideoConst
import com.tencent.xnet.XP2P
import com.tencent.xnet.XP2PCallback
import kotlinx.android.synthetic.main.activity_video.*
import tv.danmaku.ijk.media.player.IjkMediaPlayer
import java.text.SimpleDateFormat
import java.util.*

class VideoActivity : BaseActivity(), View.OnClickListener, SurfaceHolder.Callback, XP2PCallback {

    private lateinit var secretId: String
    private lateinit var secretKey: String
    private lateinit var productId: String
    private lateinit var deviceName: String
    private var playback: Boolean = false
    private var isSpeaking: Boolean = false
    private var isPlaying: Boolean = true
    private var isP2PChannelAvailable: Boolean = false

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
        enableSaveLog()
        val bundle = this.intent.extras
        secretId = bundle?.get(VideoConst.VIDEO_SECRET_ID) as String
        secretKey = bundle.get(VideoConst.VIDEO_SECRET_KEY) as String
        productId = bundle.get(VideoConst.VIDEO_PRODUCT_ID) as String
        deviceName = bundle.get(VideoConst.VIDEO_DEVICE_NAME) as String
        val playbackStr = bundle.get(VideoConst.VIDEO_PLAYBACK)

        if (playbackStr != null && playbackStr == VideoConst.VIDEO_PLAYBACK) {
            playback = true
            speak.visibility = View.GONE
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
            val ret = openP2PChannel(productId, deviceName, secretId, secretKey)
            if (ret == 0) {
                isP2PChannelAvailable = true
                var url = XP2P.delegateHttpFlv() + "ipc.flv"
                url += if (playback) {
                    "?action=playback"
                } else {
                    "?action=live"
                }
                mPlayer.dataSource = url
                mPlayer.prepareAsync()
                mPlayer.start()
            } else {
                isP2PChannelAvailable = false
                speak.visibility = View.GONE
                watch_monitor.visibility = View.GONE
                Toast.makeText(this, "P2P通道建立失败，请检查设备是否上线", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun setListener() {
        speak.setOnClickListener(this)
        watch_monitor.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when(v) {
            speak -> {
                if (isSpeaking) {
                    stopSpeak()
                    isSpeaking = false
                    speak.text = "开始对讲"
                } else {
                    if (checkPermissions(permissions)) {
                        startSpeak()
                        isSpeaking = true
                        speak.text = "停止对讲"
                    } else {
                        requestPermission(permissions)
                    }
                }
            }
            watch_monitor -> {
                if (isPlaying) {
                    mPlayer.pause()
                    isPlaying = false
                    watch_monitor.text = "开始播放"
                } else {
                    mPlayer.start()
                    isPlaying = true
                    watch_monitor.text = "停止播放"
                }
            }
        }
    }

    private fun openP2PChannel(productId: String, deviceName: String, secretId: String, secretKey: String): Int {
        XP2P.setDeviceInfo(productId, deviceName)
        XP2P.setQcloudApiCred(secretId, secretKey)
        XP2P.setXp2pInfoAttributes("_sys_xp2p_info")
        val ret = XP2P.startServiceWithXp2pInfo("")
        return if (ret == 0) {
            Thread.sleep(1000)
            ret
        } else {
            ret
        }
    }

    private fun startSpeak() {
        XP2P.runSendService()
        Thread.sleep(500)
        AudioRecordUtil.getInstance().start()
    }

    private fun stopSpeak() {
        AudioRecordUtil.getInstance().stop()
    }

    private fun enableSaveLog() {
        val sdf = SimpleDateFormat("", Locale.SIMPLIFIED_CHINESE)
        sdf.applyPattern("yyyy-MM-dd-HH-mm-ss")
        val filePath: String = this.externalCacheDir!!.absolutePath + "/logcat-${sdf.format(System.currentTimeMillis())}.txt"
        Runtime.getRuntime().exec(arrayOf("logcat", "-f", filePath, "XP2P-LOG-TAG:V", "*:S"))
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

    override fun fail(msg: String?, errorCode: Int) {
        L.d("==============fail=============")
    }
}