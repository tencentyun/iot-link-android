package com.tencent.iot.explorer.link.core.demo.activity

import android.Manifest
import android.content.Context
import android.graphics.SurfaceTexture
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import android.view.Surface
import android.view.TextureView
import android.view.View
import android.widget.Toast
import com.tencent.iot.explorer.link.core.demo.R
import com.tencent.iot.explorer.link.core.demo.util.ImageSelect
import com.tencent.iot.explorer.link.core.demo.util.LogcatHelper
import com.tencent.iot.explorer.link.core.utils.SharePreferenceUtil
import com.tencent.iot.video.link.consts.VideoConst
import com.tencent.iot.video.link.util.audio.AudioRecordUtil
import com.tencent.xnet.XP2P
import com.tencent.xnet.XP2PCallback
import kotlinx.android.synthetic.main.activity_video.*
import tv.danmaku.ijk.media.player.IjkMediaPlayer
import java.util.*
import java.util.concurrent.CyclicBarrier


private lateinit var barrier: CyclicBarrier
private var isXp2pDisconnect: Boolean = false
private var isXp2pDetectReady: Boolean = false
private var isXp2pDetectError: Boolean = false
class VideoActivity : BaseActivity(), View.OnClickListener, XP2PCallback,
    TextureView.SurfaceTextureListener {

    private lateinit var secretId: String
    private lateinit var secretKey: String
    private lateinit var productId: String
    private lateinit var deviceName: String
    private var isSpeaking: Boolean = false
    private var isPlaying: Boolean = true
    private var isP2PChannelAvailable: Boolean = false

    private lateinit var surface: Surface

    private var timer: Timer? = null

    private lateinit var mPlayer: IjkMediaPlayer
    private lateinit var audioRecordUtil: AudioRecordUtil
    private val mHandler = Handler(Looper.getMainLooper())

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
        barrier = CyclicBarrier(2)
        val bundle = this.intent.extras
        secretId = bundle?.get(VideoConst.VIDEO_SECRET_ID) as String
        secretKey = bundle.get(VideoConst.VIDEO_SECRET_KEY) as String
        productId = bundle.get(VideoConst.VIDEO_PRODUCT_ID) as String
        deviceName = bundle.get(VideoConst.VIDEO_DEVICE_NAME) as String

        audioRecordUtil = AudioRecordUtil(this, "$productId/$deviceName")
        video_view.surfaceTextureListener = this
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
            xp2pRestartDaemonThread()
            xp2pStartAndPlayThread(this)
        }
    }

    override fun setListener() {
        speak.setOnClickListener(this)
        watch_monitor.setOnClickListener(this)
        capture_snapshot.setOnClickListener(this)
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
            capture_snapshot -> {
                ImageSelect.saveBitmap(video_view.bitmap)
                Toast.makeText(this, "截图保存成功", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun xp2pStartAndPlayThread(xp2p: XP2PCallback) {
        XP2P.setCallback(xp2p)
        object : Thread() {
            override fun run() {
                val ret = openP2PChannel(productId, deviceName, secretId, secretKey)
                if (ret == 0) {
                    isP2PChannelAvailable = true
                    avPlay()
                } else {
                    isP2PChannelAvailable = false
                    runOnUiThread {
                        speak.visibility = View.GONE
                        watch_monitor.visibility = View.GONE
                        capture_snapshot.visibility = View.GONE
                        Toast.makeText(getApplicationContext(), "P2P通道建立失败，请检查设备是否上线", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }.start()
    }

    private fun avPlay() {
        barrier.await()
        if (isXp2pDetectReady) {
            isXp2pDetectReady = false
            if (isSaveAVData()) {
                runOnUiThread {
                    tv_writting_raw_data.visibility = View.VISIBLE
                }
                XP2P.startAvRecvService("$productId/$deviceName", "action=live", true)
            } else {

                val url_prefix = XP2P.delegateHttpFlv("$productId/$deviceName")
                if (!TextUtils.isEmpty(url_prefix) && mPlayer != null) {
                    val url = url_prefix + "ipc.flv?action=live"
                    runOnUiThread {
                        tv_writting_raw_data.visibility = View.INVISIBLE

                        mPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "analyzemaxduration", 100)
                        mPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "probesize", 25 * 1024)
                        mPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "packet-buffering", 0)
                        mPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "start-on-prepared", 1)
                        mPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_CODEC, "threads", 1)
                        mPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "sync-av-start", 0)
                        mPlayer.dataSource = url
                        mPlayer.prepareAsync()
                        mPlayer.start()
                    }
                }
            }
        } else {
            runOnUiThread {
                Toast.makeText(getApplicationContext(), "P2P探测失败，请检查当前网络环境", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun xp2pRestartDaemonThread() {
        timer = Timer()
        val task: TimerTask = object : TimerTask() {
            override fun run() {
                if (isXp2pDisconnect) {
                    XP2P.stopService("$productId/$deviceName")
                    val ret = XP2P.startServiceWithXp2pInfo("$productId/$deviceName", productId, deviceName, "")
                    if (ret == 0) {
                        isXp2pDisconnect = false
                        barrier.await()
                        if (isXp2pDetectReady) {
                            isXp2pDetectReady = false

                            runOnUiThread {
                                val url_prefix = XP2P.delegateHttpFlv("$productId/$deviceName")
                                if (!TextUtils.isEmpty(url_prefix) && mPlayer != null) {
                                    val url = url_prefix + "ipc.flv?action=live"
                                    mPlayer.reset()
                                    mPlayer.setSurface(surface)
                                    mPlayer.dataSource = url
                                    mPlayer.prepareAsync()
                                    mPlayer.start()
                                }
                            }
                        } else {
                            runOnUiThread {
                                Toast.makeText(getApplicationContext(), "P2P探测失败，请检查当前网络环境", Toast.LENGTH_LONG).show()
                            }
                        }
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
        XP2P.setQcloudApiCred(secretId, secretKey)
        return XP2P.startServiceWithXp2pInfo("$productId/$deviceName", productId, deviceName, "")
    }

    private fun startSpeak() {
        XP2P.runSendService("$productId/$deviceName", "", true)
        Thread.sleep(500)
        audioRecordUtil.start()
    }

    private fun stopSpeak() {
        audioRecordUtil.stop()
        XP2P.stopSendService("$productId/$deviceName", null)
    }

    override fun onDestroy() {
        super.onDestroy()
        mPlayer.release()
        timer?.cancel()
        XP2P.stopService("$productId/$deviceName")
        audioRecordUtil.release()
        LogcatHelper.getInstance(this).stop()
    }

    override fun commandRequest(id: String?, msg: String?) {
        //do some non-time-consuming processing
    }

    override fun fail(msg: String?, errorCode: Int) {
        //do some non-time-consuming processing
    }

    override fun xp2pEventNotify(id: String?, msg: String?, event: Int) {
        //do some non-time-consuming processing
        if (event == 1003) {
            isXp2pDisconnect = true
        } else if (event == 1004) {
            isXp2pDetectReady = true
            barrier.await()
        } else if (event == 1005) {
            isXp2pDetectError = true
            barrier.await()
        }
    }

    override fun avDataRecvHandle(id: String?, data: ByteArray?, len: Int) { // 音视频数据回调接口
        //do some non-time-consuming processing
    }

    override fun avDataCloseHandle(id: String?, msg: String?, errorCode: Int) {
        //do some non-time-consuming processing
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

    override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture?, width: Int, height: Int) { }

    override fun onSurfaceTextureUpdated(surface: SurfaceTexture?) { }

    override fun onSurfaceTextureDestroyed(surface: SurfaceTexture?): Boolean {
        return false
    }

    override fun onSurfaceTextureAvailable(surface: SurfaceTexture?, width: Int, height: Int) {
        if (surface != null) {
            this.surface = Surface(surface)
        }
        mPlayer.setSurface(this.surface)
    }
}
