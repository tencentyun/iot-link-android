package com.tencent.iot.explorer.link.demo.video.preview

import android.Manifest
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.graphics.SurfaceTexture
import android.media.AudioFormat
import android.media.AudioManager
import android.os.Bundle
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
import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.JSONArray
import com.tencent.iot.explorer.link.demo.App
import com.tencent.iot.explorer.link.demo.BuildConfig
import com.tencent.iot.explorer.link.demo.R
import com.tencent.iot.explorer.link.demo.common.log.L
import com.tencent.iot.explorer.link.demo.common.util.CommonUtils
import com.tencent.iot.explorer.link.demo.common.util.ImageSelect
import com.tencent.iot.explorer.link.demo.video.Command
import com.tencent.iot.explorer.link.demo.video.DevInfo
import com.tencent.iot.explorer.link.demo.video.VideoPreviewBaseActivity
import com.tencent.iot.explorer.link.demo.video.playback.VideoPlaybackActivity
import com.tencent.iot.explorer.link.demo.video.playback.cloudPlayback.event.ActionListAdapter
import com.tencent.iot.explorer.link.demo.video.playback.cloudPlayback.event.ActionRecord
import com.tencent.iot.explorer.link.demo.video.playback.cloudPlayback.event.EventView
import com.tencent.iot.explorer.link.demo.video.utils.ListOptionsDialog
import com.tencent.iot.explorer.link.demo.video.utils.TipToastDialog
import com.tencent.iot.explorer.link.demo.video.utils.ToastDialog
import com.tencent.iot.explorer.link.demo.video.utils.VolumeChangeObserver
import com.tencent.iot.video.link.consts.VideoConst
import com.tencent.iot.video.link.entity.DeviceStatus
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
import kotlinx.android.synthetic.main.activity_video_preview.list_event
import kotlinx.android.synthetic.main.activity_video_preview.radio_photo
import kotlinx.android.synthetic.main.activity_video_preview.radio_playback
import kotlinx.android.synthetic.main.activity_video_preview.radio_record
import kotlinx.android.synthetic.main.activity_video_preview.radio_talk
import kotlinx.android.synthetic.main.activity_video_preview.today_tip
import kotlinx.android.synthetic.main.activity_video_preview.tv_event_status
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
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import tv.danmaku.ijk.media.player.IMediaPlayer
import tv.danmaku.ijk.media.player.IjkMediaPlayer
import java.lang.ref.WeakReference
import java.util.Locale


