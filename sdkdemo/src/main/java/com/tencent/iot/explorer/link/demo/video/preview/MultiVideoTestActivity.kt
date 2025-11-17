package com.tencent.iot.explorer.link.demo.video.preview

import android.graphics.SurfaceTexture
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.Surface
import android.view.TextureView
import android.widget.Toast
import com.alibaba.fastjson.JSONArray
import com.tencent.iot.explorer.link.demo.R
import com.tencent.iot.explorer.link.demo.VideoBaseActivity
import com.tencent.iot.explorer.link.demo.databinding.ActivityMultiVideoTestBinding
import com.tencent.iot.explorer.link.demo.video.Command
import com.tencent.iot.video.link.entity.DeviceStatus
import com.tencent.xnet.XP2P
import com.tencent.xnet.XP2PAppConfig
import com.tencent.xnet.XP2PCallback
import com.tencent.xnet.annotations.XP2PProtocolType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import tv.danmaku.ijk.media.player.IMediaPlayer
import tv.danmaku.ijk.media.player.IjkMediaPlayer
import java.util.concurrent.Executors

class MultiVideoTestActivity : VideoBaseActivity<ActivityMultiVideoTestBinding>(), XP2PCallback,
    IMediaPlayer.OnInfoListener {

    private val tag = MultiVideoTestActivity::class.simpleName
    private val taskThread = Executors.newSingleThreadExecutor()

    private val coroutineScope = CoroutineScope(Dispatchers.Main)

    // 设备信息
    private var device1Info: DeviceInfo? = null
    private var device2Info: DeviceInfo? = null
    private var device3Info: DeviceInfo? = null
    private var device4Info: DeviceInfo? = null

    // 四个设备的播放器
    private lateinit var player1: IjkMediaPlayer
    private lateinit var player2: IjkMediaPlayer
    private lateinit var player3: IjkMediaPlayer
    private lateinit var player4: IjkMediaPlayer

    // 四个设备的Surface
    private lateinit var surface1: Surface
    private lateinit var surface2: Surface
    private lateinit var surface3: Surface
    private lateinit var surface4: Surface

    // 四个设备的URL前缀
    private var urlPrefix1 = ""
    private var urlPrefix2 = ""
    private var urlPrefix3 = ""
    private var urlPrefix4 = ""

    private val xP2PAppConfig1 = XP2PAppConfig().also { appConfig ->
        appConfig.appKey = "xxx"
        appConfig.appSecret = "xxx"
        appConfig.autoConfigFromDevice = false
        appConfig.type = XP2PProtocolType.XP2P_PROTOCOL_AUTO
    }

    private val xP2PAppConfig2 = XP2PAppConfig().also { appConfig ->
        appConfig.appKey = "xxx"
        appConfig.appSecret = "xxx"
        appConfig.autoConfigFromDevice = false
        appConfig.type = XP2PProtocolType.XP2P_PROTOCOL_AUTO
    }

    private val xP2PAppConfig3 = XP2PAppConfig().also { appConfig ->
        appConfig.appKey = "xxx"
        appConfig.appSecret = "xxx"
        appConfig.autoConfigFromDevice = false
        appConfig.type = XP2PProtocolType.XP2P_PROTOCOL_AUTO
    }

    private val xP2PAppConfig4 = XP2PAppConfig().also { appConfig ->
        appConfig.appKey = "xxx"
        appConfig.appSecret = "xxx"
        appConfig.autoConfigFromDevice = false
        appConfig.type = XP2PProtocolType.XP2P_PROTOCOL_AUTO
    }

    override fun getViewBinding(): ActivityMultiVideoTestBinding =
        ActivityMultiVideoTestBinding.inflate(layoutInflater)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initDeviceInfoFromIntent()
        initView()
        setListener()
        startMultiDeviceServices()
    }

    private fun initDeviceInfoFromIntent() {
        device1Info = DeviceInfo(
            productId = intent.getStringExtra("device1_productId") ?: "",
            deviceName = intent.getStringExtra("device1_deviceName") ?: "",
            p2pInfo = intent.getStringExtra("device1_p2pInfo") ?: ""
        )

        device2Info = DeviceInfo(
            productId = intent.getStringExtra("device2_productId") ?: "",
            deviceName = intent.getStringExtra("device2_deviceName") ?: "",
            p2pInfo = intent.getStringExtra("device2_p2pInfo") ?: ""
        )

        device3Info = DeviceInfo(
            productId = intent.getStringExtra("device3_productId") ?: "",
            deviceName = intent.getStringExtra("device3_deviceName") ?: "",
            p2pInfo = intent.getStringExtra("device3_p2pInfo") ?: ""
        )

        device4Info = DeviceInfo(
            productId = intent.getStringExtra("device4_productId") ?: "",
            deviceName = intent.getStringExtra("device4_deviceName") ?: "",
            p2pInfo = intent.getStringExtra("device4_p2pInfo") ?: ""
        )
    }

    override fun initView() {
        with(binding) {
            vTitle.tvTitle.setText(R.string.multi_device_test)

            // 显示设备连接状态
            tvDevice1Status.text = "设备1: ${device1Info?.deviceName}"
            tvDevice2Status.text = "设备2: ${device2Info?.deviceName}"
            tvDevice3Status.text = "设备3: ${device3Info?.deviceName}"
            tvDevice4Status.text = "设备4: ${device4Info?.deviceName}"

            // 设置SurfaceTextureListener
            vPreviewDevice1.surfaceTextureListener = object : TextureView.SurfaceTextureListener {
                override fun onSurfaceTextureAvailable(
                    surface: SurfaceTexture?,
                    width: Int,
                    height: Int
                ) {
                    surface1 = Surface(surface)
                    player1.setSurface(surface1)
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

                override fun onSurfaceTextureUpdated(surface: SurfaceTexture?) {
                }

            }
            vPreviewDevice2.surfaceTextureListener = object : TextureView.SurfaceTextureListener {
                override fun onSurfaceTextureAvailable(
                    surface: SurfaceTexture?,
                    width: Int,
                    height: Int
                ) {
                    surface2 = Surface(surface)
                    player2.setSurface(surface2)
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

                override fun onSurfaceTextureUpdated(surface: SurfaceTexture?) {
                }

            }
            vPreviewDevice3.surfaceTextureListener = object : TextureView.SurfaceTextureListener {
                override fun onSurfaceTextureAvailable(
                    surface: SurfaceTexture?,
                    width: Int,
                    height: Int
                ) {
                    surface3 = Surface(surface)
                    player3.setSurface(surface3)
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

                override fun onSurfaceTextureUpdated(surface: SurfaceTexture?) {
                }

            }
            vPreviewDevice4.surfaceTextureListener = object : TextureView.SurfaceTextureListener {
                override fun onSurfaceTextureAvailable(
                    surface: SurfaceTexture?,
                    width: Int,
                    height: Int
                ) {
                    surface4 = Surface(surface)
                    player4.setSurface(surface4)
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

                override fun onSurfaceTextureUpdated(surface: SurfaceTexture?) {
                }

            }
        }

        // 初始化播放器
        startPlayers()

        // 设置XP2P回调
        XP2P.setCallback(this)
    }

    override fun setListener() {
        with(binding) {
            vTitle.ivBack?.setOnClickListener { finish() }

            // 单个设备测试按钮
            btnTestDevice1.setOnClickListener {
                taskThread.submit {
                    device1Info?.let { it1 -> startSingleDeviceTest(it1, xP2PAppConfig1) }
                }
            }
            btnTestDevice2.setOnClickListener {
                taskThread.submit {
                    device2Info?.let { it1 -> startSingleDeviceTest(it1, xP2PAppConfig2) }
                }
            }
            btnTestDevice3.setOnClickListener {
                taskThread.submit {
                    device3Info?.let { it1 -> startSingleDeviceTest(it1, xP2PAppConfig3) }
                }
            }
            btnTestDevice4.setOnClickListener {
                taskThread.submit {
                    device4Info?.let { it1 -> startSingleDeviceTest(it1, xP2PAppConfig4) }
                }
            }

            // 停止所有设备
            btnStopAll.setOnClickListener {
                stopMultiDeviceServices()
            }
        }
    }

    private fun startMultiDeviceServices() {
        taskThread.submit {
            device1Info?.let { it1 -> startSingleDeviceTest(it1, xP2PAppConfig1) }
            device2Info?.let { it1 -> startSingleDeviceTest(it1, xP2PAppConfig2) }
            device3Info?.let { it1 -> startSingleDeviceTest(it1, xP2PAppConfig3) }
            device4Info?.let { it1 -> startSingleDeviceTest(it1, xP2PAppConfig4) }
        }

//        runOnUiThread {
//            Toast.makeText(this, "开始启动4个设备连接...", Toast.LENGTH_SHORT).show()
//        }
//        updateDeviceStatus(deviceIndex, "连接成功")
    }

    private fun updateDeviceStatus(deviceIndex: Int, status: String) {
        when (deviceIndex) {
            1 -> binding.tvDevice1Status.text = "设备1: ${device1Info?.deviceName} - $status"
            2 -> binding.tvDevice2Status.text = "设备2: ${device2Info?.deviceName} - $status"
            3 -> binding.tvDevice3Status.text = "设备3: ${device3Info?.deviceName} - $status"
            4 -> binding.tvDevice4Status.text = "设备4: ${device4Info?.deviceName} - $status"
        }
    }

    private fun stopMultiDeviceServices() {

        // 停止XP2P服务
        device1Info?.let { stopDeviceService(it, 1) }
        device2Info?.let { stopDeviceService(it, 2) }
        device3Info?.let { stopDeviceService(it, 3) }
        device4Info?.let { stopDeviceService(it, 4) }

        runOnUiThread {
            updateDeviceStatus(1, "已停止")
            updateDeviceStatus(2, "已停止")
            updateDeviceStatus(3, "已停止")
            updateDeviceStatus(4, "已停止")
            Toast.makeText(this, "所有设备连接已停止", Toast.LENGTH_SHORT).show()
        }
    }

    private fun stopDeviceService(deviceInfo: DeviceInfo, deviceIndex: Int) {
        try {
            XP2P.stopService("${deviceInfo.productId}/${deviceInfo.deviceName}")
            Log.d(tag, "设备$deviceIndex XP2P服务已停止")
        } catch (e: Exception) {
            Log.e(tag, "停止设备$deviceIndex XP2P服务失败", e)
        }
    }

    private fun startSingleDeviceTest(deviceInfo: DeviceInfo, xP2PAppConfig: XP2PAppConfig) {
        XP2P.startService(
            this@MultiVideoTestActivity,
            deviceInfo.productId, deviceInfo.deviceName,
            deviceInfo.p2pInfo,
            xP2PAppConfig
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        stopMultiDeviceServices()

        // 释放播放器资源
        releasePlayers()

        // 释放Surface资源
        releaseSurfaces()
    }

    private fun releasePlayers() {
        try {
            if (::player1.isInitialized) player1.release()
            if (::player2.isInitialized) player2.release()
            if (::player3.isInitialized) player3.release()
            if (::player4.isInitialized) player4.release()
        } catch (e: Exception) {
            Log.e(tag, "释放播放器资源失败", e)
        }
    }

    private fun releaseSurfaces() {
        try {
            if (::surface1.isInitialized) surface1.release()
            if (::surface2.isInitialized) surface2.release()
            if (::surface3.isInitialized) surface3.release()
            if (::surface4.isInitialized) surface4.release()
        } catch (e: Exception) {
            Log.e(tag, "释放Surface资源失败", e)
        }
    }

    data class DeviceInfo(
        val productId: String,
        val deviceName: String,
        val p2pInfo: String
    )

    // 播放器初始化方法
    private fun startPlayers() {
        player1 = IjkMediaPlayer()
        player2 = IjkMediaPlayer()
        player3 = IjkMediaPlayer()
        player4 = IjkMediaPlayer()

        // 设置播放器参数
        setupPlayer(player1)
        setupPlayer(player2)
        setupPlayer(player3)
        setupPlayer(player4)
    }

    private fun setupPlayer(player: IjkMediaPlayer) {
        player.setOnInfoListener(this)
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
    }

    private fun restartService(id: String?) {
        taskThread.submit {
            when (id) {
                "${device1Info?.productId}/${device1Info?.deviceName}" -> {
                    stopDeviceService(device1Info!!, 1)
                    device1Info?.let { it1 -> startSingleDeviceTest(it1, xP2PAppConfig1) }
                }

                "${device2Info?.productId}/${device2Info?.deviceName}" -> {
                    stopDeviceService(device2Info!!, 2)
                    device2Info?.let { it1 -> startSingleDeviceTest(it1, xP2PAppConfig2) }
                }

                "${device3Info?.productId}/${device3Info?.deviceName}" -> {
                    stopDeviceService(device3Info!!, 3)
                    device3Info?.let { it1 -> startSingleDeviceTest(it1, xP2PAppConfig3) }

                }

                "${device4Info?.productId}/${device4Info?.deviceName}" -> {
                    stopDeviceService(device4Info!!, 4)
                    device4Info?.let { it1 -> startSingleDeviceTest(it1, xP2PAppConfig4) }
                }
            }
        }
    }


    // IMediaPlayer.OnInfoListener接口实现
    override fun onInfo(p0: IMediaPlayer?, p1: Int, p2: Int): Boolean {
        return true
    }

    override fun onInfoSEI(p0: IMediaPlayer?, p1: Int, p2: Int, p3: String?): Boolean {
        return false
    }

    override fun onInfoAudioPcmData(p0: IMediaPlayer?, p1: ByteArray?, p2: Int) {
        // 音频PCM数据处理
    }

    // XP2PCallback接口实现
    override fun fail(id: String?, errorCode: Int) {
        Log.e(tag, "XP2P连接失败: $id, errorCode: $errorCode")
        if (-1011 == errorCode) {
            restartService(id)
        }
    }

    override fun commandRequest(id: String?, msg: String?) {
        Log.d(tag, "XP2P命令请求: id:$id, msg:$msg")
    }

    override fun xp2pEventNotify(id: String?, msg: String?, event: Int) {
        Log.d(tag, "XP2P事件通知: id:$id, msg:$msg, event:$event")

        when (event) {
            1003 -> {
                // 连接断开，尝试重连
                Log.e(tag, "XP2P连接断开，尝试重连")
                restartService(id)
                coroutineScope.launch(Dispatchers.Main) {
                    Toast.makeText(
                        this@MultiVideoTestActivity,
                        "连接断开，正在重连...",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            1004, 1005 -> {
                // 连接成功
                Log.d(tag, "${id} XP2P连接成功 thread:${Thread.currentThread().name}")
                if (event == 1004) {
                    when (id) {
                        "${device1Info?.productId}/${device1Info?.deviceName}" -> {
                            checkDeviceState(device1Info!!, player1, 1)
                        }

                        "${device2Info?.productId}/${device2Info?.deviceName}" -> {
                            checkDeviceState(device2Info!!, player2, 2)
                        }

                        "${device3Info?.productId}/${device3Info?.deviceName}" -> {
                            checkDeviceState(device3Info!!, player3, 3)
                        }

                        "${device4Info?.productId}/${device4Info?.deviceName}" -> {
                            checkDeviceState(device4Info!!, player4, 4)
                        }
                    }
                }
            }

            1010 -> {
                // 校验失败
                Log.e(tag, "XP2P校验失败: $msg")
            }
        }
    }

    fun checkDeviceState(deviceInfo: DeviceInfo, player: IjkMediaPlayer, index: Int) {
        coroutineScope.launch(Dispatchers.Main) {
            Log.d(tag, "====检测设备状态=== deviceInfo:$deviceInfo")
            getDeviceStatus("${deviceInfo.productId}/${deviceInfo.deviceName}") { isOnline, msg ->
                Log.d(tag, "====检测设备状态=== isOnline:$isOnline, msg:$msg")
                if (isOnline) {
                    startDeviceStream(deviceInfo, player, index)
                }
            }
        }
    }

    private fun getDeviceStatus(id: String?, block: ((Boolean, String) -> Unit)? = null) {
        var command: ByteArray? = null
        command =
            "action=inner_define&channel=0&cmd=get_device_st&type=live&quality=standard".toByteArray()
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

    override fun avDataRecvHandle(id: String?, data: ByteArray?, len: Int) {
        Log.d(tag, "AV数据接收: id:$id, len:$len")
    }

    override fun avDataCloseHandle(id: String?, msg: String?, errorCode: Int) {
        Log.d(tag, "AV数据关闭: id:$id, msg:$msg, errorCode:$errorCode")
    }

    override fun onDeviceMsgArrived(id: String?, data: ByteArray?, len: Int): String {
        Log.d(tag, "设备消息到达: id:$id, len:$len")
        return "{\"code\":\"0\",\"msg\":\"test command reply\"}"
    }

    // 启动设备流媒体播放
    private fun startDeviceStream(
        deviceInfo: DeviceInfo,
        player: IjkMediaPlayer,
        deviceIndex: Int
    ) {
        try {
            val deviceId = "${deviceInfo.productId}/${deviceInfo.deviceName}"
            val urlPrefix = XP2P.delegateHttpFlv(deviceId)
            if (urlPrefix.isNotEmpty()) {
                when (deviceIndex) {
                    1 -> urlPrefix1 = urlPrefix
                    2 -> urlPrefix2 = urlPrefix
                    3 -> urlPrefix3 = urlPrefix
                    4 -> urlPrefix4 = urlPrefix
                }

                // 设置播放URL（使用中等质量）
                val url = urlPrefix + Command.getVideoStandardQualityUrlSuffix(0)
                Log.d(tag, "设备$deviceIndex 视频流开始播放: url:$url")
                coroutineScope.launch(Dispatchers.Main) {
                    player.reset()
                    player.setSurface(
                        when (deviceIndex) {
                            1 -> surface1
                            2 -> surface2
                            3 -> surface3
                            4 -> surface4
                            else -> surface1
                        }
                    )
                    player.dataSource = url
                    player.prepareAsync()
                    player.start()

                    updateDeviceStatus(deviceIndex, "视频流播放中")
                    Toast.makeText(
                        this@MultiVideoTestActivity,
                        "设备$deviceIndex 视频流开始播放",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } else {
                coroutineScope.launch(Dispatchers.Main) {
                    updateDeviceStatus(deviceIndex, "获取流地址失败")
                    Toast.makeText(
                        this@MultiVideoTestActivity,
                        "设备$deviceIndex 获取流地址失败",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        } catch (e: Exception) {
            Log.e(tag, "设备$deviceIndex 启动视频流失败", e)
            coroutineScope.launch(Dispatchers.Main) {
                updateDeviceStatus(deviceIndex, "视频流失败: ${e.message}")
                Toast.makeText(
                    this@MultiVideoTestActivity,
                    "设备$deviceIndex 视频流失败: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}