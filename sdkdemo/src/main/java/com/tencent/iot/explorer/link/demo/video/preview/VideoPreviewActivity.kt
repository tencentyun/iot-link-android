package com.tencent.iot.explorer.link.demo.video.preview

import android.Manifest
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.graphics.SurfaceTexture
import android.media.AudioManager
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.Surface
import android.view.TextureView
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.LinearLayoutManager
import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.JSONArray
import com.tencent.iot.explorer.link.demo.App
import com.tencent.iot.explorer.link.demo.R
import com.tencent.iot.explorer.link.demo.BaseActivity
import com.tencent.iot.explorer.link.demo.common.util.ImageSelect
import com.tencent.iot.explorer.link.demo.video.Command
import com.tencent.iot.explorer.link.demo.video.playback.cloudPlayback.event.ActionListAdapter
import com.tencent.iot.explorer.link.demo.video.utils.ListOptionsDialog
import com.tencent.iot.explorer.link.demo.video.utils.ToastDialog
import com.tencent.iot.explorer.link.demo.video.playback.cloudPlayback.event.ActionRecord
import com.tencent.iot.explorer.link.demo.video.DevInfo
import com.tencent.iot.explorer.link.demo.video.playback.cloudPlayback.event.EventPresenter
import com.tencent.iot.explorer.link.demo.video.playback.cloudPlayback.event.EventView
import com.tencent.iot.explorer.link.demo.common.util.CommonUtils
import com.tencent.iot.explorer.link.demo.video.playback.VideoPlaybackActivity
import com.tencent.iot.video.link.consts.VideoConst
import com.tencent.iot.video.link.util.audio.AudioRecordUtil
import com.tencent.xnet.XP2P
import com.tencent.xnet.XP2PCallback
import kotlinx.android.synthetic.main.activity_video_preview.*
import kotlinx.android.synthetic.main.fragment_video_cloud_playback.*
import kotlinx.android.synthetic.main.title_layout.*
import kotlinx.coroutines.*
import tv.danmaku.ijk.media.player.IjkMediaPlayer
import java.lang.Runnable
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CountDownLatch

private var countDownLatchs : MutableMap<String, CountDownLatch> = ConcurrentHashMap()
private var keepPlayThreadLock = Object()
@Volatile
private var keepAliveThreadRuning = true

class VideoPreviewActivity : BaseActivity(), EventView, TextureView.SurfaceTextureListener,
    XP2PCallback, CoroutineScope by MainScope() {

    private var tag = VideoPreviewActivity::class.simpleName
    private var orientationV = true
    private var adapter : ActionListAdapter? = null
    private var records : MutableList<ActionRecord> = ArrayList()
    private lateinit var presenter: EventPresenter
    private lateinit var player : IjkMediaPlayer
    private lateinit var surface: Surface
    @Volatile
    private var audioAble = true
    @Volatile
    private var urlPrefix = ""
    private var filePath: String? = null
    private lateinit var audioRecordUtil: AudioRecordUtil
    private var permissions = arrayOf(Manifest.permission.RECORD_AUDIO)

    override fun getContentView(): Int {
        return R.layout.activity_video_preview
    }

    override fun initView() {
        keepAliveThreadRuning = true
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
            presenter.getCurrentDayEventsData()
            XP2P.setQcloudApiCred(it.accessId, it.accessToken)
            XP2P.setCallback(this)
            audioRecordUtil = AudioRecordUtil(this, "${it.productId}/${presenter.getDeviceName()}")
        }

        startPlayer()
    }

    private fun startPlayer() {
        if (App.data.accessInfo == null || TextUtils.isEmpty(presenter.getDeviceName())) return
        player = IjkMediaPlayer()

        Thread(Runnable {
            var started = XP2P.startServiceWithXp2pInfo("${App.data.accessInfo!!.productId}/${presenter.getDeviceName()}",
                App.data.accessInfo!!.productId, presenter.getDeviceName(), "")
            if (started != 0) return@Runnable

            var tmpCountDownLatch = CountDownLatch(1)
            countDownLatchs.put("${App.data.accessInfo!!.productId}/${presenter.getDeviceName()}", tmpCountDownLatch)
            tmpCountDownLatch.await()

            urlPrefix = XP2P.delegateHttpFlv("${App.data.accessInfo!!.productId}/${presenter.getDeviceName()}")
            if (!TextUtils.isEmpty(urlPrefix)) {
                player?.let {
                    val url = urlPrefix + Command.getVideoHightQualityUrlSuffix(presenter.getChannel())
                    playPlayer(it, url)
                    keepPlayerplay("${App.data.accessInfo!!.productId}/${presenter.getDeviceName()}")
                }
            }
        }).start()
    }

    private fun playPlayer(player: IjkMediaPlayer, url: String) {
        player.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "analyzemaxduration", 100)
        player.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "probesize", 25 * 1024)
        player.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "packet-buffering", 0)
        player.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "start-on-prepared", 1)
        player.setOption(IjkMediaPlayer.OPT_CATEGORY_CODEC, "threads", 1)
        player.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "sync-av-start", 0)
        player.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "reconnect", 1)