class VideoPreviewActivity : VideoPreviewBaseActivity(), EventView,
    TextureView.SurfaceTextureListener,
    XP2PCallback, CoroutineScope by MainScope(), VolumeChangeObserver.VolumeChangeListener,
    IMediaPlayer.OnInfoListener {

    open var tag = VideoPreviewActivity::class.simpleName
    var orientationV = true
    private var adapter: ActionListAdapter? = null
    private var records: MutableList<ActionRecord> = ArrayList()
    lateinit var player: IjkMediaPlayer
    lateinit var surface: Surface

    @Volatile
    var audioAble = true
    var filePath: String? = null
    lateinit var audioRecordUtil: AudioRecordUtil
    var permissions = arrayOf(Manifest.permission.RECORD_AUDIO)

    @Volatile
    var showTip = false

    @Volatile
    var connectStartTime = 0L

    @Volatile
    var connectTime = 0L

    @Volatile
    var startShowVideoTime = 0L

    @Volatile
    var showVideoTime = 0L
    var volumeChangeObserver: VolumeChangeObserver? = null
    val MSG_UPDATE_HUD = 1

    var screenWidth = 0
    var screenHeight = 0
    var firstIn = true

    @Volatile
    var speakAble = false

    private var isRestart: Boolean = false

    val xP2PAppConfig = XP2PAppConfig().also { appConfig ->
        appConfig.appKey =
            BuildConfig.TencentIotLinkSDKDemoAppkey //为explorer平台注册的应用信息(https://console.cloud.tencent.com/iotexplorer/v2/instance/app/detai) explorer控制台- 应用开发 - 选对应的应用下的 appkey/appsecret
        appConfig.appSecret =
            BuildConfig.TencentIotLinkSDKDemoAppSecret //为explorer平台注册的应用信息(https://console.cloud.tencent.com/iotexplorer/v2/instance/app/detai) explorer控制台- 应用开发 - 选对应的应用下的 appkey/appsecret
        appConfig.autoConfigFromDevice = false //是否启动跟随配置，需要控制台配置
        appConfig.type = XP2PProtocolType.XP2P_PROTOCOL_AUTO
    }

    override fun getContentView(): Int {
        return R.layout.activity_video_preview
    }

    override fun updateXp2pInfo(xp2pInfo: String) {
        Log.d(tag, "update xp2p2Info:$xp2pInfo")
        startService()
    }

    override fun initView() {
        tv_title.text = presenter.getDeviceName()
        adapter = ActionListAdapter(this@VideoPreviewActivity, records)
        list_event.adapter = adapter

        tv_video_quality.setText(R.string.video_quality_medium_str)
        today_tip.setText(getString(R.string.today) + " " + CommonUtils.getWeekDay(this@VideoPreviewActivity))
        records.clear()
        tv_event_status.visibility = View.VISIBLE
        tv_event_status.setText(R.string.loading)
        audioRecordUtil = AudioRecordUtil(
            this,
            "${presenter.getProductId()}/${presenter.getDeviceName()}",
            16000,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        )
//        //变调可以传入pitch参数
//        audioRecordUtil = AudioRecordUtil(this, "${it.productId}/${presenter.getDeviceName()}", 16000, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, -6)
//        //变调可以传入pitch参数
//        audioRecordUtil = AudioRecordUtil(this, "${it.productId}/${presenter.getDeviceName()}", 16000, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, 0, this)
//        audioRecordUtil.recordSpeakFlv(true)
        getDeviceP2PInfo()
        XP2P.setCallback(this)
        //实例化对象并设置监听器
        volumeChangeObserver = VolumeChangeObserver(this)
        volumeChangeObserver?.volumeChangeListener = this
        volumeChangeObserver?.registerReceiver()

        val wm = this.getSystemService(WINDOW_SERVICE) as WindowManager
        val dm = DisplayMetrics()
        wm.defaultDisplay.getMetrics(dm)
        val width = dm.widthPixels // 屏幕宽度（像素）
        val height = dm.heightPixels // 屏幕高度（像素）
        val density = dm.density // 屏幕密度（0.75 / 1.0 / 1.5）
        screenWidth = (width / density).toInt() // 屏幕宽度(dp)
        screenHeight = (height / density).toInt() // 屏幕高度(dp)
        startPlayer()
    }

    private fun startService() {
        Log.d(tag, "startService")
        XP2P.startService(
            this,
            presenter.getProductId(),
            presenter.getDeviceName(),
            xp2pInfo,
            xP2PAppConfig
        )
    }

    private fun checkDeviceState() {
        Log.d(tag, "====检测设备状态===")
        launch(Dispatchers.IO) {
            getDeviceStatus("${presenter.getProductId()}/${presenter.getDeviceName()}") { isOnline, msg ->
                launch(Dispatchers.Main) {
                    Toast.makeText(this@VideoPreviewActivity, msg, Toast.LENGTH_SHORT).show()
                    if (isOnline) {
                        isRestart = false
                        delegateHttpFlv()
                    } else {
                        restartService()
                    }
                }
            }
        }
    }

    private fun restartService() {
        Log.d(tag, "====开始重连===")
        XP2P.stopService("${presenter.getProductId()}/${presenter.getDeviceName()}")
        getDeviceP2PInfo()
    }

    private fun delegateHttpFlv() {
        val id = "${presenter.getProductId()}/${presenter.getDeviceName()}"
//        XP2P.recordstreamPath("/storage/emulated/0/data_video.flv") //自定义采集裸流路径
//        XP2P.recordstream(id) //开启自定义采集裸流
        val prefix = XP2P.delegateHttpFlv(id)
        if (prefix.isNotEmpty()) {
            resetPlayer(prefix)
        } else {
            Toast.makeText(this@VideoPreviewActivity, "get urlPrefix is empty", Toast.LENGTH_SHORT)
                .show()
        }
    }


    open fun startPlayer() {
        if (App.data.accessInfo == null || TextUtils.isEmpty(presenter.getDeviceName())) return
        player = IjkMediaPlayer()
        player.setOnInfoListener(this)
        mHandler.sendEmptyMessageDelayed(MSG_UPDATE_HUD, 500)
    }

    override fun onSurfaceTextureAvailable(surface: SurfaceTexture?, width: Int, height: Int) {
        Log.d(tag, "onSurfaceTextureAvailable")
        surface?.let {
            this.surface = Surface(surface)
            player.setSurface(this.surface)
        }
    }

    private fun speakAble(able: Boolean): Boolean {
        App.data.accessInfo?.let { accessInfo ->
            if (able) {
                val command = Command.getNvrIpcStatus(presenter.getChannel(), 0)
                val repStatus = XP2P.postCommandRequestSync(
                    "${accessInfo.productId}/${presenter.getDeviceName()}",
                    command.toByteArray(), command.toByteArray().size.toLong(), 2 * 1000 * 1000
                ) ?: ""

                launch(Dispatchers.Main) {
                    var retContent = StringBuilder(repStatus).toString()
                    if (TextUtils.isEmpty(retContent)) {
                        retContent = getString(R.string.command_with_error, command)
                    }
                    Toast.makeText(this@VideoPreviewActivity, retContent, Toast.LENGTH_SHORT).show()
                }

                JSONArray.parseArray(repStatus, DevStatus::class.java)?.let {
                    if (it.size == 1 && it.get(0).status == 0) {
                        XP2P.runSendService(
                            "${accessInfo.productId}/${presenter.getDeviceName()}",
                            Command.getTwoWayRadio(presenter.getChannel()),
                            true
                        )
                        audioRecordUtil.start()
                        speakAble = true
                        return true
                    }
                }
            } else {
                speakAble = false
                audioRecordUtil.stop()
                XP2P.stopSendService("${accessInfo.productId}/${presenter.getDeviceName()}", null)
                return true
            }
        }
        speakAble = false
        return false
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
                CommonUtils.refreshVideoList(this@VideoPreviewActivity, filePath)
            }
        }
        radio_playback.setOnClickListener {
            val dev = DevInfo()
            dev.DeviceName = presenter.getDeviceName()
            VideoPlaybackActivity.startPlaybackActivity(this@VideoPreviewActivity, dev)
        }
        radio_photo.setOnClickListener {
            val bitmap = v_preview.getBitmap(player.videoWidth, player.videoHeight)
            ImageSelect.saveBitmap(this@VideoPreviewActivity, bitmap)
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
        adapter?.setOnItemClicked(onItemVideoClicked)
        v_preview.surfaceTextureListener = this
        iv_audio.setOnClickListener {
            audioAble = !audioAble
            chgAudioStatus(audioAble)
        }
    }

    open fun chgAudioStatus(audioAble: Boolean) {
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

    open var controlListener = object : View.OnClickListener {
        override fun onClick(v: View?) {
            var command = ""
            when (v) {
                iv_up -> command = Command.getPtzUpCommand(presenter.getChannel())
                iv_down -> command = Command.getPtzDownCommand(presenter.getChannel())
                iv_right -> command = Command.getPtzRightCommand(presenter.getChannel())
                iv_left -> command = Command.getPtzLeftCommand(presenter.getChannel())
            }

            Thread(Runnable {
                App.data.accessInfo?.let {
                    if (command.length <= 0) return@Runnable
                    var retContent = XP2P.postCommandRequestSync(
                        "${it.productId}/${presenter.getDeviceName()}",
                        command.toByteArray(), command.toByteArray().size.toLong(), 2 * 1000 * 1000
                    ) ?: ""
                    launch(Dispatchers.Main) {
                        if (TextUtils.isEmpty(retContent)) {
                            retContent = getString(R.string.command_with_error, command)
                        }
                        Toast.makeText(this@VideoPreviewActivity, retContent, Toast.LENGTH_SHORT)
                            .show()
                    }
                }
            }).start()
        }
    }

    private var onItemVideoClicked = object : ActionListAdapter.OnItemClicked {
        override fun onItemVideoClicked(pos: Int) {
            radio_playback.performClick()
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
        val dlg = ListOptionsDialog(this@VideoPreviewActivity, options)
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
        val dlg = VideoQualityDialog(this@VideoPreviewActivity, pos)
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

    private fun chgTextState(value: Int) {
        val url = when (value) {
            0 -> {
                tv_video_quality.setText(R.string.video_quality_high_str)
                prefix + Command.getVideoSuperQualityUrlSuffix(presenter.getChannel())
            }

            1 -> {
                tv_video_quality.setText(R.string.video_quality_medium_str)
                prefix + Command.getVideoHightQualityUrlSuffix(presenter.getChannel())
            }

            2 -> {
                tv_video_quality.setText(R.string.video_quality_low_str)
                prefix + Command.getVideoStandardQualityUrlSuffix(presenter.getChannel())
            }

            else -> ""
        }
        setPlayerUrl(url)
        chgAudioStatus(audioAble)
    }

    private fun setPlayerUrl(url: String) {
        showTip = false
        startShowVideoTime = System.currentTimeMillis()
        launch(Dispatchers.Main) {
            layout_video_preview?.removeView(v_preview)
            layout_video_preview?.addView(v_preview, 0)
            player.setOnInfoListener(this@VideoPreviewActivity)
            player.reset()
            player.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "analyzemaxduration", 100)
            player.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "probesize", 50 * 1024)
            player.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "packet-buffering", 0)
            player.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "start-on-prepared", 1)
            player.setOption(IjkMediaPlayer.OPT_CATEGORY_CODEC, "threads", 1)
            player.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "sync-av-start", 0)
            player.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec", 1)
            player.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec-auto-rotate", 1)
            player.setOption(
                IjkMediaPlayer.OPT_CATEGORY_PLAYER,
                "mediacodec-handle-resolution-change",
                1
            )
            player.setFrameSpeed(1.5f)
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

    override fun eventReady(events: MutableList<ActionRecord>) {
        if (events.size <= 0) {
            launch(Dispatchers.Main) {
                tv_event_status.setText(R.string.no_data)
            }
            return
        }

        launch(Dispatchers.Main) {
            tv_event_status.visibility = View.GONE
            records.addAll(events)
            adapter?.notifyDataSetChanged()
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

    override fun fail(msg: String?, errorCode: Int) {}
    override fun commandRequest(id: String?, msg: String?) {}
    override fun avDataRecvHandle(id: String?, data: ByteArray?, len: Int) {}
    override fun avDataCloseHandle(id: String?, msg: String?, errorCode: Int) {}
    override fun onDeviceMsgArrived(id: String?, data: ByteArray?, len: Int): String {
        return "app reply to device"
    }

    override fun xp2pEventNotify(id: String?, msg: String?, event: Int) {
        Log.e(tag, "id=${id}, event=${event}")
        if (event == 1003) {
            Log.e(tag, "====event === 1003")
            startShowVideoTime = 0L
            launch(Dispatchers.Main) {
                val content = getString(R.string.disconnected_and_reconnecting, id)
                Toast.makeText(this@VideoPreviewActivity, content, Toast.LENGTH_SHORT).show()
                if (!isRestart) {
                    restartService()
                    isRestart = true
                }
            }
        } else if (event == 1004 || event == 1005) {
            connectTime = System.currentTimeMillis() - connectStartTime
            if (event == 1004) {
                Log.e(tag, "====event === 1004")
                checkDeviceState()
            }
        } else if (event == 1010) {
            Log.e(tag, "====event === 1010, 校验失败，info撞库防止串流： $msg")
        }
    }

    private var prefix: String = ""
    private fun resetPlayer(prefix: String) {
        this.prefix = prefix
        val url = when (tv_video_quality.text.toString()) {
            getString(R.string.video_quality_high_str) -> prefix + Command.getVideoSuperQualityUrlSuffix(
                presenter.getChannel()
            )

            getString(R.string.video_quality_medium_str) -> prefix + Command.getVideoHightQualityUrlSuffix(
                presenter.getChannel()
            )

            getString(R.string.video_quality_low_str) -> prefix + Command.getVideoStandardQualityUrlSuffix(
                presenter.getChannel()
            )

            else -> ""
        }
        setPlayerUrl(url)
    }

    override fun onPause() {
        super.onPause()
        finishPlayer()
    }

    private fun finishPlayer() {
        mHandler.removeMessages(MSG_UPDATE_HUD)
        if (radio_talk.isChecked) speakAble(false)
        if (radio_record.isChecked) {
            player.stopRecord()
            CommonUtils.refreshVideoList(this@VideoPreviewActivity, filePath)
        }
        player.release()
        surface.release()
    }

    override fun onDestroy() {
        super.onDestroy()
        finishPlayer()
        XP2P.stopService("${presenter.getProductId()}/${presenter.getDeviceName()}")
        XP2P.setCallback(null)
        cancel()
        volumeChangeObserver?.unregisterReceiver();
    }

    companion object {
        fun startPreviewActivity(context: Context?, dev: DevInfo) {
            context ?: let { return }

            val intent = Intent(context, VideoPreviewActivity::class.java)
            val bundle = Bundle()
            intent.putExtra(VideoConst.VIDEO_CONFIG, bundle)
            val devInfo = DevUrl2Preview()
            devInfo.devName = dev.DeviceName
            devInfo.Status = dev.Status
            devInfo.channel = dev.Channel
            bundle.putString(VideoConst.VIDEO_CONFIG, JSON.toJSONString(devInfo))
            context.startActivity(intent)
        }
    }

    override fun onVolumeChanged(volume: Int) {
        if (audioAble) {
            player.setVolume(volume.toFloat(), volume.toFloat())
        }
    }

    private fun getDeviceStatus(id: String?, block: ((Boolean, String) -> Unit)? = null) {
        var command: ByteArray? = null
        when (tv_video_quality.text.toString()) {
            getString(R.string.video_quality_high_str) -> {
                command =
                    "action=inner_define&channel=0&cmd=get_device_st&type=live&quality=super".toByteArray()
            }

            getString(R.string.video_quality_medium_str) -> {
                command =
                    "action=inner_define&channel=0&cmd=get_device_st&type=live&quality=high".toByteArray()
            }

            getString(R.string.video_quality_low_str) -> {
                command =
                    "action=inner_define&channel=0&cmd=get_device_st&type=live&quality=standard".toByteArray()
            }
        }
        val reponse =
            XP2P.postCommandRequestSync(id, command, command!!.size.toLong(), 2 * 1000 * 1000)
        if (!TextUtils.isEmpty(reponse)) {
            val deviceStatuses: List<DeviceStatus> =
                JSONArray.parseArray(reponse, DeviceStatus::class.java)
            // 0   接收请求
            // 1   拒绝请求
            // 404 error request message
            // 405 connect number too many
            // 406 current command don't support
            // 407 device process error
            var deviceState: Int = -1
            var msg: String = ""
            if (deviceStatuses.isNotEmpty()) {
                msg = when (deviceStatuses[0].status) {
                    0 -> "设备状态正常"
                    404 -> "设备状态异常, error request message: $reponse"
                    405 -> "设备状态异常, connect number too many: $reponse"
                    406 -> "设备状态异常, current command don't support: $reponse"
                    407 -> "设备状态异常, device process error: $reponse"
                    else -> "设备状态异常, 拒绝请求: $reponse"
                }
                deviceState = deviceStatuses[0].status
            } else {
                msg = "获取设备状态失败"
            }
            block?.invoke(deviceState == 0, msg)
        }
    }

    private val mHandler = MyHandler(this)

    private class MyHandler(activity: VideoPreviewActivity) : Handler() {
        private val mActivity: WeakReference<VideoPreviewActivity> = WeakReference(activity)
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

    override fun onInfo(mp: IMediaPlayer?, what: Int, extra: Int): Boolean {
        return true
    }

    override fun onInfoSEI(
        mp: IMediaPlayer?,
        what: Int,
        extra: Int,
        sei_content: String?
    ): Boolean {
        return false
    }

    override fun onInfoAudioPcmData(mp: IMediaPlayer?, arrPcm: ByteArray?, length: Int) {
//        if (audioRecordUtil != null && length > 0 && speakAble) {
//            audioRecordUtil.setPlayerPcmData(arrPcm);
//        }
    }
}