package com.tencent.iot.explorer.link.demo.video.preview

import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.graphics.SurfaceTexture
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.Surface
import android.view.TextureView
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.JSONArray
import com.tencent.iot.explorer.link.demo.App
import com.tencent.iot.explorer.link.demo.BuildConfig
import com.tencent.iot.explorer.link.demo.R
import com.tencent.iot.explorer.link.demo.databinding.ActivityVideoMultiPreviewBinding
import com.tencent.iot.explorer.link.demo.video.Command
import com.tencent.iot.explorer.link.demo.video.DevInfo
import com.tencent.iot.explorer.link.demo.video.VideoPreviewBaseActivity
import com.tencent.iot.explorer.link.demo.video.playback.cloudPlayback.event.ActionRecord
import com.tencent.iot.video.link.consts.VideoConst
import com.tencent.xnet.XP2P
import com.tencent.xnet.XP2PAppConfig
import com.tencent.xnet.XP2PCallback
import com.tencent.xnet.annotations.XP2PProtocolType
import kotlinx.coroutines.*
import tv.danmaku.ijk.media.player.IjkMediaPlayer
import java.util.*
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.CountDownLatch
import kotlin.concurrent.thread


private var allDevUrl: MutableList<DevUrl2Preview> = CopyOnWriteArrayList()

