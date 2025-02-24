package com.tencent.iot.explorer.link.demo.video.preview

import android.Manifest
import android.app.Service
import android.content.pm.ActivityInfo
import android.graphics.SurfaceTexture
import android.media.AudioFormat
import android.media.AudioManager
import android.os.Handler
import android.os.Message
import android.text.TextUtils
import android.util.DisplayMetrics
import android.util.Log
import android.view.Surface
import android.view.TextureView
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import com.alibaba.fastjson.JSONArray
import com.tencent.iot.explorer.link.demo.BuildConfig
import com.tencent.iot.explorer.link.demo.R
import com.tencent.iot.explorer.link.demo.VideoBaseActivity
import com.tencent.iot.explorer.link.demo.common.log.L
import com.tencent.iot.explorer.link.demo.common.util.CommonUtils
import com.tencent.iot.explorer.link.demo.common.util.ImageSelect
import com.tencent.iot.explorer.link.demo.video.Command
import com.tencent.iot.explorer.link.demo.video.DevInfo
import com.tencent.iot.explorer.link.demo.video.playback.VideoPlaybackActivity
import com.tencent.iot.explorer.link.demo.video.utils.ListOptionsDialog
import com.tencent.iot.explorer.link.demo.video.utils.TipToastDialog
import com.tencent.iot.explorer.link.demo.video.utils.ToastDialog
import com.tencent.iot.video.link.util.audio.AudioRecordUtil
import com.tencent.xnet.XP2P
import com.tencent.xnet.XP2PAppConfig
import com.tencent.xnet.XP2PCallback
import com.tencent.xnet.annotations.XP2PProtocolType
import kotlinx.android.synthetic.main.activity_video_preview.btn_layout
import kotlinx.android.synthetic.main.activity_video_preview.iv_audio
import kotlinx.android.synthetic.main.activity_video_preview.iv_down
import kotlinx.android.synthetic.main.activity_video_preview.iv_left
import kotlinx.android.synthetic.main.activity_video_preview.iv_orientation
import kotlinx.android.synthetic.main.activity_video_preview.iv_right
import kotlinx.android.synthetic.main.activity_video_preview.iv_up
import kotlinx.android.synthetic.main.activity_video_preview.layout_content
import kotlinx.android.synthetic.main.activity_video_preview.layout_video_preview
import kotlinx.android.synthetic.main.activity_video_preview.radio_photo
import kotlinx.android.synthetic.main.activity_video_preview.radio_playback
import kotlinx.android.synthetic.main.activity_video_preview.radio_record
import kotlinx.android.synthetic.main.activity_video_preview.radio_talk
import kotlinx.android.synthetic.main.activity_video_preview.tv_video_quality
import kotlinx.android.synthetic.main.activity_video_preview.v_preview
import kotlinx.android.synthetic.main.activity_video_preview.v_title
import kotlinx.android.synthetic.main.dash_board_layout.tv_a_cache
import kotlinx.android.synthetic.main.dash_board_layout.tv_tcp_speed
import kotlinx.android.synthetic.main.dash_board_layout.tv_v_cache
import kotlinx.android.synthetic.main.dash_board_layout.tv_video_w_h
import kotlinx.android.synthetic.main.title_layout.iv_back
import kotlinx.android.synthetic.main.title_layout.tv_title
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

