package com.tencent.iot.explorer.link.demo.video.preview

import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.res.Configuration
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
import com.tencent.iot.explorer.link.demo.BaseActivity
import com.tencent.iot.explorer.link.demo.R
import com.tencent.iot.explorer.link.demo.VideoBaseActivity
import com.tencent.iot.explorer.link.demo.core.activity.*
import com.tencent.iot.explorer.link.demo.video.Command
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
import kotlin.collections.ArrayList


private var countDownLatchs : MutableMap<String, CountDownLatch> = ConcurrentHashMap()
private var allDevUrl: MutableList<DevUrl2Preview> = CopyOnWriteArrayList()

class VideoMultiPreviewActivity : VideoBaseActivity(), XP2PCallback, CoroutineScope by MainScope() {
    lateinit var gridLayoutManager : GridLayoutManager
    lateinit var linearLayoutManager : LinearLayoutManager
    private var adapter : DevPreviewAdapter? = null
    private var tag = VideoMultiPreviewActivity::class.simpleName
    private var orientation = true

    override fun getContentView(): Int {
        return R.layout.activity_video_multi_preview
    }

    override fun onResume() {
        super.onResume()
        adapter = DevPreviewAdapter(this@VideoMultiPreviewActivity, allDevUrl)
        gl_video.layoutManager = linearLayoutManager
        gl_video.adapter = adapter
        switchOrientation(orientation)
        playAll()
    }

