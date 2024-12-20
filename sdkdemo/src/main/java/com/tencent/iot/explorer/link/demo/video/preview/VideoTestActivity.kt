package com.tencent.iot.explorer.link.demo.video.preview

import android.graphics.SurfaceTexture
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.text.TextUtils
import android.util.DisplayMetrics
import android.util.Log
import android.view.Surface
import android.view.TextureView
import android.view.WindowManager
import android.widget.Toast
import com.tencent.iot.explorer.link.demo.R
import com.tencent.iot.explorer.link.demo.VideoBaseActivity
import com.tencent.iot.explorer.link.demo.common.log.L
import com.tencent.iot.explorer.link.demo.common.util.CommonUtils
import com.tencent.iot.explorer.link.demo.video.Command
import com.tencent.iot.explorer.link.demo.video.utils.TipToastDialog
import com.tencent.xnet.XP2P
import com.tencent.xnet.XP2PCallback
import kotlinx.android.synthetic.main.activity_video_preview.layout_video_preview
import kotlinx.android.synthetic.main.activity_video_preview.tv_video_quality
import kotlinx.android.synthetic.main.activity_video_preview.v_preview
import kotlinx.android.synthetic.main.activity_video_test.btn_connect
import kotlinx.android.synthetic.main.activity_video_test.et_device_name
import kotlinx.android.synthetic.main.activity_video_test.et_p2p_info
import kotlinx.android.synthetic.main.activity_video_test.et_product_id
import kotlinx.android.synthetic.main.dash_board_layout.tv_a_cache
import kotlinx.android.synthetic.main.dash_board_layout.tv_tcp_speed
import kotlinx.android.synthetic.main.dash_board_layout.tv_v_cache
import kotlinx.android.synthetic.main.dash_board_layout.tv_video_w_h
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.json.JSONObject
import tv.danmaku.ijk.media.player.IMediaPlayer
import tv.danmaku.ijk.media.player.IjkMediaPlayer
import java.lang.ref.WeakReference
import java.util.Locale

