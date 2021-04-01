package com.tencent.iot.explorer.link.core.demo.activity

import android.os.Handler
import android.os.Looper
import android.view.SurfaceHolder
import com.tencent.iot.explorer.link.core.demo.R
import com.tencent.iot.video.link.consts.VideoConst
import com.tencent.xnet.XP2P
import com.tencent.xnet.XP2PCallback
import kotlinx.android.synthetic.main.activity_multi_video.*
import tv.danmaku.ijk.media.player.IjkMediaPlayer


class MultiVideoActivity : BaseActivity(), XP2PCallback {

    private lateinit var secretId: String
    private lateinit var secretKey: String
    private lateinit var productId: String
    private lateinit var deviceName01: String
    private lateinit var deviceName02: String
    private lateinit var mPlayer01: IjkMediaPlayer
    private lateinit var mPlayer02: IjkMediaPlayer
    private val mHandler = Handler(Looper.getMainLooper())

    override fun getContentView(): Int {
        return R.layout.activity_multi_video
    }

    override fun initView() {
        val bundle = this.intent.extras
        secretId = bundle?.get(VideoConst.MULTI_VIDEO_SECRET_ID) as String
        secretKey = bundle.get(VideoConst.MULTI_VIDEO_SECRET_KEY) as String
        productId = bundle.get(VideoConst.MULTI_VIDEO_PROD_ID) as String
        deviceName01 = bundle.get(VideoConst.MULTI_VIDEO_DEVICE_NAME01) as String
        deviceName02 = bundle.get(VideoConst.MULTI_VIDEO_DEVICE_NAME02) as String

        mPlayer01 = IjkMediaPlayer()
        mPlayer02 = IjkMediaPlayer()
        video_view_01.holder.addCallback(object: SurfaceHolder.Callback {
            override fun surfaceChanged(holder: SurfaceHolder?, format: Int, width: Int, height: Int) { }
            override fun surfaceDestroyed(holder: SurfaceHolder?) { }
            override fun surfaceCreated(holder: SurfaceHolder?) {
                mPlayer01.setDisplay(holder)
            }
        })
        video_view_02.holder.addCallback(object: SurfaceHolder.Callback {
            override fun surfaceChanged(holder: SurfaceHolder?, format: Int, width: Int, height: Int) { }
            override fun surfaceDestroyed(holder: SurfaceHolder?) { }
            override fun surfaceCreated(holder: SurfaceHolder?) {
                mPlayer02.setDisplay(holder)
            }
        })

        mPlayer01.setOnPreparedListener {
            mHandler.post {
                val viewWidth = video_view_01.width
                val videoWidth = mPlayer01.videoWidth
                val videoHeight = mPlayer01.videoHeight
                val lp = video_view_01.layoutParams
                lp.width = viewWidth
                lp.height = (videoHeight.toFloat() * viewWidth.toFloat() / videoWidth.toFloat()).toInt()
                video_view_01.layoutParams = lp
            }
        }

        mPlayer02.setOnPreparedListener {
            mHandler.post {
                val viewWidth = video_view_02.width
                val videoWidth = mPlayer02.videoWidth
                val videoHeight = mPlayer02.videoHeight
                val lp = video_view_02.layoutParams
                lp.width = viewWidth
                lp.height = (videoHeight.toFloat() * viewWidth.toFloat() / videoWidth.toFloat()).toInt()
                video_view_02.layoutParams = lp
            }
        }
        startPlay(mPlayer01, deviceName01)
        startPlay(mPlayer02, deviceName02)
    }


    private fun openP2PChannel(productId: String, deviceName: String, secretId: String, secretKey: String): Int {
        XP2P.setQcloudApiCred(secretId, secretKey)
        XP2P.setCallback(this)
        return XP2P.startServiceWithXp2pInfo("$productId/$deviceName", productId, deviceName, "")
    }

    private fun startPlay(player: IjkMediaPlayer, deviceName: String) {
        val ret = openP2PChannel(productId, deviceName, secretId, secretKey)
        if (ret == 0) {
            val url = XP2P.delegateHttpFlv("$productId/$deviceName") + "ipc.flv?action=live"
            player.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "analyzemaxduration", 100)
            player.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "probesize", 25 * 1024)
            player.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "packet-buffering", 0)
            player.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "start-on-prepared", 1)
            player.setOption(IjkMediaPlayer.OPT_CATEGORY_CODEC, "threads", 1)
            player.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "sync-av-start", 0)
            player.dataSource = url
            player.prepareAsync()
            player.start()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mPlayer01.release()
        mPlayer02.release()
        XP2P.stopService("$productId/$deviceName01")
        XP2P.stopService("$productId/$deviceName02")
    }

    override fun setListener() { }
    override fun commandRequest(id: String?, msg: String?) { }
    override fun xp2pLinkError(id: String?, msg: String?) { }
    override fun fail(msg: String?, errorCode: Int) { }
    override fun avDataRecvHandle(id: String?, data: ByteArray?, len: Int) { }
    override fun avDataCloseHandle(id: String?, msg: String?, errorCode: Int) { }
}