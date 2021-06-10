package com.tencent.iot.explorer.link.core.demo.video.activity

import android.content.pm.ActivityInfo
import android.graphics.SurfaceTexture
import android.text.TextUtils
import android.util.Log
import android.view.Surface
import android.view.TextureView
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.alibaba.fastjson.JSONArray
import com.tencent.iot.explorer.link.core.demo.App
import com.tencent.iot.explorer.link.core.demo.R
import com.tencent.iot.explorer.link.core.demo.activity.*
import com.tencent.iot.explorer.link.core.demo.video.adapter.DevPreviewAdapter
import com.tencent.iot.explorer.link.core.demo.video.entity.DevUrl2Preview
import com.tencent.iot.video.link.consts.VideoConst
import com.tencent.xnet.XP2P
import com.tencent.xnet.XP2PCallback
import kotlinx.android.synthetic.main.activity_video.*
import kotlinx.android.synthetic.main.activity_video_multi_preview.*
import kotlinx.coroutines.*
import tv.danmaku.ijk.media.player.IjkMediaPlayer
import java.lang.Runnable
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CountDownLatch


private var countDownLatchs : MutableMap<String, CountDownLatch> = ConcurrentHashMap()

class VideoMultiPreviewActivity : BaseActivity(), XP2PCallback {
    lateinit var gridLayoutManager : GridLayoutManager
    lateinit var linearLayoutManager : LinearLayoutManager
    private var allDevUrl: MutableList<DevUrl2Preview> = ArrayList()
    private var adapter : DevPreviewAdapter? = null

    override fun getContentView(): Int {
        return R.layout.activity_video_multi_preview
    }

    override fun initView() {
        App.data.accessInfo?.let {
            XP2P.setQcloudApiCred(it.accessId, it.accessToken)
            XP2P.setCallback(this)
        }

        gridLayoutManager = GridLayoutManager(this@VideoMultiPreviewActivity, 2)
        linearLayoutManager = LinearLayoutManager(this@VideoMultiPreviewActivity)
        var bundle = intent.getBundleExtra(VideoConst.VIDEO_URLS)
        bundle?.let {
            var jsonArrStr = it.getString(VideoConst.VIDEO_URLS)
            jsonArrStr?.let {
                try {
                    allDevUrl = JSONArray.parseArray(it, DevUrl2Preview::class.java)
                    var column = 2
                    if (allDevUrl.size <= 1) column = 1  // 当只有一个元素的时候，网格只有一列
                    gridLayoutManager = GridLayoutManager(this@VideoMultiPreviewActivity, column)
                    linearLayoutManager = LinearLayoutManager(this@VideoMultiPreviewActivity)
                    adapter = DevPreviewAdapter(this@VideoMultiPreviewActivity, allDevUrl)
                    gl_video.layoutManager = linearLayoutManager
                    gl_video.adapter = adapter
                    playAll()
                } catch (e : Exception) {
                    e.printStackTrace()
                }
            }
        }
        switchOrientation(true)
        rg_orientation.check(radio_orientation_v.id)
    }