//        player.dataSource = url
//        player.prepareAsync()
//        player.start()

//        player.reset()
//        player.setSurface(this.surface)
//        player.dataSource = url
//        player.prepareAsync()
//        player.start()
        resetPlayer()
    }

    private fun keepPlayerplay(id: String?) {
        if (TextUtils.isEmpty(id)) return

        // 开启守护线程
        Thread(Runnable {
            var objectLock = Object()
            while (true) {
                var tmpCountDownLatch = CountDownLatch(1)
                countDownLatchs.put(id!!, tmpCountDownLatch)

                Log.d(tag, "id=${id} keepAliveThread wait disconnected msg")
                synchronized(keepPlayThreadLock) {
                    keepPlayThreadLock.wait()
                }
                Log.d(tag, "id=${id} keepAliveThread do not wait and keepAliveThreadRuning=${keepAliveThreadRuning}")
                if (!keepAliveThreadRuning) break //锁被释放后，检查守护线程是否继续运行

                // 发现断开尝试恢复视频，每隔一秒尝试一次
                XP2P.stopService(id)
                while (XP2P.startServiceWithXp2pInfo(id, App.data.accessInfo!!.productId, presenter.getDeviceName(), "") != 0) {
                    XP2P.stopService(id)
                    synchronized(objectLock) {
                        objectLock.wait(1000)
                    }
                    Log.d(tag, "id=${id}, try to call startServiceWithXp2pInfo")
                }

                Log.d(tag, "id=${id}, call startServiceWithXp2pInfo successed")
                countDownLatchs.put(id!!, tmpCountDownLatch)
                Log.d(tag, "id=${id}, tmpCountDownLatch start wait")
                tmpCountDownLatch.await()
                Log.d(tag, "id=${id}, tmpCountDownLatch do not wait any more")

                urlPrefix = XP2P.delegateHttpFlv(id)
                if (!TextUtils.isEmpty(urlPrefix)) resetPlayer()
            }
        }).start()
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

    private fun speakAble(able: Boolean): Boolean {
        App.data.accessInfo?.let { accessInfo ->
            if (able) {
                var command = Command.getNvrIpcStatus(presenter.getChannel(), 0)
                var repStatus = XP2P.postCommandRequestSync("${accessInfo.productId}/${presenter.getDeviceName()}",
                    command.toByteArray(), command.toByteArray().size.toLong(), 2 * 1000 * 1000)
                JSONArray.parseArray(repStatus, DevStatus::class.java)?.let {
                    if (it.size == 1 && it.get(0).status == 0) {
                        XP2P.runSendService("${accessInfo.productId}/${presenter.getDeviceName()}", Command.getTwoWayRadio(presenter.getChannel()), true)
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
                player.startRecord(filePath)
            } else {
                player.stopRecord()
                CommonUtils.refreshVideoList(this@VideoPreviewActivity, filePath)
            }
        }
        radio_playback.setOnClickListener {
            var dev = DevInfo()
            dev.deviceName = presenter.getDeviceName()
            VideoPlaybackActivity.startPlaybackActivity(this@VideoPreviewActivity, dev)
            finish()
        }
        radio_photo.setOnClickListener {
            ImageSelect.saveBitmap(this@VideoPreviewActivity, v_preview.bitmap)
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

    private fun chgAudioStatus(audioAble: Boolean) {
        if (!audioAble) {
            iv_audio.setImageResource(R.mipmap.no_audio)
            player.setVolume(0F, 0F)
        } else {
            iv_audio.setImageResource(R.mipmap.audio)
            var audioManager = getSystemService(Service.AUDIO_SERVICE) as AudioManager
            var volume = audioManager.getStreamVolume(AudioManager.STREAM_SYSTEM)
            player.setVolume(volume.toFloat(), volume.toFloat())
        }
    }

    private var controlListener = object : View.OnClickListener {
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
                    XP2P.postCommandRequestSync("${it.productId}/${presenter.getDeviceName()}",
                        command.toByteArray(), command.toByteArray().size.toLong(), 2 * 1000 * 1000)
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
        var dlg =
            ListOptionsDialog(
                this@VideoPreviewActivity,
                options
            )
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
        var dlg =
            VideoQualityDialog(
                this@VideoPreviewActivity,
                pos
            )
        dlg.show()
        btn_layout.visibility = View.GONE
        dlg.setOnDismisListener(object : VideoQualityDialog.OnDismisListener {
            override fun onItemClicked(pos: Int) { chgTextState(pos) }
            override fun onDismiss() { btn_layout.visibility = View.VISIBLE }
        })
    }

    private fun chgTextState(value: Int) {
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

    private fun setPlayerUrl(suffix: String) {
        player?.let {
            val url = urlPrefix + suffix
            it.reset()
            it.setSurface(this.surface)
            it.dataSource = url
            it.prepareAsync()
            it.start()
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
        launch(Dispatchers.Main) {
            records.addAll(events)
            adapter?.notifyDataSetChanged()
        }
    }

    override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture?, width: Int, height: Int) {}
    override fun onSurfaceTextureDestroyed(surface: SurfaceTexture?): Boolean { return false }
    override fun onSurfaceTextureUpdated(surface: SurfaceTexture?) {}
    override fun fail(msg: String?, errorCode: Int) {}
    override fun commandRequest(id: String?, msg: String?) {}
    override fun avDataRecvHandle(id: String?, data: ByteArray?, len: Int) {}
    override fun avDataCloseHandle(id: String?, msg: String?, errorCode: Int) {}

    override fun xp2pEventNotify(id: String?, msg: String?, event: Int) {
        Log.e(tag, "id=${id}, event=${event}")
        if (event == 1003) {
            keepPlayThreadLock?.let {
                synchronized(it) {
                    it.notify()
                }
            } // 唤醒守护线程
        } else if (event == 1004 || event == 1005) {
            countDownLatchs.get(id)?.let {
                Log.d(tag, "id=${id}, countDownLatch=${it}, countDownLatch.count=${it.count}")
                it.countDown()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        player?.release()
        if (radio_talk.isChecked) speakAble(false)
        if (radio_record.isChecked) {
            player.stopRecord()
            CommonUtils.refreshVideoList(this@VideoPreviewActivity, filePath)
        }

        App.data.accessInfo?.let {
            XP2P.stopService("${it.productId}/${presenter.getDeviceName()}")
        }
        XP2P.setCallback(null)

        countDownLatchs.clear()
        // 关闭守护线程
        keepAliveThreadRuning = false
        keepPlayThreadLock?.let {
            synchronized(it) {
                it.notify()
            }
        }
        cancel()
    }

    companion object {
        fun startPreviewActivity(context: Context?, dev: DevInfo) {
            context?:let { return }

            var intent = Intent(context, VideoPreviewActivity::class.java)
            var bundle = Bundle()
            intent.putExtra(VideoConst.VIDEO_CONFIG, bundle)
            var devInfo = DevUrl2Preview()
            devInfo.devName = dev.deviceName
            devInfo.Status = dev.Status
            devInfo.channel = dev.channel
            bundle.putString(VideoConst.VIDEO_CONFIG, JSON.toJSONString(devInfo))
            context.startActivity(intent)
        }
    }
}