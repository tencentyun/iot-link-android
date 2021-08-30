package com.tencent.iot.explorer.link.demo.video.playback.localPlayback

import android.graphics.SurfaceTexture
import android.text.TextUtils
import android.util.Log
import android.view.Surface
import android.view.TextureView
import android.view.View
import android.widget.SeekBar
import android.widget.Toast
import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.JSONObject
import com.tencent.iot.explorer.link.core.log.L
import com.tencent.iot.explorer.link.demo.App
import com.tencent.iot.explorer.link.demo.R
import com.tencent.iot.explorer.link.demo.common.customView.CalendarView
import com.tencent.iot.explorer.link.demo.common.customView.timeline.TimeBlockInfo
import com.tencent.iot.explorer.link.demo.common.util.CommonUtils
import com.tencent.iot.explorer.link.demo.core.entity.DevVideoHistory
import com.tencent.iot.explorer.link.demo.video.Command
import com.tencent.iot.explorer.link.demo.video.DevInfo
import com.tencent.iot.explorer.link.demo.video.playback.CalendarDialog
import com.tencent.iot.explorer.link.demo.video.playback.VideoPlaybackBaseFragment
import com.tencent.iot.explorer.link.demo.video.utils.ToastDialog
import com.tencent.xnet.XP2P
import com.tencent.xnet.XP2PCallback
import kotlinx.android.synthetic.main.activity_video_preview.*
import kotlinx.android.synthetic.main.fragment_video_cloud_playback.*
import kotlinx.coroutines.*
import okhttp3.Dispatcher
import tv.danmaku.ijk.media.player.IjkMediaPlayer
import java.lang.Runnable
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArraySet
import java.util.concurrent.CountDownLatch
import kotlin.collections.ArrayList

private var countDownLatchs : MutableMap<String, CountDownLatch> = ConcurrentHashMap()
private var keepConnectThreadLock = Object()
@Volatile
private var keepAliveThreadRuning = true

class VideoLocalPlaybackFragment: VideoPlaybackBaseFragment(), TextureView.SurfaceTextureListener, XP2PCallback, CoroutineScope by MainScope() {
    private var TAG = VideoLocalPlaybackFragment::class.java.simpleName
    var devInfo: DevInfo? = null
    private lateinit var player : IjkMediaPlayer
    private lateinit var surface: Surface
    private var currentPostion = -1L // 小于 0 不需要恢复录像的进度，大于等于 0 需要恢复录像的进度
    private var dateDataSet : MutableSet<String> = CopyOnWriteArraySet()
    private var dlg: CalendarDialog? = null

    override fun startHere(view: View) {
        super.startHere(view)
        tv_date.text = dateFormat.format(System.currentTimeMillis())
        setListener()
        player = IjkMediaPlayer()
        launch(Dispatchers.Main) {
            delay(100)  // 延迟一秒再进行连接，保证存在设备信息
            startConnect()
        }

        App.data.accessInfo?.let {
            XP2P.setQcloudApiCred(it.accessId, it.accessToken)
            XP2P.setCallback(this)
        }

        dlg = CalendarDialog(context, ArrayList(), onMonthChanged)
        dlg?.setOnClickedListener(dlgOnClickedListener)
    }

    private var dlgOnClickedListener = object : CalendarDialog.OnClickedListener {
        override fun onOkClicked(checkedDates: MutableList<String>?) {
            checkedDates?.let {
                if (it.size <= 0) return@let
                tv_date.text = CommonUtils.dateConvertionWithSplit(it[0])
                var parseDate = SimpleDateFormat(CalendarView.SECOND_DATE_FORMAT_PATTERN).parse(tv_date.text.toString())
                launch(Dispatchers.Main) {
                    refreshDateTime(parseDate)
                }
            }
        }

        override fun onOkClickedCheckedDateWithoutData() {
            ToastDialog(context, ToastDialog.Type.WARNING, getString(R.string.checked_date_no_video), 2000).show()
        }

        override fun onOkClickedWithoutDateChecked() {
            ToastDialog(context, ToastDialog.Type.WARNING, getString(R.string.checked_date_first), 2000).show()
        }
    }

