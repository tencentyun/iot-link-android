package com.tencent.iot.explorer.link.core.demo.activity

import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import android.view.SurfaceHolder
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.tencent.iot.explorer.link.core.demo.R
import com.tencent.iot.explorer.link.core.demo.adapter.OnItemListener
import com.tencent.iot.explorer.link.core.demo.adapter.VideoListAdapter
import com.tencent.iot.explorer.link.core.demo.holder.BaseHolder
import com.tencent.iot.explorer.link.core.demo.log.L
import com.tencent.iot.explorer.link.core.demo.view.MyDivider
import com.tencent.iot.video.link.consts.VideoConst
import com.tencent.iot.video.link.entity.PlaybackVideoEntity
import com.tencent.iot.video.link.util.JsonManager
import com.tencent.xnet.XP2P
import com.tencent.xnet.XP2PCallback
import kotlinx.android.synthetic.main.activity_playback_video.*
import tv.danmaku.ijk.media.player.IjkMediaPlayer
import java.text.SimpleDateFormat
import java.util.*

class PlaybackVideoActivity  : BaseActivity(), View.OnClickListener, SurfaceHolder.Callback,
    XP2PCallback {

    private lateinit var secretId: String
    private lateinit var secretKey: String
    private lateinit var productId: String
    private lateinit var deviceName: String
    private var isPlaying: Boolean = true
    private var isP2PChannelAvailable: Boolean = false

    private var videoList = arrayListOf<PlaybackVideoEntity>()
    private lateinit var adapter: VideoListAdapter

    private lateinit var mPlayer: IjkMediaPlayer
    private val mHandler = Handler(Looper.getMainLooper())

    override fun getContentView(): Int {
        return R.layout.activity_playback_video
    }

    override fun initView() {
        enableSaveLog()
        adapter = VideoListAdapter(this, videoList)
        playback_video_list.addItemDecoration(MyDivider(dp2px(16), dp2px(16), dp2px(16)))
        playback_video_list.layoutManager = LinearLayoutManager(this)
        playback_video_list.adapter = adapter

        val bundle = this.intent.extras
        secretId = bundle?.get(VideoConst.VIDEO_SECRET_ID) as String
        secretKey = bundle.get(VideoConst.VIDEO_SECRET_KEY) as String
        productId = bundle.get(VideoConst.VIDEO_PRODUCT_ID) as String
        deviceName = bundle.get(VideoConst.VIDEO_DEVICE_NAME) as String

        playback_video_view.holder.addCallback(this)
        mPlayer = IjkMediaPlayer()
        mPlayer.setOnPreparedListener {
            mHandler.post {
                val viewWidth = playback_video_view.width
                val videoWidth = mPlayer.videoWidth
                val videoHeight = mPlayer.videoHeight
                val lp = playback_video_view.layoutParams
                lp.width = viewWidth
                lp.height = (videoHeight.toFloat() * viewWidth.toFloat() / videoWidth.toFloat()).toInt()
                playback_video_view.layoutParams = lp
            }
        }
        if (productId == " " || deviceName == " " || secretId == " " || secretKey == " ") {
            Toast.makeText(this, "设备信息有误，请确保配置文件中的设备信息填写正确", Toast.LENGTH_LONG).show()
        } else {
            val ret = openP2PChannel(productId, deviceName, secretId, secretKey)
            if (ret == 0) {
                isP2PChannelAvailable = true
                val jsonArray = XP2P.getComandRequestWithSync("action=inner_define&cmd=get_record_index", 2*1000*1000)
                if (!TextUtils.isEmpty(jsonArray)) {
                    val list = JsonManager.parseJsonArray(jsonArray, PlaybackVideoEntity::class.java)
                    videoList.addAll(list)
                    adapter.notifyDataSetChanged()
                } else {
                    Toast.makeText(this, "录像列表为空", Toast.LENGTH_LONG).show()
//                    val url = XP2P.delegateHttpFlv() + "ipc.flv?action=playback"
//                    mPlayer.dataSource = url
//                    mPlayer.prepareAsync()
//                    mPlayer.start()
                }
            } else {
                isP2PChannelAvailable = false
                watch_playback.visibility = View.GONE
                Toast.makeText(this, "P2P通道建立失败，请检查设备是否上线", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun setListener() {
        watch_playback.setOnClickListener(this)
        user_define_test.setOnClickListener(this)
        adapter.setOnItemListener(object : OnItemListener {
            override fun onItemClick(holder: BaseHolder<*>, clickView: View, position: Int) {
                val videoEntity = videoList[position]
                val startTime = date2TimeStamp(videoEntity.start_time, "yyyy-MM-dd_HH-mm-ss")
                val endTime = date2TimeStamp(videoEntity.end_time, "yyyy-MM-dd_HH-mm-ss")
                val url = XP2P.delegateHttpFlv() + "ipc.flv?action=playback&start_time=${startTime}&end_time=${endTime}"
                if (mPlayer != null) {
                    resetWatchState()
                    mPlayer.reset()
                    mPlayer.dataSource = url
                    mPlayer.setSurface(playback_video_view.holder.surface)
                    mPlayer.prepareAsync()
                    mPlayer.start()
                }
            }
        })
    }

    override fun onClick(v: View?) {
        when(v) {
            watch_playback -> {
                if (isPlaying) {
                    mPlayer.pause()
                    isPlaying = false
                    watch_playback.text = "开始播放"
                } else {
                    mPlayer.start()
                    resetWatchState()
                }
            }
            user_define_test -> {
                if (isP2PChannelAvailable) {
                    XP2P.getCommandRequestWithAsync("action=user_define&cmd=custom_cmd")
                } else {
                    Toast.makeText(this, "P2P通道未开启", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun openP2PChannel(productId: String, deviceName: String, secretId: String, secretKey: String): Int {
        XP2P.setDeviceInfo(productId, deviceName)
        XP2P.setQcloudApiCred(secretId, secretKey)
        XP2P.setXp2pInfoAttributes("_sys_xp2p_info")
        XP2P.setCallback(this)
        val ret = XP2P.startServiceWithXp2pInfo("")
        return if (ret == 0) {
            Thread.sleep(1000)
            ret
        } else {
            ret
        }
    }

    private fun enableSaveLog() {
        val sdf = SimpleDateFormat("", Locale.SIMPLIFIED_CHINESE)
        sdf.applyPattern("yyyy-MM-dd-HH-mm-ss")
        val filePath: String = this.externalCacheDir!!.absolutePath + "/logcat-${sdf.format(System.currentTimeMillis())}.txt"
        Runtime.getRuntime().exec(arrayOf("logcat", "-f", filePath, "XP2P-LOG:V", "*:S"))
    }

    override fun surfaceChanged(holder: SurfaceHolder?, format: Int, width: Int, height: Int) {
    }

    override fun surfaceDestroyed(holder: SurfaceHolder?) { }

    override fun surfaceCreated(holder: SurfaceHolder?) {
        mPlayer.setDisplay(holder)
    }

    override fun onDestroy() {
        super.onDestroy()
        mPlayer.release()
        XP2P.stopService()
    }

    override fun commandRequest(msg: String?, len: Int) {
        runOnUiThread {
            Toast.makeText(this, "$msg", Toast.LENGTH_LONG).show()
        }
    }

    override fun fail(msg: String?, errorCode: Int) {
    }

    override fun avDataRecvHandle(data: ByteArray?, len: Int) { // 音视频数据回调接口
    }

    private fun date2TimeStamp(dateString: String?, format: String?): Long {
        val sdf = SimpleDateFormat(format)
        return sdf.parse(dateString).time / 1000
    }

    private fun resetWatchState() {
        isPlaying = true
        watch_playback.text = "停止播放"
    }
}