    private fun playAll() {
        if (App.data.accessInfo == null) return

        for (i in 0 until allDevUrl.size) {
            if (allDevUrl.get(i).Status != 1) continue
            var player = IjkMediaPlayer()
            allDevUrl.get(i).surfaceTextureListener = object : TextureView.SurfaceTextureListener {
                override fun onSurfaceTextureAvailable(surface: SurfaceTexture?, width: Int, height: Int) {
                    player.setSurface(Surface(surface))
                }

                override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture?, width: Int, height: Int) {}
                override fun onSurfaceTextureDestroyed(surface: SurfaceTexture?): Boolean {
                    return false
                }
                override fun onSurfaceTextureUpdated(surface: SurfaceTexture?) {}
            }

            setPlayerSource(player, allDevUrl.get(i).devName)
            allDevUrl.get(i).player = player
        }
    }

    private fun setPlayerSource(player: IjkMediaPlayer, devName: String) {
        Thread(Runnable {
            var started = XP2P.startServiceWithXp2pInfo("${App.data.accessInfo!!.productId}/${devName}",
                App.data.accessInfo!!.productId, devName, "")
            if (started != 0) return@Runnable

            var tmpCountDownLatch = CountDownLatch(1)
            countDownLatchs.put("${App.data.accessInfo!!.productId}/${devName}", tmpCountDownLatch)
            tmpCountDownLatch.await()

            val urlPrefix = XP2P.delegateHttpFlv("${App.data.accessInfo!!.productId}/${devName}")
            if (!TextUtils.isEmpty(urlPrefix)) {
                player?.let {
                    val url = urlPrefix + "ipc.flv?action=live"
                    playPlayer(it, url)
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
        player.dataSource = url
        player.prepareAsync()
        player.start()
    }

    override fun setListener() {
        rg_orientation.setOnCheckedChangeListener { group, checkedId ->
            when(checkedId) {
                radio_orientation_h.id -> {
                    switchOrientation(false)
                }
                radio_orientation_v.id -> {
                    switchOrientation(true)
                }
            }
        }
    }

    private fun switchOrientation(orientationV : Boolean) {
        if (orientationV) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
            radio_orientation_v.visibility = View.GONE
            radio_orientation_h.visibility = View.VISIBLE
            gl_video.layoutManager = linearLayoutManager
        } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE)
            radio_orientation_v.visibility = View.VISIBLE
            radio_orientation_h.visibility = View.GONE
            gl_video.layoutManager = gridLayoutManager
        }
        adapter?.notifyDataSetChanged()
    }

    override fun fail(msg: String?, errorCode: Int) {}

    override fun commandRequest(id: String?, msg: String?) {}

    override fun xp2pEventNotify(id: String?, msg: String?, event: Int) {
        if (event == 1003) {
            restartPlayer(id)
        } else if (event == 1004 || event == 1005) {
            countDownLatchs.get(id)?.countDown()
        }
    }

    private fun restartPlayer(id: String?) {
        if (TextUtils.isEmpty(id)) return

        Thread(Runnable {
            var playerHolder : DevUrl2Preview? = null
            for (devUrl in allDevUrl) {
                if (!TextUtils.isEmpty(devUrl.devName) && id!!.endsWith(devUrl.devName)) {
                    playerHolder = devUrl
                    break
                }
            }
            if (playerHolder == null || TextUtils.isEmpty(playerHolder.devName)) return@Runnable

            XP2P.stopService(id)
            var started = XP2P.startServiceWithXp2pInfo(id,
                App.data.accessInfo!!.productId, playerHolder.devName, "")
            if (started != 0) return@Runnable

            var tmpCountDownLatch = CountDownLatch(1)
            countDownLatchs.put(id!!, tmpCountDownLatch)
            tmpCountDownLatch.await()

            val urlPrefix = XP2P.delegateHttpFlv(id)
            if (!TextUtils.isEmpty(urlPrefix)) {
                playerHolder.player?.let {
                    val url = urlPrefix + "ipc.flv?action=live"
                    it.reset()
                    it.dataSource = url
                    it.prepareAsync()
                    it.start()
                }

            }
        }).start()
    }

    override fun avDataRecvHandle(id: String?, data: ByteArray?, len: Int) {}

    override fun avDataCloseHandle(id: String?, msg: String?, errorCode: Int) {}

    override fun onDestroy() {
        super.onDestroy()

        for (devPlayer in allDevUrl) {
            devPlayer.player?.release()
        }

        App.data.accessInfo?.let {
            for (i in 0 until allDevUrl.size) {
                XP2P.stopService("${it.productId}/${allDevUrl.get(i).devName}")
            }
        }

        XP2P.setCallback(null)
        countDownLatchs.clear()
    }
}