    private var onMonthChanged = object: CalendarDialog.OnMonthChanged {
        override fun onMonthChanged(dlg: CalendarDialog, it: String) {
            // 包含对应数据，不再重新获取当前日期的数据
            if (dateDataSet.contains(it)) return

            launch(Dispatchers.Main) {
                var resp = requestTagDateInfo(it)
                L.d(TAG, "resp $resp")
                Log.e("XXX", "1 resp $resp")
                resp = "{\"video_list\":\"1\"}"
                Log.e("XXX", "2 resp $resp")
                if (TextUtils.isEmpty(resp)) return@launch

                try {
                    var respJson = JSONObject.parseObject(resp)
                    if (!respJson.containsKey("video_list")) return@launch

                    // 转换成实际有录像的日期
                    dateDataSet.add(it)
                    var datesStrArr = Integer.toBinaryString(respJson.getInteger("video_list"))
                    var dateParam: MutableList<String> = ArrayList()
                    for (i in datesStrArr.indices) {
                        if (datesStrArr[i].toString() == "1") {
                            var eleDate = it.replace("-", "") + String.format("%02d", i + 1)
                            dateParam.add(eleDate)
                        }
                    }
                    Log.e("XXX", "dateParam ${JSON.toJSONString(dateParam)}")
                    dlg.addTagDates(dateParam)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    override fun getContentView(): Int {
        return R.layout.fragment_video_local_playback
    }

    private fun setListener() {
        playback_control.setOnClickListener {  }
        iv_left_go.setOnClickListener { time_line.last() }
        iv_right_go.setOnClickListener { time_line.next() }
        layout_select_date.setOnClickListener { showCalendarDialog() }
    }

    private fun formateDateParam(dateStr: String): String {
        var timeStr = dateStr
        timeStr = timeStr.replace("-", "")
        if (timeStr.length >= 6) {
            return timeStr.substring(0, 6)
        }
        return ""
    }

    private fun requestTagDateInfo(dateStr: String): String {
        if (App.data.accessInfo == null || devInfo == null ||
            TextUtils.isEmpty(devInfo?.deviceName)) return ""

        var id = "${App.data.accessInfo?.productId}/${devInfo?.deviceName}"
        var timeStr = formateDateParam(dateStr)
        Log.e("XXX", "request timeStr ${timeStr}")
        var command = Command.getMonthDates(devInfo!!.channel, timeStr)
        return XP2P.postCommandRequestSync(id, command.toByteArray(), command.toByteArray().size.toLong(), 2 * 1000 * 1000)
    }

    private fun refreshDateTime(date: Date) {
        if (App.data.accessInfo == null || devInfo == null ||
            TextUtils.isEmpty(devInfo?.deviceName)) return

        var id = "${App.data.accessInfo?.productId}/${devInfo?.deviceName}"
        var command = Command.getDayTimeBlocks(devInfo!!.channel, date)
        var resp = XP2P.postCommandRequestSync(id, command.toByteArray(), command.toByteArray().size.toLong(), 2 * 1000 * 1000)
        resp = "{\"video_list\":[{\"start_time\":\"1626325200\",\"end_time\":\"1626325800\"},{\"start_time\":\"1626326400\",\"end_time\":\"1626327000\"},{\"start_time\":\"1626327000\",\"end_time\":\"1626327600\"},{\"start_time\":\"1626327600\",\"end_time\":\"1626328200\"},{\"start_time\":\"1626328200\",\"end_time\":\"1626328800\"}]}"

        if (TextUtils.isEmpty(resp)) return
        var devVideoHistory = JSONObject.parseObject(resp, DevVideoHistory::class.java)
        devVideoHistory?:let { return@refreshDateTime }
        if (devVideoHistory?.video_list == null || devVideoHistory.video_list!!.size <= 0) return
        var formateDatas = CommonUtils.formatTimeData(devVideoHistory.getTimeBlocks())
        time_line.currentDayTime = Date(formateDatas[0].startTime.time)
        Log.e("XXX", "2 ${formateDatas[0].startTime.time}")
        time_line.setTimeLineTimeDay(Date(formateDatas[0].startTime.time))
        time_line.timeBlockInfos = formateDatas
        time_line.invalidate()
    }

    private fun showCalendarDialog() {
        dateDataSet.clear()
        dlg?.show()
    }

    private var onSeekBarChangeListener = object : SeekBar.OnSeekBarChangeListener {
        override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
            if (fromUser) {  // 是用户操作的，调整视频到指定的时间点
                palayback_video.seekTo(progress * 1000)
                return
            }
        }

        override fun onStartTrackingTouch(seekBar: SeekBar?) {}
        override fun onStopTrackingTouch(seekBar: SeekBar?) {}
    }

    private fun startConnect() {
        if (App.data.accessInfo == null || devInfo == null ||
            TextUtils.isEmpty(devInfo?.deviceName)) return

        Thread(Runnable {
            var id = "${App.data.accessInfo?.productId}/${devInfo?.deviceName}"
            var started = XP2P.startServiceWithXp2pInfo(id,
                App.data.accessInfo?.productId, devInfo?.deviceName, "")
            if (started != 0) {
                launch(Dispatchers.Main) {
                    var errInfo = getString(R.string.error_with_code, id, started.toString())
                    Toast.makeText(context, errInfo, Toast.LENGTH_SHORT).show()
                }
                return@Runnable
            }

            // 启动成功后，开始开启守护线程
            keepConnect(id)
        }).start()
    }

    private fun keepConnect(id: String?) {
        if (TextUtils.isEmpty(id)) return

        // 开启守护线程
        Thread{
            var objectLock = Object()
            while (true) {
                var tmpCountDownLatch = CountDownLatch(1)
                countDownLatchs.put(id!!, tmpCountDownLatch)

                Log.d(TAG, "id=${id} keepConnect wait disconnected msg")
                synchronized(keepConnectThreadLock) {
                    keepConnectThreadLock.wait()
                }
                Log.d(TAG, "id=${id} keepConnect do not wait and keepAliveThreadRuning=${keepAliveThreadRuning}")
                if (!keepAliveThreadRuning) break //锁被释放后，检查守护线程是否继续运行

                // 发现断开尝试恢复视频，每隔一秒尝试一次
                XP2P.stopService(id)
                while (XP2P.startServiceWithXp2pInfo(id, App.data.accessInfo!!.productId, devInfo?.deviceName, "") != 0) {
                    XP2P.stopService(id)
                    synchronized(objectLock) {
                        objectLock.wait(1000)
                    }
                    Log.d(TAG, "id=${id}, try to call startServiceWithXp2pInfo")
                }

                Log.d(TAG, "id=${id}, call startServiceWithXp2pInfo successed")
                countDownLatchs.put(id!!, tmpCountDownLatch)
                Log.d(TAG, "id=${id}, tmpCountDownLatch start wait")
                tmpCountDownLatch.await()
                Log.d(TAG, "id=${id}, tmpCountDownLatch do not wait any more")

                if (currentPostion >= 0) { // 尝试从上次断掉的时间节点开始恢复录像
                    continue // 进入下一次循环
                }
            }
        }.start()
    }

    override fun onSurfaceTextureAvailable(surface: SurfaceTexture?, width: Int, height: Int) {
        surface?.let {
            this.surface = Surface(surface)
            player.setSurface(this.surface)
        }
    }

    override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture?, width: Int, height: Int) {}
    override fun onSurfaceTextureDestroyed(surface: SurfaceTexture?): Boolean { return false }
    override fun onSurfaceTextureUpdated(surface: SurfaceTexture?) {}
    override fun fail(msg: String?, errorCode: Int) {}
    override fun commandRequest(id: String?, msg: String?) {}
    override fun avDataRecvHandle(id: String?, data: ByteArray?, len: Int) {}
    override fun avDataCloseHandle(id: String?, msg: String?, errorCode: Int) {}

    override fun xp2pEventNotify(id: String?, msg: String?, event: Int) {
        Log.e(TAG, "id=${id}, event=${event}")
        if (event == 1003) {
            launch (Dispatchers.Main) {
                Toast.makeText(context, getString(R.string.error_with_code, id, msg), Toast.LENGTH_SHORT).show()
            }

        } else if (event == 1004 || event == 1005) {
            launch (Dispatchers.Main) {
                Toast.makeText(context, getString(R.string.connected, id), Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        player?.release()

        App.data.accessInfo?.let { access ->
            devInfo?.let {
                XP2P.stopService("${access.productId}/${it.deviceName}")
            }
        }
        XP2P.setCallback(null)

        countDownLatchs.clear()
        // 关闭守护线程
        keepAliveThreadRuning = false
        keepConnectThreadLock?.let {
            synchronized(it) {
                it.notify()
            }
        }
        cancel()
    }
}