open class VideoTestActivity : VideoBaseActivity(), XP2PCallback, CoroutineScope by MainScope(),
    TextureView.SurfaceTextureListener,
    IMediaPlayer.OnInfoListener {
    private val player = IjkMediaPlayer()
    lateinit var surface: Surface
    private var productId = ""
    private var deviceName = ""
    private var xp2pInfo = ""
    var urlPrefix = ""
    var screenWidth = 0
    var screenHeight = 0
    var startShowVideoTime = 0L
    var showVideoTime = 0L
    var connectTime = 0L
    var showTip = false
    var firstIn = true
    val MSG_UPDATE_HUD = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        XP2P.setCallback(this)
    }

    override fun getContentView(): Int {
        return R.layout.activity_video_test
    }

    override fun initView() {
        tv_video_quality.text = "高清"
        v_preview.surfaceTextureListener = this
        et_product_id.setText(productId)
        et_device_name.setText(deviceName)
        et_p2p_info.setText(xp2pInfo)
        val wm = this.getSystemService(WINDOW_SERVICE) as WindowManager
        val dm = DisplayMetrics()
        wm.defaultDisplay.getMetrics(dm)
        val width = dm.widthPixels // 屏幕宽度（像素）
        val height = dm.heightPixels // 屏幕高度（像素）
        val density = dm.density // 屏幕密度（0.75 / 1.0 / 1.5）
        screenWidth = (width / density).toInt() // 屏幕宽度(dp)
        screenHeight = (height / density).toInt() // 屏幕高度(dp)
    }

    override fun setListener() {
        btn_connect.setOnClickListener {
            if (et_product_id.text.isNullOrEmpty()) {
                show("请输入productId")
                return@setOnClickListener
            }
            if (et_device_name.text.isNullOrEmpty()) {
                show("请输入deviceName")
                return@setOnClickListener
            }
            if (et_p2p_info.text.isNullOrEmpty()) {
                show("请输入P2PInfo")
                return@setOnClickListener
            }
            productId = et_product_id.text.toString()
            deviceName = et_device_name.text.toString()
            xp2pInfo = et_p2p_info.text.toString()
            XP2P.startService(
                this@VideoTestActivity, "${productId}/${deviceName}", productId, deviceName
            )
            val ret = XP2P.setParamsForXp2pInfo(
                "${productId}/${deviceName}", "", "", xp2pInfo
            )
            if (ret != 0) {
                launch(Dispatchers.Main) {
                    val errInfo: String
                    if (ret.toString() == "-1007") {
                        errInfo = getString(R.string.xp2p_err_version)
                    } else {
                        errInfo = getString(
                            R.string.error_with_code,
                            "${productId}/${deviceName}",
                            ret.toString()
                        )
                    }
                    Toast.makeText(this@VideoTestActivity, errInfo, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun resetPlayer() {
        when (tv_video_quality.text.toString()) {
            getString(R.string.video_quality_high_str) -> setPlayerUrl(
                Command.getVideoSuperQualityUrlSuffix(
                    0
                )
            )

            getString(R.string.video_quality_medium_str) -> setPlayerUrl(
                Command.getVideoHightQualityUrlSuffix(
                    0
                )
            )

            getString(R.string.video_quality_low_str) -> setPlayerUrl(
                Command.getVideoStandardQualityUrlSuffix(
                    0
                )
            )
        }
    }

    open fun setPlayerUrl(suffix: String) {
        mHandler.sendEmptyMessageDelayed(MSG_UPDATE_HUD, 500)
        showTip = false
        startShowVideoTime = System.currentTimeMillis()
        launch(Dispatchers.Main) {
            layout_video_preview?.removeView(v_preview)
            layout_video_preview?.addView(v_preview, 0)
            player.setOnInfoListener(this@VideoTestActivity)
            player.let {
                val url = urlPrefix + suffix
                it.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "probesize", 50 * 1024)
                it.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "packet-buffering", 0)
                it.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "start-on-prepared", 1)
                it.setOption(IjkMediaPlayer.OPT_CATEGORY_CODEC, "threads", 1)
                it.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "sync-av-start", 0)
                it.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec", 1)
                it.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec-auto-rotate", 1)
                it.setOption(
                    IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec-handle-resolution-change", 1
                )

                it.setFrameSpeed(1.5f)
                while (!::surface.isInitialized) {
                    delay(50)
                    L.e("delay for waiting surface.")
                }
                it.setSurface(surface)
                it.dataSource = url

                it.prepareAsync()
                it.start()
            }
        }
    }

    override fun fail(msg: String?, errorCode: Int) {

    }

    override fun commandRequest(id: String?, msg: String?) {
        Log.e("VideoTestActivity", "xp2pEventNotify id:$id  msg:$msg")
    }

    override fun xp2pEventNotify(id: String?, msg: String?, event: Int) {
        Log.e("VideoTestActivity", "xp2pEventNotify id:$id  msg:$msg  event:$event")
        if (event == 1004 || event == 1005) {
            if (event == 1004) {
                Log.e("VideoTestActivity", "====event === 1004")
                XP2P.delegateHttpFlv(id)?.let {
                    urlPrefix = it
                    if (!TextUtils.isEmpty(urlPrefix)) {
                        resetPlayer()
                    }
                }
            }
        }
    }

    override fun avDataRecvHandle(id: String?, data: ByteArray?, len: Int) {
        Log.e("VideoTestActivity", "avDataRecvHandle id:$id  data:$data  len:$data")
    }

    override fun avDataCloseHandle(id: String?, msg: String?, errorCode: Int) {
        Log.e("VideoTestActivity", "avDataCloseHandle id:$id  msg:$msg  errorCode:$errorCode")
    }

    override fun onDeviceMsgArrived(id: String?, data: ByteArray?, len: Int): String {
        Log.e("VideoTestActivity", "onDeviceMsgArrived id:$id  data:$data  len:$len")
        val reply = JSONObject()
        reply.put("code", "0")
        reply.put("msg", "test command reply")
        return reply.toString()
    }

    override fun onSurfaceTextureAvailable(surface: SurfaceTexture?, width: Int, height: Int) {
        surface?.let {
            this.surface = Surface(surface)
            player.setSurface(this.surface)
        }
    }

    override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture?, width: Int, height: Int) {
        val layoutParams = v_preview.layoutParams
        layoutParams.width = (player.videoWidth * (screenWidth * 16 / 9)) / player.videoHeight
        layoutParams.height = layoutParams.height
        v_preview.layoutParams = layoutParams
    }

    override fun onSurfaceTextureDestroyed(surface: SurfaceTexture?): Boolean {
        return false
    }

    override fun onSurfaceTextureUpdated(surface: SurfaceTexture?) {
        if (!showTip && startShowVideoTime > 0) {
            showVideoTime = System.currentTimeMillis() - startShowVideoTime
            val content =
                getString(R.string.time_2_show, connectTime.toString(), showVideoTime.toString())
            TipToastDialog(this, content, 10000).show()
            showTip = true
        }
        if (firstIn) {
            val layoutParams = v_preview.layoutParams
            layoutParams.width = (player.videoWidth * (screenWidth * 16 / 9)) / player.videoHeight
            layoutParams.height = layoutParams.height
            v_preview.layoutParams = layoutParams
            firstIn = false
        }
    }

    override fun onInfo(p0: IMediaPlayer?, p1: Int, p2: Int): Boolean {
        return true
    }

    override fun onInfoSEI(p0: IMediaPlayer?, p1: Int, p2: Int, p3: String?): Boolean {
        return false
    }

    override fun onInfoAudioPcmData(p0: IMediaPlayer?, p1: ByteArray?, p2: Int) {
    }

    private val mHandler = MyHandler(this)

    private class MyHandler(activity: VideoTestActivity) : Handler() {
        private val mActivity: WeakReference<VideoTestActivity> = WeakReference(activity)
        override fun handleMessage(msg: Message) {
            if (mActivity.get() == null) {
                return
            }
            val activity = mActivity.get()
            when (msg.what) {
                activity?.MSG_UPDATE_HUD -> {
                    activity.updateDashboard()
                    removeMessages(activity.MSG_UPDATE_HUD)
                    sendEmptyMessageDelayed(activity.MSG_UPDATE_HUD, 500)
                }
            }
        }
    }

    private fun updateDashboard() {
        val videoCachedDuration = player.videoCachedDuration
        val audioCachedDuration = player.audioCachedDuration
        val videoCachedBytes = player.videoCachedBytes
        val audioCachedBytes = player.audioCachedBytes
        val tcpSpeed = player.tcpSpeed

        tv_a_cache?.text = String.format(
            Locale.US, "%s, %s",
            CommonUtils.formatedDurationMilli(audioCachedDuration),
            CommonUtils.formatedSize(audioCachedBytes)
        )
        tv_v_cache?.text = String.format(
            Locale.US, "%s, %s",
            CommonUtils.formatedDurationMilli(videoCachedDuration),
            CommonUtils.formatedSize(videoCachedBytes)
        )
        tv_tcp_speed?.text = String.format(
            Locale.US, "%s",
            CommonUtils.formatedSpeed(tcpSpeed, 1000)
        )
        tv_video_w_h?.text = "${player.videoWidth} x ${player.videoHeight}"
    }

    override fun onDestroy() {
        super.onDestroy()
        finishPlayer()
        XP2P.stopService("${productId}/${deviceName}")
        XP2P.setCallback(null)
        cancel()
    }

    private fun finishPlayer() {
        mHandler.removeMessages(MSG_UPDATE_HUD)
        player.release()
    }
}