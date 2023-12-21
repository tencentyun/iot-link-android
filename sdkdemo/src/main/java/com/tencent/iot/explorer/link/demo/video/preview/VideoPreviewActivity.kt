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
import androidx.recyclerview.widget.LinearLayoutManager
import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.JSONArray
import com.tencent.iot.explorer.link.demo.App
import com.tencent.iot.explorer.link.demo.R
import com.tencent.iot.explorer.link.demo.VideoBaseActivity
import com.tencent.iot.explorer.link.demo.common.log.L
import com.tencent.iot.explorer.link.demo.common.util.CommonUtils
import com.tencent.iot.explorer.link.demo.common.util.ImageSelect
import com.tencent.iot.explorer.link.demo.video.Command
import com.tencent.iot.explorer.link.demo.video.DevInfo
import com.tencent.iot.explorer.link.demo.video.playback.VideoPlaybackActivity
import com.tencent.iot.explorer.link.demo.video.playback.cloudPlayback.event.ActionListAdapter
import com.tencent.iot.explorer.link.demo.video.playback.cloudPlayback.event.ActionRecord
import com.tencent.iot.explorer.link.demo.video.playback.cloudPlayback.event.EventPresenter
import com.tencent.iot.explorer.link.demo.video.playback.cloudPlayback.event.EventView
import com.tencent.iot.explorer.link.demo.video.utils.ListOptionsDialog
import com.tencent.iot.explorer.link.demo.video.utils.TipToastDialog
import com.tencent.iot.explorer.link.demo.video.utils.ToastDialog
import com.tencent.iot.explorer.link.demo.video.utils.VolumeChangeObserver
import com.tencent.iot.video.link.consts.VideoConst
import com.tencent.iot.video.link.entity.DeviceStatus
import com.tencent.iot.video.link.util.audio.AudioRecordUtil
import com.tencent.iot.video.link.util.audio.OnReadAECProcessedPcmListener
import com.tencent.xnet.XP2P
import com.tencent.xnet.XP2PCallback
import kotlinx.android.synthetic.main.activity_video_preview.*
import kotlinx.android.synthetic.main.dash_board_layout.*
import kotlinx.android.synthetic.main.fragment_video_cloud_playback.*
import kotlinx.android.synthetic.main.title_layout.*
import kotlinx.coroutines.*
import tv.danmaku.ijk.media.player.IjkMediaPlayer
import java.lang.Runnable
import java.lang.ref.WeakReference
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CountDownLatch
import java.util.concurrent.LinkedBlockingDeque


private var countDownLatchs : MutableMap<String, CountDownLatch> = ConcurrentHashMap()
private var keepPlayThreadLock = Object()
@Volatile
private var keepAliveThreadRuning = true