class VideoTestActivity : VideoBaseActivity(), XP2PCallback, CoroutineScope by MainScope(),
    TextureView.SurfaceTextureListener, IMediaPlayer.OnInfoListener {

    private val tag = VideoTestActivity::class.simpleName

    private lateinit var player: IjkMediaPlayer
    private lateinit var surface: Surface
    private var productId: String = ""
    private var deviceName: String = ""
    private var xp2pInfo: String = ""
    private val channel: Int = 0
    private var urlPrefix = ""
    private var audioRecordUtil: AudioRecordUtil? = null

    private var permissions = arrayOf(Manifest.permission.RECORD_AUDIO)
    private var showTip = false
    private var connectStartTime = 0L
    private var connectTime = 0L
    private var startShowVideoTime = 0L
    private var showVideoTime = 0L


    private val MSG_UPDATE_HUD = 1
    private var screenWidth = 0
    private var screenHeight = 0

    private var firstIn = true
    private var speakAble = false
    private var audioAble = true
    private var filePath: String? = null
    private var orientationV = true

    private val xP2PAppConfig = XP2PAppConfig().also { appConfig ->
        appConfig.appKey =
            BuildConfig.TencentIotLinkSDKDemoAppkey //为explorer平台注册的应用信息(https://console.cloud.tencent.com/iotexplorer/v2/instance/app/detai) explorer控制台- 应用开发 - 选对应的应用下的 appkey/appsecret
        appConfig.appSecret =
            BuildConfig.TencentIotLinkSDKDemoAppSecret //为explorer平台注册的应用信息(https://console.cloud.tencent.com/iotexplorer/v2/instance/app/detai) explorer控制台- 应用开发 - 选对应的应用下的 appkey/appsecret
        appConfig.autoConfigFromDevice = false
        appConfig.type = XP2PProtocolType.XP2P_PROTOCOL_AUTO
    }

    override fun getContentView(): Int {
        return R.layout.activity_video_test
    }

    override fun initView() {
        productId = intent.getStringExtra("productId")?.toString() ?: ""
        deviceName = intent.getStringExtra("deviceName")?.toString() ?: ""
        xp2pInfo = intent.getStringExtra("p2pInfo")?.toString() ?: ""
        xP2PAppConfig.autoConfigFromDevice = intent.getBooleanExtra("isStartCross", false)
        val protocol = intent.getStringExtra("protocol")?.toString() ?: "auto"
        if (protocol == "udp") {
            xP2PAppConfig.type = XP2PProtocolType.XP2P_PROTOCOL_UDP
        } else if (protocol == "tcp") {
            xP2PAppConfig.type = XP2PProtocolType.XP2P_PROTOCOL_TCP
        } else {
            xP2PAppConfig.type = XP2PProtocolType.XP2P_PROTOCOL_AUTO
        }

        tv_title.text = deviceName
        tv_video_quality.text = getString(R.string.video_quality_medium_str)

        audioRecordUtil = AudioRecordUtil(
            this,
            "${productId}/${deviceName}",
            16000,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        )
//        //变调可以传入pitch参数
//        audioRecordUtil = AudioRecordUtil(this, "${it.productId}/${presenter.getDeviceName()}", 16000, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, -6)
//        //变调可以传入pitch参数
//        audioRecordUtil = AudioRecordUtil(this, "${it.productId}/${presenter.getDeviceName()}", 16000, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, 0, this)
//        audioRecordUtil.recordSpeakFlv(true)

        XP2P.setCallback(this)
        val wm = this.getSystemService(WINDOW_SERVICE) as WindowManager
        val dm = DisplayMetrics()
        wm.defaultDisplay.getMetrics(dm)
        val width = dm.widthPixels // 屏幕宽度（像素）
        val height = dm.heightPixels // 屏幕高度（像素）
        val density = dm.density // 屏幕密度（0.75 / 1.0 / 1.5）
        screenWidth = (width / density).toInt() // 屏幕宽度(dp)
        screenHeight = (height / density).toInt() // 屏幕高度(dp)

        startPlayer()
        startService()
    }

    private fun startPlayer() {
        player = IjkMediaPlayer()
        player.setOnInfoListener(this)
        mHandler.sendEmptyMessageDelayed(MSG_UPDATE_HUD, 500)
    }

    private fun startService() {
        XP2P.startService(
            this,
            productId,
            deviceName,
            xp2pInfo,
            xP2PAppConfig
        )
    }

    private fun restartService() {
        val id = "${productId}/${deviceName}"
        XP2P.stopService(id)
    }

    private fun checkDeviceState() {
        Log.d(tag, "====检测设备状态===")
        launch(Dispatchers.IO) {
            val command = "action=user_define&channel=${channel}&cmd=device_state"
            val retContent = XP2P.postCommandRequestSync(
                "${productId}/${deviceName}",
                command.toByteArray(), command.toByteArray().size.toLong(), 1 * 1000 * 1000
            ) ?: ""
            Log.d(tag, "device_state back content:$retContent")

//            if (retContent.isEmpty()) {
//                launch(Dispatchers.Main) {
//                    restartService()
//                }
//            } else {
            launch(Dispatchers.Main) {
                delegateHttpFlv()
            }
//            }
        }
    }

    private fun delegateHttpFlv() {
        val id = "${productId}/${deviceName}"
//        XP2P.recordstreamPath("/storage/emulated/0/data_video.flv") //自定义采集裸流路径
//        XP2P.recordstream(id) //开启自定义采集裸流
        val prefix = XP2P.delegateHttpFlv(id)
        if (prefix.isNotEmpty()) {
            urlPrefix = prefix
            resetPlayer()
        } else {
            Toast.makeText(this, "get urlPrefix is empty", Toast.LENGTH_SHORT)
                .show()
        }
    }

    override fun setListener() {
        iv_back.setOnClickListener { finish() }
        iv_orientation.setOnClickListener {
            orientationV = !orientationV
            switchOrientation(orientationV)
        }
        tv_video_quality.setOnClickListener(switchVideoQualityListener)
        radio_talk.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked && checkPermissions(permissions)) {
                if (!speakAble(true)) radio_talk.isChecked = false
            } else if (isChecked && !checkPermissions(permissions)) {
                requestPermission(permissions)
            } else {
                speakAble(false)
            }
        }
        radio_record.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                filePath = CommonUtils.generateFileDefaultPath()
                val ret = player.startRecord(filePath)
                if (ret != 0) {
                    ToastDialog(
                        this,
                        ToastDialog.Type.WARNING,
                        getString(R.string.record_failed),
                        2000
                    ).show()
                    radio_record.isChecked = false
                }
            } else {
                player.stopRecord()
                CommonUtils.refreshVideoList(this@VideoTestActivity, filePath)
            }
        }
        radio_playback.setOnClickListener {
            val dev = DevInfo()
            dev.DeviceName = deviceName
            VideoPlaybackActivity.startPlaybackActivity(this@VideoTestActivity, dev)
        }
        radio_photo.setOnClickListener {
            val bitmap = v_preview.getBitmap(player.videoWidth, player.videoHeight)
            ImageSelect.saveBitmap(this@VideoTestActivity, bitmap)
            ToastDialog(
                this,
                ToastDialog.Type.SUCCESS,
                getString(R.string.capture_successed),
                2000
            ).show()
        }
        iv_up.setOnClickListener(controlListener)
        iv_down.setOnClickListener(controlListener)
        iv_right.setOnClickListener(controlListener)
        iv_left.setOnClickListener(controlListener)
        v_preview.surfaceTextureListener = this
        iv_audio.setOnClickListener {
            audioAble = !audioAble
            chgAudioStatus(audioAble)
        }
    }

    private fun chgAudioStatus(audioAble: Boolean) {
        if (!audioAble) {
            iv_audio.setImageResource(R.mipmap.no_audio)
            player.setVolume(0F, 0F)
        } else {
            iv_audio.setImageResource(R.mipmap.audio)
            val audioManager = getSystemService(Service.AUDIO_SERVICE) as AudioManager
            val volume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
            player.setVolume(volume.toFloat(), volume.toFloat())
        }
    }

    private var switchVideoQualityListener = View.OnClickListener {
        if (orientationV) {
            showVVideoQualityDialog()
        } else {
            showHVideoQualityDialog()
        }
    }

    private fun showVVideoQualityDialog() {
        val options = arrayListOf(
            getString(R.string.video_quality_high_str) + " " + getString(R.string.video_quality_high),
            getString(R.string.video_quality_medium_str) + " " + getString(R.string.video_quality_medium),
            getString(R.string.video_quality_low_str) + " " + getString(R.string.video_quality_low)
        )
        val dlg = ListOptionsDialog(this, options)
        dlg.show()
        dlg.setOnDismisListener { chgTextState(it) }
    }

    private fun showHVideoQualityDialog() {
        var pos = -1
        when (tv_video_quality.text.toString()) {
            getString(R.string.video_quality_high_str) -> pos = 2
            getString(R.string.video_quality_medium_str) -> pos = 1
            getString(R.string.video_quality_low_str) -> pos = 0
        }
        val dlg = VideoQualityDialog(this, pos)
        dlg.show()
        btn_layout.visibility = View.GONE
        dlg.setOnDismisListener(object : VideoQualityDialog.OnDismisListener {
            override fun onItemClicked(pos: Int) {
                chgTextState(pos)
            }

            override fun onDismiss() {
                btn_layout.visibility = View.VISIBLE
            }
        })
    }

    private fun switchOrientation(orientation: Boolean) {
        var marginWidth = 0
        val layoutParams = layout_video_preview.layoutParams as ConstraintLayout.LayoutParams
        var fitSize = 0
        var visibility = View.VISIBLE
        var moreSpace = 10
        if (orientation) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
        } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE)
            visibility = View.GONE
            fitSize = ConstraintLayout.LayoutParams.MATCH_PARENT
            marginWidth = 73
            moreSpace = 32
        }

        v_title.visibility = visibility
        layout_content.visibility = visibility

        layoutParams.height = fitSize
        layoutParams.width = fitSize
        layout_video_preview.layoutParams = layoutParams

        val videoLayoutParams = v_preview.layoutParams as ConstraintLayout.LayoutParams
        videoLayoutParams.marginStart = dp2px(marginWidth)
        videoLayoutParams.marginEnd = dp2px(marginWidth)
        v_preview.layoutParams = videoLayoutParams

        val btnLayoutParams = btn_layout.layoutParams as ConstraintLayout.LayoutParams
        btnLayoutParams.bottomMargin = dp2px(moreSpace)
        btn_layout.layoutParams = btnLayoutParams
    }

    private fun chgTextState(value: Int) {
        val url = when (value) {
            0 -> {
                tv_video_quality.setText(R.string.video_quality_high_str)
                Command.getVideoSuperQualityUrlSuffix(channel)
            }

            1 -> {
                tv_video_quality.setText(R.string.video_quality_medium_str)
                Command.getVideoHightQualityUrlSuffix(channel)
            }

            2 -> {
                tv_video_quality.setText(R.string.video_quality_low_str)
                Command.getVideoStandardQualityUrlSuffix(channel)
            }

            else -> ""
        }
        setPlayerUrl(urlPrefix + url)
        chgAudioStatus(audioAble)
    }

    private var controlListener = object : View.OnClickListener {
        override fun onClick(v: View?) {
            var command = ""
            when (v) {
                iv_up -> command = Command.getPtzUpCommand(channel)
                iv_down -> command = Command.getPtzDownCommand(channel)
                iv_right -> command = Command.getPtzRightCommand(channel)
                iv_left -> command = Command.getPtzLeftCommand(channel)
            }

            Thread(Runnable {
                if (command.length <= 0) return@Runnable
                var retContent = XP2P.postCommandRequestSync(
                    "${productId}/${deviceName}",
                    command.toByteArray(), command.toByteArray().size.toLong(), 2 * 1000 * 1000
                ) ?: ""
                launch(Dispatchers.Main) {
                    if (TextUtils.isEmpty(retContent)) {
                        retContent = getString(R.string.command_with_error, command)
                    }
                    Toast.makeText(this@VideoTestActivity, retContent, Toast.LENGTH_SHORT).show()
                }
            }).start()
        }
    }

    private fun speakAble(able: Boolean): Boolean {
        if (able) {
            val command = Command.getNvrIpcStatus(channel, 0)
            val repStatus = XP2P.postCommandRequestSync(
                "${productId}/${deviceName}",
                command.toByteArray(), command.toByteArray().size.toLong(), 2 * 1000 * 1000
            ) ?: ""

            launch(Dispatchers.Main) {
                var retContent = StringBuilder(repStatus).toString()
                if (TextUtils.isEmpty(retContent)) {
                    retContent = getString(R.string.command_with_error, command)
                }
                Toast.makeText(this@VideoTestActivity, retContent, Toast.LENGTH_SHORT).show()
            }

            JSONArray.parseArray(repStatus, DevStatus::class.java)?.let {
                if (it.size == 1 && it.get(0).status == 0) {
                    XP2P.runSendService(
                        "${productId}/${deviceName}",
                        Command.getTwoWayRadio(channel),
                        true
                    )
                    audioRecordUtil?.start()
                    speakAble = true
                    return true
                }
            }

        } else {
            speakAble = false
            audioRecordUtil?.stop()
            XP2P.stopSendService("${productId}/${deviceName}", null)
            return true
        }
        speakAble = false
        return false
    }

    private fun resetPlayer() {
        val url = when (tv_video_quality.text.toString()) {
            getString(R.string.video_quality_high_str) ->
                Command.getVideoSuperQualityUrlSuffix(channel)

            getString(R.string.video_quality_medium_str) -> Command.getVideoHightQualityUrlSuffix(
                channel
            )

            getString(R.string.video_quality_low_str) -> Command.getVideoStandardQualityUrlSuffix(
                channel
            )

            else -> ""
        }
        setPlayerUrl(urlPrefix + url)
    }

    private fun setPlayerUrl(url: String) {
        showTip = false
        startShowVideoTime = System.currentTimeMillis()
        launch(Dispatchers.Main) {
//            layout_video_preview?.removeView(v_preview)
//            layout_video_preview?.addView(v_preview, 0)
            player.setOnInfoListener(this@VideoTestActivity)
            player.reset()
            player.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "analyzemaxduration", 100)
            player.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "probesize", 25 * 1024)
            player.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "packet-buffering", 0)
            player.setOption(IjkMediaPlayer.OPT_CATEGORY_CODEC, "threads", 2)
            player.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "start-on-prepared", 1)
            player.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "sync-av-start", 0)
            player.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "min-frames", 5)
            player.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "max-buffer-size", 512 * 1024)
            player.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec", 1)
            player.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec-auto-rotate", 1)
            player.setOption(
                IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec-handle-resolution-change", 1
            )
            player.setFrameSpeed(1.5f)
            player.setMaxPacketNum(2)
            while (!::surface.isInitialized) {
                delay(50)
                L.e("delay for waiting surface.")
            }
            player.setSurface(surface)
            player.dataSource = url
            player.prepareAsync()
            player.start()
        }
    }

    override fun fail(msg: String?, errorCode: Int) {}

    override fun commandRequest(id: String?, msg: String?) {
        Log.e(tag, "xp2pEventNotify id:$id  msg:$msg")
    }

    override fun xp2pEventNotify(id: String?, msg: String?, event: Int) {
        Log.e(tag, "xp2pEventNotify id:$id  msg:$msg  event:$event")
        if (event == 1003) {
            Log.e(tag, "====event === 1003")
            startShowVideoTime = 0L
            launch(Dispatchers.Main) {
                val content = getString(R.string.disconnected_and_reconnecting, id)
                Toast.makeText(this@VideoTestActivity, content, Toast.LENGTH_SHORT).show()
                XP2P.stopService("${productId}/${deviceName}")
                XP2P.startService(
                    this@VideoTestActivity,
                    productId,
                    deviceName,
                    xp2pInfo,
                    xP2PAppConfig
                )
            }
        } else if (event == 1004 || event == 1005) {
            connectTime = System.currentTimeMillis() - connectStartTime
            if (event == 1004) {
                Log.e(tag, "====event === 1004")
//                checkDeviceState()
                delegateHttpFlv()
            }
        } else if (event == 1010) {
            Log.e(tag, "====event === 1010, 校验失败，info撞库防止串流： $msg")
        }
    }

    override fun avDataRecvHandle(id: String?, data: ByteArray?, len: Int) {
        Log.e(tag, "avDataRecvHandle id:$id  data:$data  len:$data")
    }

    override fun avDataCloseHandle(id: String?, msg: String?, errorCode: Int) {
        Log.e(tag, "avDataCloseHandle id:$id  msg:$msg  errorCode:$errorCode")
    }

    override fun onDeviceMsgArrived(id: String?, data: ByteArray?, len: Int): String {
        Log.e(tag, "onDeviceMsgArrived id:$id  data:$data  len:$len")
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
        Log.d(tag, "onSurfaceTextureSizeChanged")
        val layoutParams = v_preview.layoutParams
        if (orientationV) {
            layoutParams.width = (player.videoWidth * (screenWidth * 16 / 9)) / player.videoHeight
            layoutParams.height = layoutParams.height
        } else {
            layoutParams.width = (player.videoWidth * height) / player.videoHeight
        }
        v_preview.layoutParams = layoutParams
    }

    override fun onSurfaceTextureDestroyed(surface: SurfaceTexture?): Boolean {
        Log.d(tag, "onSurfaceTextureDestroyed")
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
        if (orientationV && firstIn) {
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
        if (radio_talk.isChecked) speakAble(false)
        if (radio_record.isChecked) {
            player.stopRecord()
            CommonUtils.refreshVideoList(this, filePath)
        }
        player.release()
        surface.release()
    }
}