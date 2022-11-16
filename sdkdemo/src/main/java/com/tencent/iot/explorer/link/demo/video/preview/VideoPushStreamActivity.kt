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
import android.util.DisplayMetrics
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.JSONArray
import com.tencent.iot.explorer.link.demo.App
import com.tencent.iot.explorer.link.demo.R
import com.tencent.iot.explorer.link.demo.VideoBaseActivity
import com.tencent.iot.explorer.link.demo.video.Command
import com.tencent.iot.explorer.link.demo.video.DevInfo
import com.tencent.iot.explorer.link.demo.video.playback.cloudPlayback.event.ActionRecord
import com.tencent.iot.explorer.link.demo.video.playback.cloudPlayback.event.EventPresenter
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
import com.tencent.xnet.XP2PCallback
import kotlinx.android.synthetic.main.activity_video_push_stream.*
import kotlinx.android.synthetic.main.dash_board_layout.*
import kotlinx.android.synthetic.main.title_layout.*
import kotlinx.coroutines.*
import java.io.IOException
import java.lang.Runnable
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors


private var countDownLatchs : MutableMap<String, CountDownLatch> = ConcurrentHashMap()
private var keepPlayThreadLock = Object()
@Volatile
private var keepAliveThreadRuning = true

class VideoPushStreamActivity : VideoBaseActivity(), EventView, TextureView.SurfaceTextureListener,
    XP2PCallback, CoroutineScope by MainScope(), SurfaceHolder.Callback, OnEncodeListener {

    open var tag = VideoPushStreamActivity::class.simpleName
    lateinit var presenter: EventPresenter
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
        XP2P.dataSend("${App.data.accessInfo!!.productId}/${presenter.getDeviceName()}", data, data.size)
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

    override fun getContentView(): Int {
        return R.layout.activity_video_push_stream
    }

    override fun onResume() {
        super.onResume()
        keepAliveThreadRuning = true
        startPlayer()
        holder?.addCallback(this)
    }

    override fun initView() {
        presenter = EventPresenter(this@VideoPushStreamActivity)
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

        App.data.accessInfo?.let {
            presenter.setAccessId(it.accessId)
            presenter.setAccessToken(it.accessToken)
            presenter.setProductId(it.productId)
            presenter.getEventsData(Date())
        }

        XP2P.setCallback(this)
        XP2P.startService("${App.data.accessInfo!!.productId}/${presenter.getDeviceName()}",
            App.data.accessInfo!!.productId, presenter.getDeviceName()
        )

        holder = sv_camera_view.holder
        initAudioEncoder()
        initVideoEncoder()
    }

    private fun initAudioEncoder() {
        val micParam: MicParam = MicParam.Builder()
            .setAudioSource(MediaRecorder.AudioSource.MIC)
            .setSampleRateInHz(16000) // 采样率
            .setChannelConfig(AudioFormat.CHANNEL_IN_MONO)
            .setAudioFormat(AudioFormat.ENCODING_PCM_16BIT) // PCM
            .build()
        val audioEncodeParam: AudioEncodeParam = AudioEncodeParam.Builder().build()
        audioEncoder = AudioEncoder(micParam, audioEncodeParam)
        audioEncoder!!.setOnEncodeListener(this)
    }

    private fun initVideoEncoder() {
        val videoEncodeParam: VideoEncodeParam =
            VideoEncodeParam.Builder().setSize(vw, vh).setFrameRate(frameRate).setBitRate(vw * vh).build()
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

    open fun startPlayer() {
        if (App.data.accessInfo == null || TextUtils.isEmpty(presenter.getDeviceName())) return

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
                    Toast.makeText(this@VideoPushStreamActivity, errInfo, Toast.LENGTH_SHORT).show()
                }
                return@Runnable
            }
            XP2P.delegateHttpFlv(id)?.let {
                urlPrefix = it
                if (!TextUtils.isEmpty(urlPrefix)) {
                    setPlayerUrl(Command.getVideoHightQualityUrlSuffix(presenter.getChannel()))
                    keepPlayerplay(id)
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
                while (XP2P.startService(id, App.data.accessInfo!!.productId, presenter.getDeviceName())!=0
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
                    if (!TextUtils.isEmpty(urlPrefix)) setPlayerUrl(Command.getVideoHightQualityUrlSuffix(presenter.getChannel()))
                }
            }
        }.start()
    }

    override fun onSurfaceTextureAvailable(surface: SurfaceTexture?, width: Int, height: Int) {}

    override fun setListener() {
        iv_back.setOnClickListener { finish() }
    }

    open fun setPlayerUrl(suffix: String) {
        launch (Dispatchers.Main) {
            var command = Command.getNvrIpcStatus(presenter.getChannel(), 0)
            var repStatus = XP2P.postCommandRequestSync("${App.data.accessInfo?.productId}/${presenter.getDeviceName()}",
                command.toByteArray(), command.toByteArray().size.toLong(), 2 * 1000 * 1000) ?:""

            launch(Dispatchers.Main) {
                var retContent = StringBuilder(repStatus).toString()
                if (TextUtils.isEmpty(retContent)) {
                    retContent = getString(R.string.command_with_error, command)
                }
                Toast.makeText(this@VideoPushStreamActivity, retContent, Toast.LENGTH_SHORT).show()
            }

            // 开始推流
            XP2P.runSendService("${App.data.accessInfo?.productId}/${presenter.getDeviceName()}", "channel=0", false)
            handler.post(Runnable {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    startRecord()
                }
            })
        }
    }

    override fun eventReady(events: MutableList<ActionRecord>) {

    }

    override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture?, width: Int, height: Int) {}
    override fun onSurfaceTextureDestroyed(surface: SurfaceTexture?): Boolean { return false }
    override fun onSurfaceTextureUpdated(surface: SurfaceTexture?) {}
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
            keepPlayThreadLock?.let {
                synchronized(it) {
                    Log.d(tag, "====p2p链路断开, event=$event.")
                    it.notify()
                }
            } // 唤醒守护线程
            launch(Dispatchers.Main) {
                var content = getString(R.string.disconnected_and_reconnecting, id)
                Toast.makeText(this@VideoPushStreamActivity, content, Toast.LENGTH_SHORT).show()
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
        stopRecord()
        executor.shutdown()
        finishPlayer()
        App.data.accessInfo?.let {
            XP2P.stopService("${it.productId}/${presenter.getDeviceName()}")
        }
        XP2P.setCallback(null)
        cancel()
    }

    companion object {
        fun startPreviewActivity(context: Context?, dev: DevInfo) {
            context?:let { return }

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

    private fun getDeviceStatus(id: String?): Int {
        var command: ByteArray? = null
        command = "action=inner_define&channel=0&cmd=get_device_st&type=live&quality=high".toByteArray()
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
        Log.d(tag, "surfaceCreated")
        openCamera()
    }

    override fun surfaceChanged(p0: SurfaceHolder?, p1: Int, p2: Int, p3: Int) {
        Log.d(tag, "surfaceChanged")
    }

    override fun surfaceDestroyed(p0: SurfaceHolder?) {
        Log.d(tag, "surfaceDestroyed")
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