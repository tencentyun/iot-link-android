package com.tencent.iot.explorer.link.demo.video.preview

import android.Manifest
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.SurfaceTexture
import android.media.AudioManager
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.text.TextUtils
import android.util.Log
import android.view.Surface
import android.view.TextureView
import android.view.View
import android.widget.Toast
import com.alibaba.fastjson.JSON
import com.tencent.iot.explorer.link.demo.App
import com.tencent.iot.explorer.link.demo.R
import com.tencent.iot.explorer.link.demo.VideoBaseActivity
import com.tencent.iot.explorer.link.demo.common.util.CommonUtils
import com.tencent.iot.explorer.link.demo.video.Command
import com.tencent.iot.explorer.link.demo.video.DevInfo
import com.tencent.iot.explorer.link.demo.video.utils.ListOptionsDialog
import com.tencent.iot.explorer.link.demo.video.utils.TipToastDialog
import com.tencent.iot.video.link.consts.VideoConst
import com.tencent.iot.video.link.util.audio.AudioRecordUtil
import com.tencent.xnet.XP2P
import com.tencent.xnet.XP2PCallback
import kotlinx.android.synthetic.main.activity_video_preview.*
import kotlinx.android.synthetic.main.dash_board_layout.*
import kotlinx.android.synthetic.main.fragment_video_cloud_playback.*
import kotlinx.android.synthetic.main.title_layout.*
import kotlinx.coroutines.*
import tv.danmaku.ijk.media.player.IjkMediaPlayer
import java.lang.Runnable
import java.util.*


