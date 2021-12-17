package com.tencent.iot.explorer.link.demo.video.preview

import android.content.Context
import android.content.Intent
import android.graphics.SurfaceTexture
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.text.TextUtils
import android.util.Log
import android.view.Surface
import android.view.View
import android.widget.Toast
import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.JSONArray
import com.tencent.iot.explorer.link.demo.App
import com.tencent.iot.explorer.link.demo.R
import com.tencent.iot.explorer.link.demo.common.util.CommonUtils
import com.tencent.iot.explorer.link.demo.video.Command
import com.tencent.iot.explorer.link.demo.video.DevInfo
import com.tencent.iot.explorer.link.demo.video.playback.cloudPlayback.event.EventPresenter
import com.tencent.iot.explorer.link.demo.video.utils.VolumeChangeObserver
import com.tencent.iot.video.link.consts.VideoConst
import com.tencent.iot.video.link.util.audio.AudioRecordUtil
import com.tencent.xnet.XP2P
import kotlinx.android.synthetic.main.activity_video_preview.*
import kotlinx.android.synthetic.main.dash_board_layout.*
import kotlinx.android.synthetic.main.fragment_video_cloud_playback.*
import kotlinx.android.synthetic.main.title_layout.*
import kotlinx.coroutines.*
import tv.danmaku.ijk.media.player.IjkMediaPlayer
import java.nio.charset.StandardCharsets
import java.util.*

class WlanVideoPreviewActivity : VideoPreviewActivity() {
    override var tag = WlanVideoPreviewActivity::class.simpleName
    var port = 0
    var address = ""
    private var waitLock = Object()

    override fun getContentView(): Int {
        return R.layout.activity_video_preview
    }

    override fun initView() {
        player = IjkMediaPlayer()
        presenter = EventPresenter(this@WlanVideoPreviewActivity)
        var bundle = intent.getBundleExtra(VideoConst.VIDEO_CONFIG)
        bundle?.let {
            var videoConfig = bundle.getString(VideoConst.VIDEO_CONFIG)
            if (TextUtils.isEmpty(videoConfig)) return@let

            var devInfo = JSON.parseObject(videoConfig, DevUrl2Preview::class.java)
            devInfo?.let {
                tv_title.setText(it.devName)
                presenter.setDeviceName(it.devName)
                presenter.setChannel(it.channel)
                address = it.address
                port = it.port
            }
        }

        tv_video_quality.setText(R.string.video_quality_medium_str)
        App.data.accessInfo?.let {
            audioRecordUtil = AudioRecordUtil(this, "${it.productId}/${presenter.getDeviceName()}", 16000)
        }

        //实例化对象并设置监听器
        volumeChangeObserver = VolumeChangeObserver(this)
        volumeChangeObserver?.setVolumeChangeListener(this)
        volumeChangeObserver?.registerReceiver();
    }