    override fun initView() {
        App.data.accessInfo?.let {
            XP2P.setQcloudApiCred(it.accessId, it.accessToken)
            XP2P.setCallback(this)
        }

        gridLayoutManager = GridLayoutManager(this@VideoMultiPreviewActivity, 2)
        linearLayoutManager = LinearLayoutManager(this@VideoMultiPreviewActivity)
        intent.getBundleExtra(VideoConst.VIDEO_URLS)?.let {
            it.getString(VideoConst.VIDEO_URLS)?.let {
                try {
                    allDevUrl = JSONArray.parseArray(it, DevUrl2Preview::class.java)
                    var column = 2
                    if (allDevUrl.size <= 1) column = 1  // 当只有一个元素的时候，网格只有一列
                    gridLayoutManager = GridLayoutManager(this@VideoMultiPreviewActivity, column)
                    linearLayoutManager = LinearLayoutManager(this@VideoMultiPreviewActivity)
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
            if (allDevUrl[i].Status != 1) continue

            var player = IjkMediaPlayer()
            allDevUrl[i].surfaceTextureListener = object : TextureView.SurfaceTextureListener {
                override fun onSurfaceTextureAvailable(surface: SurfaceTexture?, width: Int, height: Int) {
                    surface?.let {
                        allDevUrl[i].surface = Surface(surface)
                        player.setSurface(allDevUrl[i].surface)
                    }
                }

                override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture?, width: Int, height: Int) {}
                override fun onSurfaceTextureDestroyed(surface: SurfaceTexture?): Boolean { return false }
                override fun onSurfaceTextureUpdated(surface: SurfaceTexture?) {}
            }

            setPlayerSource(player, allDevUrl[i].devName, allDevUrl[i].channel)
            allDevUrl.get(i).player = player
        }
    }

    private fun setPlayerSource(player: IjkMediaPlayer, devName: String, channel: Int) {
        Thread(Runnable {
            var id = "${App.data.accessInfo!!.productId}/${devName}"
            var started = XP2P.startServiceWithXp2pInfo(this@VideoMultiPreviewActivity, id,
                App.data.accessInfo!!.productId, devName, "")
            // 已经启动过，或者第一次启动，继续进行
            if (started != 0 && started != -1011) {
                launch(Dispatchers.Main) {
                    var errInfo = getString(R.string.error_with_code, id, started.toString())
                    Toast.makeText(this@VideoMultiPreviewActivity, errInfo, Toast.LENGTH_SHORT).show()
                }
                return@Runnable
            }

            var tmpCountDownLatch = CountDownLatch(1)
            countDownLatchs.put("${App.data.accessInfo!!.productId}/${devName}/${channel}", tmpCountDownLatch)
            tmpCountDownLatch.await()

            val urlPrefix = XP2P.delegateHttpFlv("${App.data.accessInfo!!.productId}/${devName}")
            if (!TextUtils.isEmpty(urlPrefix)) {
                player?.let {
                    val url = urlPrefix + Command.getVideoHightQualityUrlSuffix(channel)
                    playPlayer(it, url)
                    keepPlayerplay("${App.data.accessInfo!!.productId}/${devName}", channel)
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
        player.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec",1)
        player.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec-auto-rotate", 1)
        player.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec-handle-resolution-change", 1)

        player.dataSource = url
        player.prepareAsync()
        player.start()
    }

    override fun setListener() {
        rg_orientation.setOnCheckedChangeListener { group, checkedId ->
            when(checkedId) {
                radio_orientation_h.id -> switchOrientation(false)
                radio_orientation_v.id -> switchOrientation(true)
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
        orientation = orientationV
        adapter?.notifyDataSetChanged()
    }

    override fun fail(msg: String?, errorCode: Int) {}
    override fun commandRequest(id: String?, msg: String?) {}

    override fun xp2pEventNotify(id: String?, msg: String?, event: Int) {
        Log.d(tag, "id=${id},event=${event},msg=${msg}")
        if (event == 1003) {
            XP2P.stopService(id)
            var holders = getHolderById(id)
            for (i in 0 until holders.size) {
                holders.get(i).lock?.let {
                    synchronized(it) {
                        it.notify()
                    }
                } // 唤醒守护线程
            }
            launch(Dispatchers.Main) {
                var content = getString(R.string.disconnected_and_reconnecting, id)
                Toast.makeText(this@VideoMultiPreviewActivity, content, Toast.LENGTH_SHORT).show()
            }

        } else if (event == 1004) {
            launch(Dispatchers.Main) {
                var content = getString(R.string.connected, id)
                Toast.makeText(this@VideoMultiPreviewActivity, content, Toast.LENGTH_SHORT).show()
            }

            tryReleaseLock(id)
        } else if (event == 1005) { }
    }

    private fun tryReleaseLock(id: String?) {
        var holders = getHolderById(id)
        for (i in 0 until holders.size) {
            Thread(Runnable {
                Timer().schedule(object : TimerTask() {
                    override fun run() {
                        App.data.accessInfo?.let {
                            var command = Command.getNvrIpcStatus(holders.get(i).channel, 0)
                            var repStatus = XP2P.postCommandRequestSync("${it.productId}/${holders.get(i).devName}",
                                command.toByteArray(), command.toByteArray().size.toLong(), 2 * 1000 * 1000)
                            JSONArray.parseArray(repStatus, DevStatus::class.java)?.let {
                                if (it.size == 1 && it.get(0).status == 0) {
                                    countDownLatchs.get("${id}/${holders.get(i).channel}")?.let {
                                        it.countDown()
                                    }
                                }
                            }
                        }
                    }

                }, 500)
            }).start()
        }
    }

    private fun getHolderById(id: String?) : MutableList<DevUrl2Preview> {
        if (TextUtils.isEmpty(id)) return ArrayList()

        var ret = ArrayList<DevUrl2Preview>()
        for (devUrl in allDevUrl) {
            if (!TextUtils.isEmpty(devUrl.devName) && id!!.endsWith(devUrl.devName)) {
                ret.add(devUrl)
            }
        }
        return ret
    }

    private fun getHolderById(id: String?, channel: Int) : DevUrl2Preview? {
        if (TextUtils.isEmpty(id)) return null

        var ret: DevUrl2Preview? = null
        for (devUrl in allDevUrl) {
            if (!TextUtils.isEmpty(devUrl.devName) &&
                id!!.endsWith(devUrl.devName) &&
                channel == devUrl.channel) {
                ret = devUrl
            }
        }
        return ret
    }

    private fun keepPlayerplay(id: String?, channel: Int) {
        if (TextUtils.isEmpty(id)) return

        // 开启守护线程
        Thread(Runnable {
            var objectLock = Object()
            while (true) {
                var playerHolder = getHolderById(id, channel)
                if (playerHolder == null || TextUtils.isEmpty(playerHolder.devName)) return@Runnable

                var tmpCountDownLatch = CountDownLatch(1)

                Log.d(tag, "index=${id!!}/${channel} lock wait ")
                synchronized(playerHolder.lock) {
                    playerHolder.lock.wait()
                }
                Log.d(tag, "index=${id!!}/${channel} lock passed ")
                if (!playerHolder.keepAliveThreadRuning) break //锁被释放后，检查守护线程是否继续运行

                // 发现断开尝试恢复视频，每隔一秒尝试一次
                var flag = XP2P.startServiceWithXp2pInfo(this@VideoMultiPreviewActivity, id, App.data.accessInfo!!.productId, playerHolder.devName, "")
                while (flag != 0 && flag != -1011) {
                    // XP2P.stopService(id)
                    synchronized(objectLock) {
                        objectLock.wait(1000)
                    }
                    flag = XP2P.startServiceWithXp2pInfo(this@VideoMultiPreviewActivity, id, App.data.accessInfo!!.productId, playerHolder.devName, "")
                }

                Log.d(tag, "index=${id!!}/${channel} keepPlayerplay countDownLatch wait ")
                countDownLatchs.put("${id!!}/${channel}", tmpCountDownLatch)
                tmpCountDownLatch.await()
                Log.d(tag, "index=${id!!}/${channel} keepPlayerplay countDownLatch passed")

                val urlPrefix = XP2P.delegateHttpFlv(id)
                if (!TextUtils.isEmpty(urlPrefix)) {
                    playerHolder.player?.let {
                        val url = urlPrefix + Command.getVideoHightQualityUrlSuffix(playerHolder.channel)
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
    override fun onDeviceMsgArrived(id: String?, data: ByteArray?, len: Int): String {
        return "app reply to device"
    }

    override fun onPause() {
        super.onPause()
        finishAll()
    }

    private fun finishAll() {
        for (devPlayer in allDevUrl) {
            devPlayer.player?.release()
        }

        App.data.accessInfo?.let {
            for (i in 0 until allDevUrl.size) {
                XP2P.stopService("${it.productId}/${allDevUrl.get(i).devName}")
            }
        }

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

    override fun onDestroy() {
        super.onDestroy()
        finishAll()
        XP2P.setCallback(null)
        cancel()
    }

    companion object {
        fun startMultiPreviewActivity(context: Context?, allUrl: ArrayList<DevUrl2Preview>) {
            if (context == null) return

            var intent = Intent(context, VideoMultiPreviewActivity::class.java)
            var bundle = Bundle()
            intent.putExtra(VideoConst.VIDEO_URLS, bundle)
            bundle.putString(VideoConst.VIDEO_URLS, JSON.toJSONString(allUrl))
            context?.startActivity(intent)
        }
    }
}