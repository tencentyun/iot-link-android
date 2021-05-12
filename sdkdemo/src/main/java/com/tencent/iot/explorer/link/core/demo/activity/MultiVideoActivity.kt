package com.tencent.iot.explorer.link.core.demo.activity

import android.os.Handler
import android.os.Looper
import android.view.SurfaceHolder
import android.view.View
import android.widget.Toast
import com.tencent.iot.explorer.link.core.demo.R
import com.tencent.iot.video.link.consts.VideoConst
import com.tencent.xnet.XP2P
import com.tencent.xnet.XP2PCallback
import kotlinx.android.synthetic.main.activity_multi_video.*
import kotlinx.android.synthetic.main.activity_video.*
import tv.danmaku.ijk.media.player.IjkMediaPlayer
import java.util.*
import java.util.concurrent.CyclicBarrier

private var playerMap = TreeMap<String, PlayerClient>()
private lateinit var playerClient01: PlayerClient
private lateinit var playerClient02: PlayerClient

class MultiVideoActivity : BaseActivity(), XP2PCallback {

    private lateinit var secretId: String
    private lateinit var secretKey: String
    private lateinit var productId: String

    private val mHandler = Handler(Looper.getMainLooper())

    override fun getContentView(): Int {
        return R.layout.activity_multi_video
    }

    override fun initView() {
        val bundle = this.intent.extras
        secretId = bundle?.get(VideoConst.MULTI_VIDEO_SECRET_ID) as String
        secretKey = bundle.get(VideoConst.MULTI_VIDEO_SECRET_KEY) as String
        productId = bundle.get(VideoConst.MULTI_VIDEO_PROD_ID) as String

        playerClient01 = PlayerClient()
        playerClient02 = PlayerClient()

        playerClient01.deviceName = bundle.get(VideoConst.MULTI_VIDEO_DEVICE_NAME01) as String
        playerClient01.deviceId = productId + "/" + playerClient01.deviceName
        playerClient01.mPlayer = IjkMediaPlayer()
        playerClient01.barrier = CyclicBarrier(2)
        playerMap[playerClient01.deviceId] = playerClient01

        playerClient02.deviceName = bundle.get(VideoConst.MULTI_VIDEO_DEVICE_NAME02) as String
        playerClient02.deviceId = productId + "/" + playerClient02.deviceName
        playerClient02.mPlayer = IjkMediaPlayer()
        playerClient02.barrier = CyclicBarrier(2)
        playerMap[playerClient02.deviceId] = playerClient02

        video_view_01.holder.addCallback(object: SurfaceHolder.Callback {
            override fun surfaceChanged(holder: SurfaceHolder?, format: Int, width: Int, height: Int) { }
            override fun surfaceDestroyed(holder: SurfaceHolder?) { }
            override fun surfaceCreated(holder: SurfaceHolder?) {
                playerClient01.mPlayer.setDisplay(holder)
            }
        })
        video_view_02.holder.addCallback(object: SurfaceHolder.Callback {
            override fun surfaceChanged(holder: SurfaceHolder?, format: Int, width: Int, height: Int) { }
            override fun surfaceDestroyed(holder: SurfaceHolder?) { }
            override fun surfaceCreated(holder: SurfaceHolder?) {
                playerClient02.mPlayer.setDisplay(holder)
            }
        })

        playerClient01.mPlayer.setOnPreparedListener {
            mHandler.post {
                val viewWidth = video_view_01.width
                val videoWidth = playerClient01.mPlayer.videoWidth
                val videoHeight = playerClient01.mPlayer.videoHeight
                val lp = video_view_01.layoutParams
                lp.width = viewWidth
                lp.height = (videoHeight.toFloat() * viewWidth.toFloat() / videoWidth.toFloat()).toInt()
                video_view_01.layoutParams = lp
            }
        }

        playerClient02.mPlayer.setOnPreparedListener {
            mHandler.post {
                val viewWidth = video_view_02.width
                val videoWidth = playerClient02.mPlayer.videoWidth
                val videoHeight = playerClient02.mPlayer.videoHeight
                val lp = video_view_02.layoutParams
                lp.width = viewWidth
                lp.height = (videoHeight.toFloat() * viewWidth.toFloat() / videoWidth.toFloat()).toInt()
                video_view_02.layoutParams = lp
            }
        }
        xp2pStartAndPlayThread(this, playerClient01)
        xp2pStartAndPlayThread(this, playerClient02)
    }

    private fun xp2pStartAndPlayThread(xp2p: XP2PCallback, playerClient: PlayerClient) {
        XP2P.setCallback(xp2p)
        object : Thread() {
            override fun run() {
                val ret = openP2PChannel(productId, playerClient.deviceName, secretId, secretKey)
                if (ret == 0) {
                    avPlay(playerClient)
                } else {
                    runOnUiThread {
                        Toast.makeText(getApplicationContext(), "P2P通道建立失败，请检查设备是否上线", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }.start()
    }

    private fun avPlay(playerClient: PlayerClient) {
        playerClient.barrier.await()
        if (playerClient.isXp2pDetectReady) {
            playerClient.isXp2pDetectReady = false

            val url = XP2P.delegateHttpFlv(playerClient.deviceId) + "ipc.flv?action=live"

            playerClient.mPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "analyzemaxduration", 100)
            playerClient.mPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "probesize", 25 * 1024)
            playerClient.mPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "packet-buffering", 0)
            playerClient.mPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "start-on-prepared", 1)
            playerClient.mPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_CODEC, "threads", 1)
            playerClient.mPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "sync-av-start", 0)

            playerClient.mPlayer.dataSource = url
            playerClient.mPlayer.prepareAsync()
            playerClient.mPlayer.start()
        } else {
            runOnUiThread {
                Toast.makeText(getApplicationContext(), "P2P探测失败，请检查当前网络环境", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun openP2PChannel(productId: String, deviceName: String, secretId: String, secretKey: String): Int {
        XP2P.setQcloudApiCred(secretId, secretKey)
        return XP2P.startServiceWithXp2pInfo("$productId/$deviceName", productId, deviceName, "")
    }

    override fun onDestroy() {
        super.onDestroy()
        playerClient01.mPlayer.release()
        playerClient02.mPlayer.release()
        XP2P.stopService(playerClient01.deviceId)
        XP2P.stopService(playerClient02.deviceId)
    }

    override fun setListener() { }
    override fun commandRequest(id: String?, msg: String?) { }
    override fun fail(msg: String?, errorCode: Int) { }
    override fun avDataRecvHandle(id: String?, data: ByteArray?, len: Int) { }
    override fun avDataCloseHandle(id: String?, msg: String?, errorCode: Int) { }
    override fun xp2pEventNotify(id: String?, msg: String?, event: Int) {
        var client: PlayerClient? = playerMap[id]
        if (event == 1003) {
            if (client != null) {
                client.isXp2pDisconnect = true
            }
        } else if (event == 1004) {
            if (client != null) {
                client.isXp2pDetectReady = true
                client.barrier.await()
            }
        } else if (event == 1005) {
            if (client != null) {
                client.isXp2pDetectError = true
                client.barrier.await()
            }
        }
    }
}

class PlayerClient {
    lateinit var barrier: CyclicBarrier
    var isXp2pDisconnect: Boolean = false
    var isXp2pDetectReady: Boolean = false
    var isXp2pDetectError: Boolean = false

    lateinit var deviceName: String
    lateinit var deviceId: String
    lateinit var mPlayer: IjkMediaPlayer
}