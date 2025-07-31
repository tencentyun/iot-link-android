package com.tencent.iot.explorer.link.demo.video.preview

import android.Manifest
import android.content.Context
import android.content.Intent
import android.graphics.ImageFormat
import android.graphics.SurfaceTexture
import android.hardware.Camera
import android.media.AudioFormat
import android.media.MediaRecorder
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.text.TextUtils
import android.view.Surface
import android.view.SurfaceHolder
import android.view.TextureView
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.JSONArray
import com.tencent.iot.explorer.link.demo.App
import com.tencent.iot.explorer.link.demo.BuildConfig
import com.tencent.iot.explorer.link.demo.R
import com.tencent.iot.explorer.link.demo.common.log.L.ld
import com.tencent.iot.explorer.link.demo.common.log.L.le
import com.tencent.iot.explorer.link.demo.databinding.ActivityVideoPushStreamBinding
import com.tencent.iot.explorer.link.demo.video.Command
import com.tencent.iot.explorer.link.demo.video.DevInfo
import com.tencent.iot.explorer.link.demo.video.VideoPreviewBaseActivity
import com.tencent.iot.explorer.link.demo.video.playback.cloudPlayback.event.ActionRecord
import com.tencent.iot.explorer.link.demo.video.playback.cloudPlayback.event.EventView
import com.tencent.iot.thirdparty.flv.FLVListener
import com.tencent.iot.thirdparty.flv.FLVPacker
import com.tencent.iot.video.link.consts.CameraConstants
import com.tencent.iot.video.link.consts.VideoConst
import com.tencent.iot.video.link.encoder.AudioEncoder
import com.tencent.iot.video.link.encoder.VideoEncoder
import com.tencent.iot.video.link.entity.DeviceStatus
import com.tencent.iot.video.link.listener.OnEncodeListener
import com.tencent.iot.video.link.param.AudioEncodeParam
import com.tencent.iot.video.link.param.MicParam
import com.tencent.iot.video.link.param.VideoEncodeParam
import com.tencent.iot.video.link.util.CameraUtils
import com.tencent.xnet.XP2P
import com.tencent.xnet.XP2PAppConfig
import com.tencent.xnet.XP2PCallback
import com.tencent.xnet.annotations.XP2PProtocolType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.io.IOException
import java.util.Date
import java.util.concurrent.Executors

