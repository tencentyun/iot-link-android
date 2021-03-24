package com.tencent.iot.explorer.link.core.demo.activity

import android.Manifest
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.view.SurfaceHolder
import android.view.View
import android.widget.Toast
import com.tencent.iot.explorer.link.core.demo.R
import com.tencent.iot.explorer.link.core.demo.log.L
import com.tencent.iot.explorer.link.core.demo.util.LogcatHelper
import com.tencent.iot.explorer.link.core.utils.SharePreferenceUtil
import com.tencent.iot.video.link.util.audio.AudioRecordUtil
import com.tencent.iot.video.link.consts.VideoConst
import com.tencent.xnet.XP2P
import com.tencent.xnet.XP2PCallback
import kotlinx.android.synthetic.main.activity_video.*
import tv.danmaku.ijk.media.player.IjkMediaPlayer
import java.util.*


class VideoActivity : BaseActivity(), View.OnClickListener, SurfaceHolder.Callback, XP2PCallback {

    private lateinit var secretId: String
    private lateinit var secretKey: String
    private lateinit var productId: String
    private lateinit var deviceName: String
    private var isSpeaking: Boolean = false
    private var isPlaying: Boolean = true
    private var isP2PChannelAvailable: Boolean = false
    private var isXp2pDisconnect: Boolean = false
    private var timer: Timer? = null
    private lateinit var deviceID: String

    private lateinit var mPlayer: IjkMediaPlayer
    private lateinit var audioRecordUtil: AudioRecordUtil
    private val mHandler = Handler(Looper.getMainLooper())

    private lateinit var mPlayer1: IjkMediaPlayer
    private val mHandler1 = Handler(Looper.getMainLooper())

    private var permissions = arrayOf(
        Manifest.permission.RECORD_AUDIO
    )

    override fun getContentView(): Int {
        return R.layout.activity_video
    }

    override fun initView() {
        requestPermission(permissions)
        getSwitchState(this)
        LogcatHelper.getInstance(this).start()
        val bundle = this.intent.extras
        secretId = bundle?.get(VideoConst.VIDEO_SECRET_ID) as String
        secretKey = bundle.get(VideoConst.VIDEO_SECRET_KEY) as String
        productId = bundle.get(VideoConst.VIDEO_PRODUCT_ID) as String
        deviceName = bundle.get(VideoConst.VIDEO_DEVICE_NAME) as String

        deviceID = productId + "/" + deviceName

        audioRecordUtil = AudioRecordUtil(this)
        video_view.holder.addCallback(this)
        video_view1.holder.addCallback(this)
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

        mPlayer1 = IjkMediaPlayer()
        mPlayer1.setOnPreparedListener {
            mHandler1.post {
                val viewWidth = video_view1.width
                val videoWidth = mPlayer1.videoWidth
                val videoHeight = mPlayer1.videoHeight
                val lp = video_view1.layoutParams
                lp.width = viewWidth
                lp.height = (videoHeight.toFloat() * viewWidth.toFloat() / videoWidth.toFloat()).toInt()
                video_view1.layoutParams = lp
            }
        }

        if (productId == " " || deviceName == " " || secretId == " " || secretKey == " ") {
            Toast.makeText(this, "设备信息有误，请确保配置文件中的设备信息填写正确", Toast.LENGTH_LONG).show()
        } else {
            //reStartXp2pThread()
            //openP2PChannel(productId, "sp01_32820237_9", secretId, secretKey)
            //Thread.sleep(1000)
            val ret = openP2PChannel(productId, deviceName, secretId, secretKey)
            if (ret == 0) {
                isP2PChannelAvailable = true
                if (isSaveAVData()) {
//                    tv_writting_raw_data.visibility = View.VISIBLE
//                    XP2P.startAvRecvService("action=live")
                } else {
                    tv_writting_raw_data.visibility = View.INVISIBLE
//                    val url = XP2P.delegateHttpFlv() + "ipc.flv?action=live"
                    val url = XP2P.delegateHttpFlv(deviceID) + "ipc.flv?action=live"
                    val url1 = XP2P.delegateHttpFlv("AQTV2839QJ/sp01_32820237_9") + "ipc.flv?action=live"
                    L.d("==============url $url $url1");

                    mPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "analyzemaxduration", 100)
                    mPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "probesize", 25 * 1024)
                    mPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "packet-buffering", 0)
                    mPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "start-on-prepared", 1)
                    mPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_CODEC, "threads", 1)
                    mPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "sync-av-start", 0)

                    mPlayer1.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "analyzemaxduration", 100)
                    mPlayer1.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "probesize", 25 * 1024)
                    mPlayer1.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "packet-buffering", 0)
                    mPlayer1.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "start-on-prepared", 1)
                    mPlayer1.setOption(IjkMediaPlayer.OPT_CATEGORY_CODEC, "threads", 1)
                    mPlayer1.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "sync-av-start", 0)

                    mPlayer.dataSource = url
                    mPlayer.prepareAsync()
                    mPlayer.start()