class VideoPreviewActivity : VideoBaseActivity(), EventView, TextureView.SurfaceTextureListener,
    XP2PCallback, CoroutineScope by MainScope(), VolumeChangeObserver.VolumeChangeListener{

    open var tag = VideoPreviewActivity::class.simpleName
    var orientationV = true
    private var adapter : ActionListAdapter? = null
    private var records : MutableList<ActionRecord> = ArrayList()
    lateinit var presenter: EventPresenter
    lateinit var player : IjkMediaPlayer
    lateinit var surface: Surface
    @Volatile
    var audioAble = true
    @Volatile
    var urlPrefix = ""
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

    override fun getContentView(): Int {
        return R.layout.activity_video_preview
    }

    override fun onResume() {
        super.onResume()
        keepAliveThreadRuning = true
        startPlayer()
    }

    override fun initView() {
        presenter = EventPresenter(this@VideoPreviewActivity)
        var bundle = intent.getBundleExtra(VideoConst.VIDEO_CONFIG)
        bundle?.let {
            var videoConfig = bundle.getString(VideoConst.VIDEO_CONFIG)
            if (TextUtils.isEmpty(videoConfig)) return@let

            var devInfo = JSON.parseObject(videoConfig, DevUrl2Preview::class.java)
            devInfo?.let {
                tv_title.setText(it.devName)
                presenter.setDeviceName(it.devName)
                presenter.setChannel(it.channel)
            }
        }

        var linearLayoutManager = LinearLayoutManager(this@VideoPreviewActivity)
        adapter = ActionListAdapter(this@VideoPreviewActivity, records)
        list_event.layoutManager = linearLayoutManager
        list_event.adapter = adapter

        tv_video_quality.setText(R.string.video_quality_medium_str)
        today_tip.setText(getString(R.string.today) + " " + CommonUtils.getWeekDay(this@VideoPreviewActivity))
        records.clear()
        App.data.accessInfo?.let {
            presenter.setAccessId(it.accessId)
            presenter.setAccessToken(it.accessToken)
            presenter.setProductId(it.productId)
            presenter.getEventsData(Date())
            tv_event_status.visibility = View.VISIBLE
            tv_event_status.setText(R.string.loading)
            audioRecordUtil = AudioRecordUtil(this, "${it.productId}/${presenter.getDeviceName()}", 16000, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT)
//            //变调可以传入pitch参数
//            audioRecordUtil = AudioRecordUtil(this, "${it.productId}/${presenter.getDeviceName()}", 16000, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, -6)
            audioRecordUtil.recordSpeakFlv(true)
        }

        XP2P.setCallback(this)
        XP2P.startService(this@VideoPreviewActivity, "${App.data.accessInfo!!.productId}/${presenter.getDeviceName()}",
            App.data.accessInfo!!.productId, presenter.getDeviceName()
        )

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
    }

    open fun startPlayer() {
        if (App.data.accessInfo == null || TextUtils.isEmpty(presenter.getDeviceName())) return
        player = IjkMediaPlayer()
        mHandler.sendEmptyMessageDelayed(MSG_UPDATE_HUD, 500)

        Thread(Runnable {
            val id = "${App.data.accessInfo!!.productId}/${presenter.getDeviceName()}"
            connectStartTime = System.currentTimeMillis()
            val ret = XP2P.setParamsForXp2pInfo(id, App.data.accessInfo!!.accessId,
                App.data.accessInfo!!.accessToken, "")
            if (ret != 0) {
                launch(Dispatchers.Main) {
                    val errInfo: String
                    if (ret.toString() == "-1007") {
                        errInfo = getString(R.string.xp2p_err_version)
                    } else {
                        errInfo = getString(R.string.error_with_code, id, ret.toString())
                    }
                    Toast.makeText(this@VideoPreviewActivity, errInfo, Toast.LENGTH_SHORT).show()
                }
                return@Runnable
            }
            XP2P.delegateHttpFlv(id)?.let {
                urlPrefix = it
                if (!TextUtils.isEmpty(urlPrefix)) {
                    player.let {
                        resetPlayer()
                        keepPlayerplay(id)
                    }
                }
            }
        }).start()
    }

    private fun keepPlayerplay(id: String?) {
        if (TextUtils.isEmpty(id)) return
        val accessId = App.data.accessInfo!!.accessId
        val accessToken = App.data.accessInfo!!.accessToken
        // 开启守护线程
        Thread{
            val objectLock = Object()
            while (true) {
                Log.d(tag, "id=${id} keepAliveThread wait disconnected msg")
                synchronized(keepPlayThreadLock) {
                    keepPlayThreadLock.wait()
                }
                Log.d(tag, "id=${id} keepAliveThread do not wait and keepAliveThreadRuning=${keepAliveThreadRuning}")
                if (!keepAliveThreadRuning) break //锁被释放后，检查守护线程是否继续运行

                // 发现断开尝试恢复视频，每隔一秒尝试一次
                Log.d(tag, "====开始尝试重连...")
                XP2P.stopService(id)
                while (XP2P.startService(this@VideoPreviewActivity, id, App.data.accessInfo!!.productId, presenter.getDeviceName())!=0
                    || XP2P.setParamsForXp2pInfo(id, accessId, accessToken, "") != 0
                    || getDeviceStatus(id) != 0) {
                    XP2P.stopService(id)
                    synchronized(objectLock) {
                        objectLock.wait(500)
                    }
                    Log.d(tag, "====正在重连...")
                }
                connectStartTime = System.currentTimeMillis()

                Log.d(tag, "====尝试拉流...")
                XP2P.delegateHttpFlv(id)?.let {
                    urlPrefix = it
                    if (!TextUtils.isEmpty(urlPrefix)) resetPlayer()
                }
            }
        }.start()
    }

    private fun resetPlayer() {
        when (tv_video_quality.text.toString()) {
            getString(R.string.video_quality_high_str) -> setPlayerUrl(Command.getVideoSuperQualityUrlSuffix(presenter.getChannel()))
            getString(R.string.video_quality_medium_str) -> setPlayerUrl(Command.getVideoHightQualityUrlSuffix(presenter.getChannel()))
            getString(R.string.video_quality_low_str) -> setPlayerUrl(Command.getVideoStandardQualityUrlSuffix(presenter.getChannel()))
        }
    }

    override fun onSurfaceTextureAvailable(surface: SurfaceTexture?, width: Int, height: Int) {
        surface?.let {
            this.surface = Surface(surface)
            player.setSurface(this.surface)
        }
    }

    open fun speakAble(able: Boolean): Boolean {
        App.data.accessInfo?.let { accessInfo ->
            if (able) {
                var command = Command.getNvrIpcStatus(presenter.getChannel(), 0)
                var repStatus = XP2P.postCommandRequestSync("${accessInfo.productId}/${presenter.getDeviceName()}",
                    command.toByteArray(), command.toByteArray().size.toLong(), 2 * 1000 * 1000) ?:""

                launch(Dispatchers.Main) {
                    var retContent = StringBuilder(repStatus).toString()
                    if (TextUtils.isEmpty(retContent)) {
                        retContent = getString(R.string.command_with_error, command)
                    }
                    Toast.makeText(this@VideoPreviewActivity, retContent, Toast.LENGTH_SHORT).show()
                }

                JSONArray.parseArray(repStatus, DevStatus::class.java)?.let {
                    if (it.size == 1 && it.get(0).status == 0) {
                        XP2P.runSendService("${accessInfo.productId}/${presenter.getDeviceName()}", Command.getTwoWayRadio(presenter.getChannel()), true)
                        audioRecordUtil.setPlayer(player)
                        audioRecordUtil.start()
                        return true
                    }
                }

            } else {
                audioRecordUtil.stop()
                XP2P.stopSendService("${accessInfo.productId}/${presenter.getDeviceName()}", null)
                return true
            }
        }
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
                var ret = player.startRecord(filePath)
                if (ret != 0) {
                    ToastDialog(this, ToastDialog.Type.WARNING, getString(R.string.record_failed), 2000).show()
                    radio_record.isChecked = false
                }
            } else {
                player.stopRecord()
                CommonUtils.refreshVideoList(this@VideoPreviewActivity, filePath)
            }
        }
        radio_playback.setOnClickListener {
            var dev = DevInfo()
            dev.DeviceName = presenter.getDeviceName()
            VideoPlaybackActivity.startPlaybackActivity(this@VideoPreviewActivity, dev)
        }
        radio_photo.setOnClickListener {
            val bitmap = v_preview.getBitmap(player.videoWidth, player.videoHeight)
            ImageSelect.saveBitmap(this@VideoPreviewActivity, bitmap)
            ToastDialog(this, ToastDialog.Type.SUCCESS, getString(R.string.capture_successed), 2000).show()
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
            var audioManager = getSystemService(Service.AUDIO_SERVICE) as AudioManager
            var volume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
            player.setVolume(volume.toFloat(), volume.toFloat())
        }
    }

    open var controlListener = object : View.OnClickListener {
        override fun onClick(v: View?) {
            var command = ""
            when(v) {
                iv_up -> command = Command.getPtzUpCommand(presenter.getChannel())
                iv_down -> command = Command.getPtzDownCommand(presenter.getChannel())
                iv_right -> command = Command.getPtzRightCommand(presenter.getChannel())
                iv_left -> command = Command.getPtzLeftCommand(presenter.getChannel())
            }

            Thread(Runnable {
                App.data.accessInfo?.let {
                    if (command.length <= 0) return@Runnable
                    var retContent = XP2P.postCommandRequestSync("${it.productId}/${presenter.getDeviceName()}",
                        command.toByteArray(), command.toByteArray().size.toLong(), 2 * 1000 * 1000)?:""
                    launch(Dispatchers.Main) {
                        if (TextUtils.isEmpty(retContent)) {
                            retContent = getString(R.string.command_with_error, command)
                        }
                        Toast.makeText(this@VideoPreviewActivity, retContent, Toast.LENGTH_SHORT).show()
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

    private var switchVideoQualityListener = object : View.OnClickListener {
        override fun onClick(v: View?) {
            if (orientationV) {
                showVVideoQualityDialog()
            } else {
                showHVideoQualityDialog()
            }
        }
    }

    private fun showVVideoQualityDialog() {
        var options = arrayListOf(getString(R.string.video_quality_high_str) + " " + getString(R.string.video_quality_high),
                getString(R.string.video_quality_medium_str) + " " + getString(R.string.video_quality_medium),
                getString(R.string.video_quality_low_str) + " " + getString(R.string.video_quality_low))
        var dlg = ListOptionsDialog(this@VideoPreviewActivity, options)
        dlg.show()
        dlg.setOnDismisListener { chgTextState(it) }
    }

    private fun showHVideoQualityDialog() {
        var pos = -1
        when(tv_video_quality.text.toString()) {
            getString(R.string.video_quality_high_str) -> pos = 2
            getString(R.string.video_quality_medium_str) -> pos = 1
            getString(R.string.video_quality_low_str) -> pos = 0
        }
        var dlg = VideoQualityDialog(this@VideoPreviewActivity, pos)
        dlg.show()
        btn_layout.visibility = View.GONE
        dlg.setOnDismisListener(object : VideoQualityDialog.OnDismisListener {
            override fun onItemClicked(pos: Int) { chgTextState(pos) }
            override fun onDismiss() { btn_layout.visibility = View.VISIBLE }
        })
    }

    open fun chgTextState(value: Int) {
        when(value) {
            0 -> {
                tv_video_quality.setText(R.string.video_quality_high_str)
                setPlayerUrl(Command.getVideoSuperQualityUrlSuffix(presenter.getChannel()))
            }
            1 -> {
                tv_video_quality.setText(R.string.video_quality_medium_str)
                setPlayerUrl(Command.getVideoHightQualityUrlSuffix(presenter.getChannel()))
            }
            2 -> {
                tv_video_quality.setText(R.string.video_quality_low_str)
                setPlayerUrl(Command.getVideoStandardQualityUrlSuffix(presenter.getChannel()))
            }
        }

        chgAudioStatus(audioAble)
    }

    open fun setPlayerUrl(suffix: String) {
        showTip = false
        startShowVideoTime = System.currentTimeMillis()
        player.release()
        launch (Dispatchers.Main) {
            layout_video_preview?.removeView(v_preview)
            layout_video_preview?.addView(v_preview, 0)

            player = IjkMediaPlayer()
            player._setApmStatus(true)
            player?.let {
                val url = urlPrefix + suffix
                it.reset()

                it.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "analyzemaxduration", 100)
                it.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "probesize", 50 * 1024)
                it.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "packet-buffering", 0)
                it.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "start-on-prepared", 1)
                it.setOption(IjkMediaPlayer.OPT_CATEGORY_CODEC, "threads", 1)
                it.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "sync-av-start", 0)
                it.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec",1)
                it.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec-auto-rotate", 1)
                it.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec-handle-resolution-change", 1)

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

    private fun switchOrientation(orientation : Boolean) {
        var marginWidth = 0
        var layoutParams = layout_video_preview.layoutParams as ConstraintLayout.LayoutParams
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

        var videoLayoutParams = v_preview.layoutParams as ConstraintLayout.LayoutParams
        videoLayoutParams.marginStart = dp2px(marginWidth)
        videoLayoutParams.marginEnd = dp2px(marginWidth)
        v_preview.layoutParams = videoLayoutParams

        var btnLayoutParams = btn_layout.layoutParams as ConstraintLayout.LayoutParams
        btnLayoutParams.bottomMargin = dp2px(moreSpace)
        btn_layout.layoutParams = btnLayoutParams
    }

    override fun eventReady(events: MutableList<ActionRecord>) {
        if (events == null || events.size <= 0) {
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
        val layoutParams = v_preview.layoutParams
        if (orientationV) {
            layoutParams.width = (player.videoWidth * (screenWidth * 16 / 9)) / player.videoHeight
            layoutParams.height = layoutParams.height
        } else {
            layoutParams.width = (player.videoWidth * height) / player.videoHeight
        }
        v_preview.layoutParams = layoutParams

    }
    override fun onSurfaceTextureDestroyed(surface: SurfaceTexture?): Boolean { return false }
    override fun onSurfaceTextureUpdated(surface: SurfaceTexture?) {
        if (!showTip && startShowVideoTime > 0) {
            showVideoTime = System.currentTimeMillis() - startShowVideoTime
            var content = getString(R.string.time_2_show, connectTime.toString(), showVideoTime.toString())
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
            keepPlayThreadLock?.let {
                synchronized(it) {
                    Log.d(tag, "====p2p链路断开, event=$event.")
                    it.notify()
                }
            } // 唤醒守护线程
            launch(Dispatchers.Main) {
                var content = getString(R.string.disconnected_and_reconnecting, id)
                Toast.makeText(this@VideoPreviewActivity, content, Toast.LENGTH_SHORT).show()
            }
        } else if (event == 1004 || event == 1005) {
            connectTime = System.currentTimeMillis() - connectStartTime
            if (event == 1004) {
                Log.e(tag, "====event === 1004")
            }
        }
    }

    override fun onPause() {
        super.onPause()
        finishPlayer()
    }

    private fun finishPlayer() {
        mHandler.removeMessages(MSG_UPDATE_HUD)
        player?._setApmStatus(false);
        player?.release()
        if (radio_talk.isChecked) speakAble(false)
        if (radio_record.isChecked) {
            player.stopRecord()
            CommonUtils.refreshVideoList(this@VideoPreviewActivity, filePath)
        }

        countDownLatchs.clear()
        // 关闭守护线程
        keepAliveThreadRuning = false
        keepPlayThreadLock?.let {
            synchronized(it) {
                it.notify()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        finishPlayer()
        App.data.accessInfo?.let {
            XP2P.stopService("${it.productId}/${presenter.getDeviceName()}")
        }
        XP2P.setCallback(null)
        cancel()
        volumeChangeObserver?.unregisterReceiver();
    }

    companion object {
        fun startPreviewActivity(context: Context?, dev: DevInfo) {
            context?:let { return }

            var intent = Intent(context, VideoPreviewActivity::class.java)
            var bundle = Bundle()
            intent.putExtra(VideoConst.VIDEO_CONFIG, bundle)
            var devInfo = DevUrl2Preview()
            devInfo.devName = dev.DeviceName
            devInfo.Status = dev.Status
            devInfo.channel = dev.Channel
            bundle.putString(VideoConst.VIDEO_CONFIG, JSON.toJSONString(devInfo))
            context.startActivity(intent)
        }
    }

    override fun onVolumeChanged(volume: Int) {
        if (audioAble) {
            player?.setVolume(volume.toFloat(), volume.toFloat())
        }
    }

    private fun getDeviceStatus(id: String?): Int {
        var command: ByteArray? = null
        when (tv_video_quality.text.toString()) {
            getString(R.string.video_quality_high_str) -> {
                command = "action=inner_define&channel=0&cmd=get_device_st&type=live&quality=super".toByteArray()
            }
            getString(R.string.video_quality_medium_str) -> {
                command = "action=inner_define&channel=0&cmd=get_device_st&type=live&quality=high".toByteArray()
            }
            getString(R.string.video_quality_low_str) -> {
                command = "action=inner_define&channel=0&cmd=get_device_st&type=live&quality=standard".toByteArray()
            }
        }
        val reponse = XP2P.postCommandRequestSync(id, command, command!!.size.toLong(), 2 * 1000 * 1000)
        if (!TextUtils.isEmpty(reponse)) {
            val deviceStatuses: List<DeviceStatus> = JSONArray.parseArray(reponse, DeviceStatus::class.java)
            // 0   接收请求
            // 1   拒绝请求
            // 404 error request message
            // 405 connect number too many
            // 406 current command don't support
            // 407 device process error
            if (deviceStatuses.isNotEmpty()) {
                runOnUiThread {
                    when (deviceStatuses[0].status) {
                        0 -> Toast.makeText(this, "设备状态正常", Toast.LENGTH_SHORT).show()
                        1 -> Toast.makeText(this, "设备状态异常, 拒绝请求: $reponse", Toast.LENGTH_SHORT).show()
                        404 -> Toast.makeText(
                            this,
                            "设备状态异常, error request message: $reponse",
                            Toast.LENGTH_SHORT
                        ).show()
                        405 -> Toast.makeText(
                            this,
                            "设备状态异常, connect number too many: $reponse",
                            Toast.LENGTH_SHORT
                        ).show()
                        406 -> Toast.makeText(
                            this,
                            "设备状态异常, current command don't support: $reponse",
                            Toast.LENGTH_SHORT
                        ).show()
                        407 -> Toast.makeText(
                            this,
                            "设备状态异常, device process error: $reponse",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
                return deviceStatuses[0].status
            } else {
                runOnUiThread {
                    Toast.makeText(this, "获取设备状态失败", Toast.LENGTH_SHORT).show()
                }
                return -1
            }
        }
        return -1
    }

    private val mHandler = MyHandler(this)

    private class MyHandler(activity: VideoPreviewActivity) : Handler() {
        private val mActivity: WeakReference<VideoPreviewActivity>  = WeakReference(activity)
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

        tv_a_cache?.text = String.format(Locale.US, "%s, %s",
            CommonUtils.formatedDurationMilli(audioCachedDuration),
            CommonUtils.formatedSize(audioCachedBytes))
        tv_v_cache?.text = String.format(Locale.US, "%s, %s",
            CommonUtils.formatedDurationMilli(videoCachedDuration),
            CommonUtils.formatedSize(videoCachedBytes))
        tv_tcp_speed?.text = String.format(Locale.US, "%s",
            CommonUtils.formatedSpeed(tcpSpeed, 1000))
        tv_video_w_h?.text = "${player.videoWidth} x ${player.videoHeight}"
    }
}