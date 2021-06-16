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
import kotlinx.android.synthetic.main.activity_video_multi_preview.*
import kotlinx.coroutines.*
import tv.danmaku.ijk.media.player.IjkMediaPlayer
import java.lang.Runnable
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.CountDownLatch


private var countDownLatchs : MutableMap<String, CountDownLatch> = ConcurrentHashMap()
private var allDevUrl: MutableList<DevUrl2Preview> = CopyOnWriteArrayList()

class VideoMultiPreviewActivity : BaseActivity(), XP2PCallback {
    lateinit var gridLayoutManager : GridLayoutManager
    lateinit var linearLayoutManager : LinearLayoutManager
    private var adapter : DevPreviewAdapter? = null
    private var tag = VideoMultiPreviewActivity::class.simpleName

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
                    surface?.let {
                        allDevUrl.get(i).surface = Surface(surface)
                        player.setSurface(allDevUrl.get(i).surface)
                    }
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
                    keepPlayerplay("${App.data.accessInfo!!.productId}/${devName}")
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
        player.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "reconnect", 1)
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
        Log.d(tag, "id=${id},event=${event}")
        if (event == 1003) {
            var lockHolder = getHolderById(id)
            lockHolder?.lock?.let {
                synchronized(it) {
                    it.notify()
                }
            } // 唤醒守护线程
        } else if (event == 1004 || event == 1005) {
            countDownLatchs.get(id)?.let {
                it.countDown()
            }
        }
    }

    private fun getHolderById(id: String?) : DevUrl2Preview? {
        if (TextUtils.isEmpty(id)) return null

        var playerHolder: DevUrl2Preview? = null
        for (devUrl in allDevUrl) {
            if (!TextUtils.isEmpty(devUrl.devName) && id!!.endsWith(devUrl.devName)) {
                playerHolder = devUrl
                break
            }
        }
        return playerHolder
    }

    private fun keepPlayerplay(id: String?) {
        if (TextUtils.isEmpty(id)) return

        // 开启守护线程
        Thread(Runnable {
            var objectLock = Object()
            while (true) {
                var playerHolder = getHolderById(id)
                if (playerHolder == null || TextUtils.isEmpty(playerHolder.devName)) return@Runnable

                var tmpCountDownLatch = CountDownLatch(1)
                countDownLatchs.put(id!!, tmpCountDownLatch)

                synchronized(playerHolder.lock) {
                    playerHolder.lock.wait()
                }
                if (!playerHolder.keepAliveThreadRuning) break //锁被释放后，检查守护线程是否继续运行

                // 发现断开尝试恢复视频，每隔一秒尝试一次
                var flag = -1
                while (flag != 0) {
                    XP2P.stopService(id)
                    flag = XP2P.startServiceWithXp2pInfo(id, App.data.accessInfo!!.productId, playerHolder.devName, "")
                    synchronized(objectLock) {
                        objectLock.wait(1000)
                    }
                }

                Log.d(tag, "id=${id} keepPlayerplay countDownLatch wait ")
                countDownLatchs.put(id!!, tmpCountDownLatch)
                tmpCountDownLatch.await()
                Log.d(tag, "id=${id} keepPlayerplay countDownLatch passed")

                val urlPrefix = XP2P.delegateHttpFlv(id)
                if (!TextUtils.isEmpty(urlPrefix)) {
                    playerHolder.player?.let {
                        val url = urlPrefix + "ipc.flv?action=live"
                        it.reset()
                        it.setSurface(playerHolder.surface)
                        it.dataSource = url
                        it.prepareAsync()
                        it.start()
                    }
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

        // 关闭所有守护线程
        for (devUrl in allDevUrl) {
            devUrl.keepAliveThreadRuning = false
            devUrl.lock?.let {
                synchronized(it) {
                    it.notify()
                }
            }
        }
    }
}