class VideoMultiPreviewActivity : VideoPreviewBaseActivity<ActivityVideoMultiPreviewBinding>(),
    XP2PCallback, CoroutineScope by MainScope() {
    lateinit var gridLayoutManager: GridLayoutManager
    lateinit var linearLayoutManager: LinearLayoutManager
    private var adapter: DevPreviewAdapter? = null
    private var tag = VideoMultiPreviewActivity::class.simpleName
    private var orientation = true

    @Volatile
    var connectStartTime = 0L

    @Volatile
    var connectTime = 0L

    @Volatile
    var startShowVideoTime = 0L
    private var isRestart: Boolean = false
    private val xP2PAppConfig = XP2PAppConfig().also { appConfig ->
        appConfig.appKey =
            BuildConfig.TencentIotLinkSDKDemoAppkey //为explorer平台注册的应用信息(https://console.cloud.tencent.com/iotexplorer/v2/instance/app/detai) explorer控制台- 应用开发 - 选对应的应用下的 appkey/appsecret
        appConfig.appSecret =
            BuildConfig.TencentIotLinkSDKDemoAppSecret //为explorer平台注册的应用信息(https://console.cloud.tencent.com/iotexplorer/v2/instance/app/detai) explorer控制台- 应用开发 - 选对应的应用下的 appkey/appsecret
        appConfig.autoConfigFromDevice = false //是否启动跟随配置，需要控制台配置
        appConfig.type = XP2PProtocolType.XP2P_PROTOCOL_AUTO
    }

    override fun getViewBinding(): ActivityVideoMultiPreviewBinding =
        ActivityVideoMultiPreviewBinding.inflate(layoutInflater)

    override fun onResume() {
        super.onResume()
        adapter = DevPreviewAdapter(this@VideoMultiPreviewActivity, allDevUrl)
        binding.glVideo.layoutManager = linearLayoutManager
        binding.glVideo.adapter = adapter
        switchOrientation(orientation)
    }

    override fun initView() {
        XP2P.setCallback(this)
        getDeviceP2PInfo()
        gridLayoutManager = GridLayoutManager(this@VideoMultiPreviewActivity, 2)
        linearLayoutManager = LinearLayoutManager(this@VideoMultiPreviewActivity)
        intent.getBundleExtra(VideoConst.VIDEO_CONFIG)?.let { it ->
            val channels = it.getIntegerArrayList(VideoConst.VIDEO_DEV_CHANNELS) ?: return@let
            allDevUrl = channels.map { devChannel ->
                DevUrl2Preview().apply {
                    devName = presenter.getDeviceName()
                    Status = 1
                    channel = devChannel
                }
            }.toMutableList()
            var column = 2
            if (channels.size <= 1) column = 1  // 当只有一个元素的时候，网格只有一列
            gridLayoutManager = GridLayoutManager(this@VideoMultiPreviewActivity, column)
            linearLayoutManager = LinearLayoutManager(this@VideoMultiPreviewActivity)
        }
        switchOrientation(true)
        binding.rgOrientation.check(binding.radioOrientationV.id)
        bindSurface()
    }

    private fun startService() {
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
            getNvrIpcStatus("${presenter.getProductId()}/${presenter.getDeviceName()}") { devPreview ->
                launch(Dispatchers.Main) {
                    Toast.makeText(
                        this@VideoMultiPreviewActivity,
                        "检测设备状态完成",
                        Toast.LENGTH_SHORT
                    ).show()
                    setPlayerSource(devPreview.player, devPreview.channel)
                }
            }
        }
    }

    private fun getNvrIpcStatus(id: String, block: (DevUrl2Preview) -> Unit) {
        getHolderById(id).forEachIndexed { index, devUrl2Preview ->
            val tmpCountDownLatch = CountDownLatch(1)
            thread {
                Timer().schedule(object : TimerTask() {
                    override fun run() {
                        val command = Command.getNvrIpcStatus(devUrl2Preview.channel, 0)
                        val repStatus = XP2P.postCommandRequestSync(
                            "${presenter.getProductId()}/${presenter.getDeviceName()}",
                            command.toByteArray(),
                            command.toByteArray().size.toLong(),
                            2 * 1000 * 1000
                        )
                        JSONArray.parseArray(repStatus, DevStatus::class.java)
                            ?.let { devStatus ->
                                if (devStatus.size == 1 && devStatus[0].status == 0) {
                                    tmpCountDownLatch.countDown()
                                }
                            }
                    }
                }, 500)
            }
            tmpCountDownLatch.await()
            block.invoke(devUrl2Preview)
        }
    }

    private fun getHolderById(id: String): MutableList<DevUrl2Preview> {
        if (TextUtils.isEmpty(id)) return ArrayList()
        val ret = ArrayList<DevUrl2Preview>()
        for (devUrl in allDevUrl) {
            if (!TextUtils.isEmpty(devUrl.devName) && id.endsWith(devUrl.devName)) {
                ret.add(devUrl)
            }
        }
        return ret
    }

    private fun restartService() {
        val id = "${presenter.getProductId()}/${presenter.getDeviceName()}"
        XP2P.stopService(id)
        getDeviceP2PInfo()
    }

    private fun bindSurface() {
        if (App.data.accessInfo == null) return
        allDevUrl.forEach { devPreview ->
            if (devPreview.Status != 1) return@forEach
            val player = IjkMediaPlayer()
            devPreview.surfaceTextureListener = object : TextureView.SurfaceTextureListener {
                override fun onSurfaceTextureAvailable(
                    surface: SurfaceTexture?,
                    width: Int,
                    height: Int
                ) {
                    surface?.let {
                        devPreview.surface = Surface(surface)
                        player.setSurface(devPreview.surface)
                    }
                }

                override fun onSurfaceTextureSizeChanged(
                    surface: SurfaceTexture?,
                    width: Int,
                    height: Int
                ) {
                }

                override fun onSurfaceTextureDestroyed(surface: SurfaceTexture?): Boolean {
                    return false
                }

                override fun onSurfaceTextureUpdated(surface: SurfaceTexture?) {}
            }
            devPreview.player = player
        }
    }

    private fun setPlayerSource(player: IjkMediaPlayer?, channel: Int) {
        val urlPrefix =
            XP2P.delegateHttpFlv("${presenter.getProductId()}/${presenter.getDeviceName()}")
        if (!TextUtils.isEmpty(urlPrefix)) {
            player?.let {
                val url = urlPrefix + Command.getVideoHightQualityUrlSuffix(channel)
                playPlayer(it, url)
            }
        }
    }

    private fun playPlayer(player: IjkMediaPlayer, url: String) {
        player.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "analyzemaxduration", 100)
        player.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "probesize", 25 * 1024)
        player.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "packet-buffering", 0)
        player.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "start-on-prepared", 1)
        player.setOption(IjkMediaPlayer.OPT_CATEGORY_CODEC, "threads", 1)
        player.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "sync-av-start", 0)
        player.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "reconnect", 1)
        player.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec", 1)
        player.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec-auto-rotate", 1)
        player.setOption(
            IjkMediaPlayer.OPT_CATEGORY_PLAYER,
            "mediacodec-handle-resolution-change",
            1
        )

        player.dataSource = url
        player.prepareAsync()
        player.start()
    }

    override fun setListener() {
        binding.rgOrientation.setOnCheckedChangeListener { group, checkedId ->
            when (checkedId) {
                binding.radioOrientationH.id -> switchOrientation(false)
                binding.radioOrientationV.id -> switchOrientation(true)
            }
        }
    }

    private fun switchOrientation(orientationV: Boolean) {
        if (orientationV) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
            binding.radioOrientationV.visibility = View.GONE
            binding.radioOrientationH.visibility = View.VISIBLE
            binding.glVideo.layoutManager = linearLayoutManager
        } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE)
            binding.radioOrientationV.visibility = View.VISIBLE
            binding.radioOrientationH.visibility = View.GONE
            binding.glVideo.layoutManager = gridLayoutManager
        }
        orientation = orientationV
        adapter?.notifyDataSetChanged()
    }

    override fun fail(msg: String?, errorCode: Int) {}
    override fun updateXp2pInfo(xp2pInfo: String) {
        Log.d(tag, "update xp2p2Info:$xp2pInfo")
        startService()
    }

    override fun commandRequest(id: String?, msg: String?) {}

    override fun xp2pEventNotify(id: String?, msg: String?, event: Int) {
        Log.d(tag, "id=${id},event=${event},msg=${msg}")
        if (event == 1003) {
            Log.e(tag, "====event === 1003")
            startShowVideoTime = 0L
            launch(Dispatchers.Main) {
                val content = getString(R.string.disconnected_and_reconnecting, id)
                Toast.makeText(this@VideoMultiPreviewActivity, content, Toast.LENGTH_SHORT).show()
                if (!isRestart) {
                    restartService()
                    isRestart = true
                }
            }

        } else if (event == 1004) {
            connectTime = System.currentTimeMillis() - connectStartTime
            Log.e(tag, "====event === 1004")
            checkDeviceState()
        } else if (event == 1005) {

        } else if (event == 1010) {
            Log.e(tag, "====event === 1010, 校验失败，info撞库防止串流： $msg")
        }
    }

    override fun avDataRecvHandle(id: String?, data: ByteArray?, len: Int) {}
    override fun avDataCloseHandle(id: String?, msg: String?, errorCode: Int) {}
    override fun onDeviceMsgArrived(id: String?, data: ByteArray?, len: Int): String {
        return "app reply to device"
    }

    override fun onPause() {
        super.onPause()
//        finishAll()
    }

    override fun eventReady(events: MutableList<ActionRecord>) {
    }

    private fun finishAll() {
        for (devPlayer in allDevUrl) {
            devPlayer.player?.release()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        finishAll()
        XP2P.setCallback(null)
        cancel()
    }

    companion object {
        fun startMultiPreviewActivity(context: Context?, dev: DevInfo, channels: ArrayList<Int>) {
            if (context == null) return

            val intent = Intent(context, VideoMultiPreviewActivity::class.java)
            val bundle = Bundle()
            intent.putExtra(VideoConst.VIDEO_CONFIG, bundle)
            val devInfo = DevUrl2Preview()
            devInfo.devName = dev.DeviceName
            devInfo.Status = dev.Status
            devInfo.channel = dev.Channel
            bundle.putString(VideoConst.VIDEO_CONFIG, JSON.toJSONString(devInfo))
            bundle.putIntegerArrayList(VideoConst.VIDEO_DEV_CHANNELS, channels)
            context.startActivity(intent)
        }
    }
}