    override fun startPlayer() {
        Log.e(tag, "start startPlayer productid ${App.data.accessInfo?.productId} devname ${presenter.getDeviceName()}")
        if (App.data.accessInfo == null || TextUtils.isEmpty(presenter.getDeviceName())) return
        mHandler.sendEmptyMessageDelayed(MSG_UPDATE_HUD, 500)

        Thread(Runnable {
            Log.e(tag, "===================== after startService 0 =====================")
            var id = "${App.data.accessInfo?.productId}/${presenter.getDeviceName()}"
            var startTime = System.currentTimeMillis();
            var started = XP2P.startLanService(id, App.data.accessInfo?.productId, presenter.getDeviceName(), address, port.toString())
            var timeCost = System.currentTimeMillis() - startTime;
            Log.e(tag, "===================== after startService 1  timeCost=${timeCost}=====================")
            if (started != 0) {
                launch(Dispatchers.Main) {
                    var errInfo = ""
                    if (started.toString() == "-1007") {
                        errInfo = getString(R.string.xp2p_err_version)
                    } else {
                        errInfo = getString(R.string.error_with_code, id, started.toString())
                    }
                    Toast.makeText(this@WlanVideoPreviewActivity, errInfo, Toast.LENGTH_SHORT).show()
                }
                return@Runnable
            }
            synchronized(waitLock) {
                waitLock.wait(1000)
            }

            Log.e(tag, "===================== after startService =====================")
            player?.let {
                var url = XP2P.getLanUrl(id)
                val port = XP2P.getLanProxyPort(id)

                url += "ipc.flv?action=live&_protocol=tcp&quality=high&_crypto=off&_port=$port&channel=${presenter.getChannel()}"
                Log.e(tag, "proxy ===================== $url")
                it.reset()

                it.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "analyzemaxduration", 100)
                it.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "probesize", 25 * 1024)
                it.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "packet-buffering", 0)
                it.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "start-on-prepared", 1)
                it.setOption(IjkMediaPlayer.OPT_CATEGORY_CODEC, "threads", 1)
                it.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "sync-av-start", 0)
                it.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec",1)
                it.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec-auto-rotate", 1)
                it.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec-handle-resolution-change", 1)

                it.setSurface(this.surface)
                it.dataSource = url

                it.prepareAsync()
                it.start()
            }
        }).start()
    }

    override fun onSurfaceTextureAvailable(surface: SurfaceTexture?, width: Int, height: Int) {
        surface?.let {
            this.surface = Surface(surface)
            player.setSurface(this.surface)
        }
    }

    override fun chgTextState(value: Int) {
        var id = "${App.data.accessInfo?.productId}/${presenter.getDeviceName()}"
        val port = XP2P.getLanProxyPort(id)
        var command = ""
        when(value) {
            0 -> {
                tv_video_quality.setText(R.string.video_quality_high_str)
                command = "ipc.flv?action=live&_protocol=tcp&quality=super&_crypto=off&_port=$port&channel=${presenter.getChannel()}"
            }
            1 -> {
                tv_video_quality.setText(R.string.video_quality_medium_str)
                command = "ipc.flv?action=live&_protocol=tcp&quality=high&_crypto=off&_port=$port&channel=${presenter.getChannel()}"
            }
            2 -> {
                tv_video_quality.setText(R.string.video_quality_low_str)
                command = "ipc.flv?action=live&_protocol=tcp&quality=standard&_crypto=off&_port=$port&channel=${presenter.getChannel()}"
            }
        }

        setPlayerUrl(command)
        chgAudioStatus(audioAble)
    }

    override fun setPlayerUrl(suffix: String) {
        player.release()
        launch (Dispatchers.Main) {
            layout_video_preview?.removeView(v_preview)
            layout_video_preview?.addView(v_preview, 0)
        }

        player = IjkMediaPlayer()
        player?.let {
            var id = "${App.data.accessInfo?.productId}/${presenter.getDeviceName()}"
            val url = XP2P.getLanUrl(id) + suffix
            it.reset()

            it.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "analyzemaxduration", 100)
            it.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "probesize", 25 * 1024)
            it.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "packet-buffering", 0)
            it.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "start-on-prepared", 1)
            it.setOption(IjkMediaPlayer.OPT_CATEGORY_CODEC, "threads", 1)
            it.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "sync-av-start", 0)
            it.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec",1)
            it.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec-auto-rotate", 1)
            it.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec-handle-resolution-change", 1)

            it.setSurface(this.surface)
            Log.e(tag, "switch url $url")
            it.dataSource = url

            it.prepareAsync()
            it.start()
        }
    }

    override var controlListener = object : View.OnClickListener {
        override fun onClick(v: View?) {
            var id = "${App.data.accessInfo?.productId}/${presenter.getDeviceName()}"
            var command = XP2P.getLanUrl(id) + "command?_protocol=tcp&"
            when(v) {
                iv_up -> command += Command.getPtzUpCommand(presenter.getChannel())
                iv_down -> command += Command.getPtzDownCommand(presenter.getChannel())
                iv_right -> command += Command.getPtzRightCommand(presenter.getChannel())
                iv_left -> command += Command.getPtzLeftCommand(presenter.getChannel())
            }
            Log.e(tag, "command $command" )

            Thread(Runnable {
                App.data.accessInfo?.let {
                    if (command.length <= 0) return@Runnable
                    var retContent = XP2P.postCommandRequestSync("${it.productId}/${presenter.getDeviceName()}",
                        command.toByteArray(), command.toByteArray().size.toLong(), 2 * 1000 * 1000)?:""
                    Log.d(tag, "command result -> $retContent")
                    launch(Dispatchers.Main) {
                        if (TextUtils.isEmpty(retContent)) {
                            retContent = getString(R.string.command_with_error, command)
                        }
                        Toast.makeText(this@WlanVideoPreviewActivity, retContent, Toast.LENGTH_SHORT).show()
                    }
                }
            }).start()
        }
    }

    companion object {
        fun startPreviewActivity(context: Context?, dev: DevInfo) {
            context?:let { return }

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

    override fun speakAble(able: Boolean): Boolean {
        App.data.accessInfo?.let { accessInfo ->
            var id = "${accessInfo.productId}/${presenter.getDeviceName()}"
            if (able) {
                val port = XP2P.getLanProxyPort(id)
                var command = XP2P.getLanUrl(id) + "voice?_protocol=tcp&_port=$port&channel=${presenter.getChannel()}"
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

    override fun onDeviceMsgArrived(id: String?, data: ByteArray?, len: Int): String {
        data?.let {
            Log.d(tag, "onDeviceMsgArrived ${String(it, StandardCharsets.UTF_8)}")
        }
        return "data recved"
    }

    override fun setListener() {
        super.setListener()
        radio_playback.setOnClickListener(null)
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

                    tv_a_cache?.text = String.format(Locale.US, "%s, %s",
                        CommonUtils.formatedDurationMilli(audioCachedDuration),
                        CommonUtils.formatedSize(audioCachedBytes))
                    tv_v_cache?.text = String.format(Locale.US, "%s, %s",
                        CommonUtils.formatedDurationMilli(videoCachedDuration),
                        CommonUtils.formatedSize(videoCachedBytes))
                    tv_tcp_speed?.text = String.format(Locale.US, "%s",
                        CommonUtils.formatedSpeed(tcpSpeed, 1000))
                    removeMessages(MSG_UPDATE_HUD)
                    sendEmptyMessageDelayed(MSG_UPDATE_HUD, 500)
                }
            }
        }
    }
}