class WlanVideoPreviewActivity : VideoBaseActivity(), TextureView.SurfaceTextureListener,
    XP2PCallback, CoroutineScope by MainScope() {

    var tag = WlanVideoPreviewActivity::class.simpleName
    lateinit var player: IjkMediaPlayer
    lateinit var surface: Surface

    @Volatile
    var audioAble = true
    lateinit var audioRecordUtil: AudioRecordUtil
    var permissions = arrayOf(Manifest.permission.RECORD_AUDIO)

    @Volatile
    var showTip = false

    @Volatile
    var connectTime = 0L

    @Volatile
    var startShowVideoTime = 0L

    @Volatile
    var showVideoTime = 0L

    val MSG_UPDATE_HUD = 1
    var productId = ""
    var deviceName = ""
    var port = 0
    var address = ""
    var channel = 0

    override fun getContentView(): Int {
        return R.layout.activity_video_preview
    }

    override fun onResume() {
        super.onResume()
        XP2P.setCallback(this)
        startPlayer()
    }

    override fun initView() {
        var bundle = intent.getBundleExtra(VideoConst.VIDEO_CONFIG)
        bundle?.let {
            var videoConfig = bundle.getString(VideoConst.VIDEO_CONFIG)
            if (TextUtils.isEmpty(videoConfig)) return@let
            var devInfo = JSON.parseObject(videoConfig, DevUrl2Preview::class.java)
            devInfo?.let {
                tv_title.setText(it.devName)
                deviceName = it.devName
                channel = it.channel
                address = it.address
                port = it.port
            }
        }

        tv_video_quality.setText(R.string.video_quality_medium_str)
        App.data.accessInfo?.let {
            productId = it.productId
            audioRecordUtil = AudioRecordUtil(this, "${it.productId}/${deviceName}", 16000)
        }
    }

    fun startPlayer() {
        player = IjkMediaPlayer()
        Log.e(
            tag,
            "start startPlayer productid ${App.data.accessInfo?.productId} devname ${deviceName}"
        )
        if (App.data.accessInfo == null || TextUtils.isEmpty(deviceName)) return
        mHandler.sendEmptyMessageDelayed(MSG_UPDATE_HUD, 500)

        Thread(Runnable {
            val id = "${productId}/${deviceName}"
            val started = XP2P.startLanService(id, productId, deviceName, address, port.toString())
            Log.e(tag, "===================== after startLanService =====================")
            if (started != 0) {
                launch(Dispatchers.Main) {
                    var errInfo = ""
                    if (started.toString() == "-1007") {
                        errInfo = getString(R.string.xp2p_err_version)
                    } else {
                        errInfo = getString(R.string.error_with_code, id, started.toString())
                    }
                    Toast.makeText(this@WlanVideoPreviewActivity, errInfo, Toast.LENGTH_SHORT)
                        .show()
                }
                return@Runnable
            }
        }).start()
    }

    override fun onSurfaceTextureAvailable(surface: SurfaceTexture?, width: Int, height: Int) {
        surface?.let {
            this.surface = Surface(surface)
            player.setSurface(this.surface)
        }
    }

    fun speakAble(able: Boolean): Boolean {
        val id = "${productId}/${deviceName}"
        if (!TextUtils.isEmpty(id)) {
            if (able) {
                val port = XP2P.getLanProxyPort(id)
                val command =
                    XP2P.getLanUrl(id) + "voice?_protocol=tcp&_port=$port&channel=${channel}"
                Log.e(tag, "start radio url $command")
                Log.e(tag, "speakAble id $id")
                XP2P.runSendService(id, command, true)
                audioRecordUtil.start()
                return true
            } else {
                Log.e(tag, "stop radio")
                audioRecordUtil.stop()
                XP2P.stopSendService(id, null)
                return true
            }
        }
        return false
    }

    override fun setListener() {
        iv_back.setOnClickListener { finish() }
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

    fun chgAudioStatus(audioAble: Boolean) {
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

    var controlListener = object : View.OnClickListener {
        override fun onClick(v: View?) {
            val id = "${productId}/${deviceName}"
            var command = XP2P.getLanUrl(id) + "command?_protocol=tcp&"
            when (v) {
                iv_up -> command += Command.getPtzUpCommand(channel)
                iv_down -> command += Command.getPtzDownCommand(channel)
                iv_right -> command += Command.getPtzRightCommand(channel)
                iv_left -> command += Command.getPtzLeftCommand(channel)
            }
            Log.e(tag, "command $command")

            Thread(Runnable {
                App.data.accessInfo?.let {
                    if (command.length <= 0) return@Runnable
                    var retContent = XP2P.postCommandRequestSync(
                        "${productId}/${deviceName}",
                        command.toByteArray(), command.toByteArray().size.toLong(), 2 * 1000 * 1000
                    ) ?: ""
                    Log.d(tag, "command result -> $retContent")
                    launch(Dispatchers.Main) {
                        if (TextUtils.isEmpty(retContent)) {
                            retContent = getString(R.string.command_with_error, command)
                        }
                        Toast.makeText(
                            this@WlanVideoPreviewActivity,
                            retContent,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }).start()
        }
    }

    fun chgTextState(value: Int) {
        val id = "${productId}/${deviceName}"
        val port = XP2P.getLanProxyPort(id)
        var command = ""
        when (value) {
            0 -> {
                tv_video_quality.setText(R.string.video_quality_high_str)
                command =
                    "ipc.flv?action=live&_protocol=tcp&quality=super&_crypto=off&_port=$port&channel=${channel}"
            }
            1 -> {
                tv_video_quality.setText(R.string.video_quality_medium_str)
                command =
                    "ipc.flv?action=live&_protocol=tcp&quality=high&_crypto=off&_port=$port&channel=${channel}"
            }
            2 -> {
                tv_video_quality.setText(R.string.video_quality_low_str)
                command =
                    "ipc.flv?action=live&_protocol=tcp&quality=standard&_crypto=off&_port=$port&channel=${channel}"
            }
        }

        setPlayerUrl(command)
        chgAudioStatus(audioAble)
    }

    private fun setPlayerUrl(suffix: String) {
        player.release()
        launch(Dispatchers.Main) {
            layout_video_preview?.removeView(v_preview)
            layout_video_preview?.addView(v_preview, 0)
        }

        player = IjkMediaPlayer()
        player.let {
            val id = "${productId}/${deviceName}"
            val url = XP2P.getLanUrl(id) + suffix
            it.reset()

            it.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "analyzemaxduration", 100)
            it.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "probesize", 25 * 1024)
            it.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "packet-buffering", 0)
            it.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "start-on-prepared", 1)
            it.setOption(IjkMediaPlayer.OPT_CATEGORY_CODEC, "threads", 1)
            it.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "sync-av-start", 0)
            it.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec", 1)
            it.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec-auto-rotate", 1)
            it.setOption(
                IjkMediaPlayer.OPT_CATEGORY_PLAYER,
                "mediacodec-handle-resolution-change",
                1
            )

            it.setSurface(this.surface)
            Log.e(tag, "switch url $url")
            it.dataSource = url

            it.prepareAsync()
            it.start()
        }
    }

    override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture?, width: Int, height: Int) {}
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
    }

    override fun fail(msg: String?, errorCode: Int) {}
    override fun commandRequest(id: String?, msg: String?) {}
    override fun avDataRecvHandle(id: String?, data: ByteArray?, len: Int) {}
    override fun avDataCloseHandle(id: String?, msg: String?, errorCode: Int) {}
    override fun onDeviceMsgArrived(id: String?, data: ByteArray?, len: Int): String {
        return "app reply to device"
    }

    override fun xp2pEventNotify(id: String?, msg: String?, event: Int) {
        if (event == 1004) {
            player?.let {
                var url = XP2P.getLanUrl(id)
                val port = XP2P.getLanProxyPort(id)
                url += "ipc.flv?action=live&_protocol=tcp&quality=high&_crypto=off&_port=$port&channel=${channel}"
                Log.e(tag, "proxy ===================== $url")
                it.reset()
                it.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "analyzemaxduration", 100)
                it.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "probesize", 25 * 1024)
                it.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "packet-buffering", 0)
                it.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "start-on-prepared", 1)
                it.setOption(IjkMediaPlayer.OPT_CATEGORY_CODEC, "threads", 1)
                it.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "sync-av-start", 0)
                it.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec", 1)
                it.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec-auto-rotate", 1)
                it.setOption(
                    IjkMediaPlayer.OPT_CATEGORY_PLAYER,
                    "mediacodec-handle-resolution-change",
                    1
                )
                it.dataSource = url
                it.prepareAsync()
                it.start()
            }
        }
    }

    override fun onPause() {
        super.onPause()
        finishPlayer()
    }

    private fun finishPlayer() {
        player?.release()
        if (radio_talk.isChecked) speakAble(false)
        App.data.accessInfo?.let {
            XP2P.stopService("${productId}/${deviceName}")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        finishPlayer()
        XP2P.setCallback(null)
        cancel()
    }

    companion object {
        fun startPreviewActivity(context: Context?, dev: DevInfo) {
            context ?: let { return }

            var intent = Intent(context, WlanVideoPreviewActivity::class.java)
            var bundle = Bundle()
            intent.putExtra(VideoConst.VIDEO_CONFIG, bundle)
            var devInfo = DevUrl2Preview()
            devInfo.devName = dev.deviceName
            devInfo.Status = dev.Status
            devInfo.channel = dev.channel
            devInfo.address = dev.address
            devInfo.port = dev.port
            bundle.putString(VideoConst.VIDEO_CONFIG, JSON.toJSONString(devInfo))
            context.startActivity(intent)
        }
    }

    private var switchVideoQualityListener = object : View.OnClickListener {
        override fun onClick(v: View?) {
            showVVideoQualityDialog()
        }
    }

    private fun showVVideoQualityDialog() {
        var options = arrayListOf(
            getString(R.string.video_quality_high_str) + " " + getString(R.string.video_quality_high),
            getString(R.string.video_quality_medium_str) + " " + getString(R.string.video_quality_medium),
            getString(R.string.video_quality_low_str) + " " + getString(R.string.video_quality_low)
        )
        var dlg = ListOptionsDialog(this@WlanVideoPreviewActivity, options)
        dlg.show()
        dlg.setOnDismisListener { chgTextState(it) }
    }

    private val mHandler: Handler = object : Handler() {
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                MSG_UPDATE_HUD -> {
                    val videoCachedDuration = player?.videoCachedDuration
                    val audioCachedDuration = player?.audioCachedDuration
                    val videoCachedBytes = player?.videoCachedBytes
                    val audioCachedBytes = player?.audioCachedBytes
                    val tcpSpeed = player?.tcpSpeed

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
                    removeMessages(MSG_UPDATE_HUD)
                    sendEmptyMessageDelayed(MSG_UPDATE_HUD, 500)
                }
            }
        }
    }
}