//                    mPlayer1.dataSource = url
//                    mPlayer1.prepareAsync()
//                    mPlayer1.start()
                }
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

    private fun reStartXp2pThread() {
        timer = Timer()
        val task: TimerTask = object : TimerTask() {
            override fun run() {
                if (isXp2pDisconnect) {
                    XP2P.stopService(deviceID)
                    Thread.sleep(500)
                    var ret = XP2P.startServiceWithXp2pInfo(deviceID, productId, deviceName, "_sys_xp2p_info", "")
                    if (ret == 0) {
                        isXp2pDisconnect = false

                        Thread.sleep(500)
                        mPlayer.reset()
                        mPlayer.dataSource = XP2P.delegateHttpFlv(deviceID) + "ipc.flv?action=live"
                        mPlayer.setSurface(video_view.holder.surface)
                        mPlayer.prepareAsync()
                        mPlayer.start()
                    } else {
                        runOnUiThread {
                            Toast.makeText(getApplicationContext(), "p2p连接断开,正在尝试重新连接", Toast.LENGTH_LONG).show()
                        }
                    }
                }
            }
        }
        timer!!.schedule(task,0,1000);
    }

    private fun openP2PChannel(productId: String, deviceName: String, secretId: String, secretKey: String): Int {
        //XP2P.setDeviceInfo(productId, deviceName)
        XP2P.setQcloudApiCred(secretId, secretKey)
        //XP2P.setXp2pInfoAttributes("_sys_xp2p_info")

        val devID = productId + "/" + deviceName
        XP2P.setCallback(devID, this)

        val ret = XP2P.startServiceWithXp2pInfo(devID, productId, deviceName, "_sys_xp2p_info", "")
        return ret
    }

    private fun startSpeak() {
        XP2P.runSendService(deviceID)
        Thread.sleep(500)
        audioRecordUtil.start()
    }

    private fun stopSpeak() {
        audioRecordUtil.stop()
    }

    override fun surfaceChanged(holder: SurfaceHolder?, format: Int, width: Int, height: Int) { }

    override fun surfaceDestroyed(holder: SurfaceHolder?) { }

    override fun surfaceCreated(holder: SurfaceHolder?) {
        mPlayer.setDisplay(holder)
    }

    override fun onDestroy() {
        super.onDestroy()
        mPlayer.release()
        timer?.cancel()
        XP2P.stopService(deviceID)
        XP2P.stopService("AQTV2839QJ/sp01_32820237_9")
        audioRecordUtil.release()
        LogcatHelper.getInstance(this).stop()
    }

    override fun commandRequest(p0: String?, p1: String?) {

    }

    override fun xp2pLinkError(p0: String?, p1: String?) {
        isXp2pDisconnect = true
    }

    override fun fail(msg: String?, errorCode: Int) {
        //do some non-time-consuming processing
    }

    override fun avDataRecvHandle(p0: String?, p1: ByteArray?, p2: Int) {

    }

    override fun avDataCloseHandle(p0: String?, p1: String?, p2: Int) {

    }

    companion object {
        var saveData = false
        fun getSwitchState(context: Context) {
            saveData = SharePreferenceUtil.getString(context, VideoConst.VIDEO_CONFIG, VideoConst.VIDEO_SAVE_RAW_AV) == "true"
        }
        @JvmStatic fun isSaveAVData() : Boolean {
            return saveData
        }
    }
}