class VideoPushStreamActivity : VideoPreviewBaseActivity<ActivityVideoPushStreamBinding>(), EventView,
    TextureView.SurfaceTextureListener,
    XP2PCallback, CoroutineScope by MainScope(), SurfaceHolder.Callback, OnEncodeListener {

    open var tag = VideoPushStreamActivity::class.simpleName
    lateinit var surface: Surface

    var holder: SurfaceHolder? = null
    var camera: Camera? = null

    // 默认摄像头方向
    var facing: Int = CameraConstants.facing.BACK
    val vw = 320
    val vh = 240
    val frameRate = 15
    val flvListener = FLVListener { data: ByteArray ->
//                Log.e(TAG, "===== dataLen:" + data.size);
        XP2P.dataSend(
            "${App.data.accessInfo!!.productId}/${presenter.getDeviceName()}",
            data,
            data.size
        )
    }
    var audioEncoder: AudioEncoder? = null
    var videoEncoder: VideoEncoder? = null
    var flvPacker: FLVPacker? = null

    @Volatile
    var startEncodeVideo = false
    var executor = Executors.newSingleThreadExecutor()
    var handler = Handler()

    @Volatile
    var urlPrefix = ""
    var permissions = arrayOf(Manifest.permission.RECORD_AUDIO)

    @Volatile
    var connectStartTime = 0L

    @Volatile
    var connectTime = 0L
    val MSG_UPDATE_HUD = 1
    private val xP2PAppConfig = XP2PAppConfig().also { appConfig ->
        appConfig.appKey =
            BuildConfig.TencentIotLinkSDKDemoAppkey //为explorer平台注册的应用信息(https://console.cloud.tencent.com/iotexplorer/v2/instance/app/detai) explorer控制台- 应用开发 - 选对应的应用下的 appkey/appsecret
        appConfig.appSecret =
            BuildConfig.TencentIotLinkSDKDemoAppSecret //为explorer平台注册的应用信息(https://console.cloud.tencent.com/iotexplorer/v2/instance/app/detai) explorer控制台- 应用开发 - 选对应的应用下的 appkey/appsecret
        appConfig.autoConfigFromDevice = true
        appConfig.type = XP2PProtocolType.XP2P_PROTOCOL_AUTO
    }

    override fun onResume() {
        super.onResume()
        holder?.addCallback(this)
    }

    override fun getViewBinding(): ActivityVideoPushStreamBinding = ActivityVideoPushStreamBinding.inflate(layoutInflater)

    override fun initView() {
        binding.vTitle.tvTitle.setText(presenter.getDeviceName())
        XP2P.setCallback(this)
        getDeviceP2PInfo()
        holder = binding.svCameraView.holder
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
        ld { "====检测设备状态===" }
        launch(Dispatchers.IO) {
            getDeviceStatus("${presenter.getProductId()}/${presenter.getDeviceName()}") { isOnline, msg ->
                launch(Dispatchers.Main) {
                    Toast.makeText(this@VideoPushStreamActivity, msg, Toast.LENGTH_SHORT).show()
                    if (isOnline) {
                        delegateHttpFlv()
                    } else {
                        restartService()
                    }
                }
            }
        }
    }

    private fun restartService() {
        val id = "${presenter.getProductId()}/${presenter.getDeviceName()}"
        XP2P.stopService(id)
        getDeviceP2PInfo()
    }

    private fun delegateHttpFlv() {
        val id = "${presenter.getProductId()}/${presenter.getDeviceName()}"
        val prefix = XP2P.delegateHttpFlv(id)
        if (prefix.isNotEmpty()) {
            setPlayerUrl(Command.getVideoStandardQualityUrlSuffix(presenter.getChannel()))
        } else {
            Toast.makeText(this, "get urlPrefix is empty", Toast.LENGTH_SHORT)
                .show()
        }
    }

    private fun initAudioEncoder() {
        val micParam: MicParam = MicParam.Builder()
            .setAudioSource(MediaRecorder.AudioSource.VOICE_COMMUNICATION)
            .setSampleRateInHz(16000) // 采样率
            .setChannelConfig(AudioFormat.CHANNEL_IN_MONO)
            .setAudioFormat(AudioFormat.ENCODING_PCM_16BIT) // PCM
            .build()
        val audioEncodeParam: AudioEncodeParam = AudioEncodeParam.Builder().build()
        audioEncoder = AudioEncoder(micParam, audioEncodeParam, true, true)
        audioEncoder!!.setOnEncodeListener(this)
    }

    private fun initVideoEncoder() {
        val videoEncodeParam: VideoEncodeParam =
            VideoEncodeParam.Builder().setSize(vw, vh).setFrameRate(frameRate)
                .setBitRate(vw * vh * 4).build()
        videoEncoder = VideoEncoder(videoEncodeParam)
        videoEncoder!!.setEncoderListener(this)
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun startRecord() {
//        if (callType == CallingType.TYPE_VIDEO_CALL) {
        startEncodeVideo = true
//        }
        audioEncoder!!.start()
    }

    private fun stopRecord() {
        if (audioEncoder != null) {
            audioEncoder!!.stop()
        }
        if (videoEncoder != null) {
            videoEncoder!!.stop()
        }
        startEncodeVideo = false
    }


//    private fun keepPlayerplay(id: String?) {
//        if (TextUtils.isEmpty(id)) return
//        val accessId = App.data.accessInfo!!.accessId
//        val accessToken = App.data.accessInfo!!.accessToken
//        // 开启守护线程
//        Thread {
//            val objectLock = Object()
//            while (true) {
//                Log.d(tag, "id=${id} keepAliveThread wait disconnected msg")
//                synchronized(keepPlayThreadLock) {
//                    keepPlayThreadLock.wait()
//                }
//                Log.d(
//                    tag,
//                    "id=${id} keepAliveThread do not wait and keepAliveThreadRuning=${keepAliveThreadRuning}"
//                )
//                if (!keepAliveThreadRuning) break //锁被释放后，检查守护线程是否继续运行
//                flvPacker = null
//                stopRecord()
//                // 发现断开尝试恢复视频，每隔一秒尝试一次
//                Log.d(tag, "====开始尝试重连...")
//                XP2P.stopService(id)
//                XP2P.startService(
//                    this@VideoPushStreamActivity,
//                    id,
//                    App.data.accessInfo!!.productId,
//                    presenter.getDeviceName(),
//                    xP2PAppConfig
//                )
//                while (XP2P.setParamsForXp2pInfo(id, "") != 0
//                    || getDeviceStatus(id) != 0
//                ) {
//                    XP2P.stopService(id)
//                    synchronized(objectLock) {
//                        objectLock.wait(500)
//                    }
//                    Log.d(tag, "====正在重连...")
//                }
//                connectStartTime = System.currentTimeMillis()
//
//                Log.d(tag, "====尝试拉流...")
//                XP2P.delegateHttpFlv(id)?.let {
//                    urlPrefix = it
//                    if (!TextUtils.isEmpty(urlPrefix)) setPlayerUrl(
//                        Command.getVideoStandardQualityUrlSuffix(
//                            presenter.getChannel()
//                        )
//                    )
//                }
//
//            }
//        }.start()
//    }

    override fun onSurfaceTextureAvailable(surface: SurfaceTexture?, width: Int, height: Int) {}

    override fun setListener() {
        binding.vTitle.ivBack.setOnClickListener { finish() }
    }

    open fun setPlayerUrl(suffix: String) {
        launch(Dispatchers.Main) {
            var command = Command.getNvrIpcStatus(presenter.getChannel(), 0)
            var repStatus = XP2P.postCommandRequestSync(
                "${App.data.accessInfo?.productId}/${presenter.getDeviceName()}",
                command.toByteArray(), command.toByteArray().size.toLong(), 2 * 1000 * 1000
            ) ?: ""

            launch(Dispatchers.Main) {
                var retContent = StringBuilder(repStatus).toString()
                if (TextUtils.isEmpty(retContent)) {
                    retContent = getString(R.string.command_with_error, command)
                }
                Toast.makeText(this@VideoPushStreamActivity, retContent, Toast.LENGTH_SHORT).show()
            }

            // 开始推流
            XP2P.runSendService(
                "${App.data.accessInfo?.productId}/${presenter.getDeviceName()}",
                "channel=0",
                false
            )
            handler.post(Runnable {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    initAudioEncoder()
                    initVideoEncoder()
                    startRecord()
                }
            })
        }
    }

    override fun eventReady(events: MutableList<ActionRecord>) {

    }

    override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture?, width: Int, height: Int) {}
    override fun onSurfaceTextureDestroyed(surface: SurfaceTexture?): Boolean {
        return false
    }

    override fun onSurfaceTextureUpdated(surface: SurfaceTexture?) {}
    override fun fail(msg: String?, errorCode: Int) {}
    override fun updateXp2pInfo(xp2pInfo: String) {
        startService()
    }

    override fun commandRequest(id: String?, msg: String?) {}
    override fun avDataRecvHandle(id: String?, data: ByteArray?, len: Int) {}
    override fun avDataCloseHandle(id: String?, msg: String?, errorCode: Int) {}
    override fun onDeviceMsgArrived(id: String?, data: ByteArray?, len: Int): String {
        return "app reply to device"
    }

    override fun xp2pEventNotify(id: String?, msg: String?, event: Int) {
        le { "id=${id}, event=${event}" }
        if (event == 1003) {
            le { "====event === 1003" }
            launch(Dispatchers.Main) {
                var content = getString(R.string.disconnected_and_reconnecting, id)
                Toast.makeText(this@VideoPushStreamActivity, content, Toast.LENGTH_SHORT).show()
            }
            restartService()
        } else if (event == 1004 || event == 1005) {
            connectTime = System.currentTimeMillis() - connectStartTime
            if (event == 1004) {
                le { "====event === 1004" }
                checkDeviceState()
            }
        } else if (event == 1010) {
            le { "====event === 1010, 校验失败，info撞库防止串流： $msg" }
        }
    }

    override fun onPause() {
        super.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        stopRecord()
        executor.shutdown()
        XP2P.stopService("${presenter.getProductId()}/${presenter.getDeviceName()}")
        XP2P.setCallback(null)
        cancel()
    }

    companion object {
        fun startPreviewActivity(context: Context?, dev: DevInfo) {
            context ?: let { return }

            var intent = Intent(context, VideoPushStreamActivity::class.java)
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

    private fun getDeviceStatus(id: String?, block: ((Boolean, String) -> Unit)? = null) {
        var command: ByteArray? = null
        command =
            "action=inner_define&channel=0&cmd=get_device_st&type=live&quality=high".toByteArray()
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

    /**
     * 打开相机
     */
    private fun openCamera() {
        releaseCamera(camera)
        camera = Camera.open(facing)
        //获取相机参数
        val parameters = camera?.getParameters()

        //设置预览格式（也就是每一帧的视频格式）YUV420下的NV21
        parameters?.previewFormat = ImageFormat.NV21
        if (this.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
            val focusModes = parameters?.supportedFocusModes
            if (focusModes != null && focusModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO)) {
                parameters.focusMode = Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO
            }
        }
        var cameraIndex = -1
        if (facing == CameraConstants.facing.BACK) {
            cameraIndex = Camera.CameraInfo.CAMERA_FACING_BACK
        } else if (facing == CameraConstants.facing.FRONT) {
            cameraIndex = Camera.CameraInfo.CAMERA_FACING_FRONT
            camera?.setDisplayOrientation(180)
        }
        try {
            camera?.setDisplayOrientation(CameraUtils.getDisplayOrientation(this, cameraIndex))
        } catch (e: Exception) {
            e.printStackTrace()
        }

        //设置预览图像分辨率
        parameters?.setPreviewSize(vw, vh)
        //设置帧率
        parameters?.previewFrameRate = frameRate
        //配置camera参数
        camera?.setParameters(parameters)
        try {
            camera?.setPreviewDisplay(holder)
        } catch (e: IOException) {
            e.printStackTrace()
        }
        //设置监听获取视频流的每一帧
        camera?.setPreviewCallback(Camera.PreviewCallback { data, camera ->
            if (startEncodeVideo && videoEncoder != null) {
                videoEncoder?.encoderH264(data, facing == CameraConstants.facing.FRONT)
            }
        })
        //调用startPreview()用以更新preview的surface
        camera?.startPreview()
    }

    /**
     * 关闭相机
     */
    fun releaseCamera(camera: Camera?) {
        var camera = camera
        if (camera != null) {
            camera.setPreviewCallback(null)
            camera.stopPreview()
            camera.release()
            camera = null
        }
    }

    override fun surfaceCreated(p0: SurfaceHolder?) {
        ld { "surfaceCreated" }
        openCamera()
    }

    override fun surfaceChanged(p0: SurfaceHolder?, p1: Int, p2: Int, p3: Int) {
        ld { "surfaceChanged" }
    }

    override fun surfaceDestroyed(p0: SurfaceHolder?) {
        ld { "surfaceDestroyed" }
        p0?.removeCallback(this)
    }

    override fun onAudioEncoded(datas: ByteArray?, pts: Long, seqgigit: Long) {
        if (executor.isShutdown) return
        executor.submit {
            if (flvPacker == null)
                flvPacker = FLVPacker(flvListener, true, true)
            flvPacker?.encodeFlv(datas, FLVPacker.TYPE_AUDIO, pts)
        }
    }

    override fun onVideoEncoded(datas: ByteArray?, pts: Long, seq: Long) {
        if (executor.isShutdown) return
        executor.submit {
            if (flvPacker == null) flvPacker =
                FLVPacker(flvListener, true, true)
            flvPacker!!.encodeFlv(datas, FLVPacker.TYPE_VIDEO, pts)
        }
    }
}