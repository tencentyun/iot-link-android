package com.tencent.iot.explorer.link.demo.video.playback.cloudPlayback

import android.graphics.SurfaceTexture
import android.text.TextUtils
import android.util.Log
import android.view.Surface
import android.view.TextureView
import android.view.View
import android.widget.SeekBar
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.LinearLayoutManager
import com.alibaba.fastjson.JSONArray
import com.alibaba.fastjson.JSONObject
import com.tencent.iot.explorer.link.demo.App
import com.tencent.iot.explorer.link.demo.R
import com.tencent.iot.explorer.link.demo.common.customView.CalendarView
import com.tencent.iot.explorer.link.demo.common.customView.timeline.TimeLineView
import com.tencent.iot.explorer.link.demo.common.customView.timeline.TimeLineViewChangeListener
import com.tencent.iot.explorer.link.demo.common.util.CommonUtils
import com.tencent.iot.explorer.link.demo.core.entity.BaseResponse
import com.tencent.iot.explorer.link.demo.core.entity.VideoHistory
import com.tencent.iot.explorer.link.demo.video.DevInfo
import com.tencent.iot.explorer.link.demo.video.playback.CalendarDialog
import com.tencent.iot.explorer.link.demo.video.playback.CalendarDialog.OnClickedListener
import com.tencent.iot.explorer.link.demo.video.playback.VideoPlaybackBaseFragment
import com.tencent.iot.explorer.link.demo.video.playback.cloudPlayback.event.ActionListAdapter
import com.tencent.iot.explorer.link.demo.video.playback.cloudPlayback.event.ActionRecord
import com.tencent.iot.explorer.link.demo.video.playback.cloudPlayback.event.EventPresenter
import com.tencent.iot.explorer.link.demo.video.playback.cloudPlayback.event.EventView
import com.tencent.iot.explorer.link.demo.video.utils.ToastDialog
import com.tencent.iot.video.link.callback.VideoCallback
import com.tencent.iot.video.link.consts.VideoRequestCode
import com.tencent.iot.video.link.service.VideoBaseService
import kotlinx.android.synthetic.main.activity_video_preview.*
import kotlinx.android.synthetic.main.fragment_video_cloud_playback.*
import kotlinx.android.synthetic.main.fragment_video_cloud_playback.iv_left_go
import kotlinx.android.synthetic.main.fragment_video_cloud_playback.iv_right_go
import kotlinx.android.synthetic.main.fragment_video_cloud_playback.iv_start
import kotlinx.android.synthetic.main.fragment_video_cloud_playback.layout_select_date
import kotlinx.android.synthetic.main.fragment_video_cloud_playback.pause_tip_layout
import kotlinx.android.synthetic.main.fragment_video_cloud_playback.playback_control
import kotlinx.android.synthetic.main.fragment_video_cloud_playback.time_line
import kotlinx.android.synthetic.main.fragment_video_cloud_playback.tv_all_time
import kotlinx.android.synthetic.main.fragment_video_cloud_playback.tv_current_pos
import kotlinx.android.synthetic.main.fragment_video_cloud_playback.tv_date
import kotlinx.android.synthetic.main.fragment_video_cloud_playback.video_seekbar
import kotlinx.android.synthetic.main.fragment_video_local_playback.*
import kotlinx.coroutines.*
import tv.danmaku.ijk.media.player.IMediaPlayer
import tv.danmaku.ijk.media.player.IjkMediaPlayer
import java.net.URLDecoder
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class VideoCloudPlaybackFragment: VideoPlaybackBaseFragment(), TextureView.SurfaceTextureListener, EventView, VideoCallback, CoroutineScope by MainScope() {
    var devInfo: DevInfo? = null
    private var baseUrl = ""
    private var adapter : ActionListAdapter? = null
    private var records : MutableList<ActionRecord> = ArrayList()
    private lateinit var presenter: EventPresenter
    private var URL_FORMAT = "%s?starttime_epoch=%s&endtime_epoch=%s"
    private var seekBarJob : Job? = null
    @Volatile
    private var isShowing = false
    private lateinit var surface: Surface
    private var player : IjkMediaPlayer = IjkMediaPlayer()
    @Volatile
    private var updateSeekBarAble = true  // 手动拖拽过程的标记

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        player?.let {
            if (!isVisibleToUser && it.isPlaying) {
                // 滑动该页面时，如果处于播放状态，暂停播放
                iv_start.performClick()
            }
        }
        isShowing = isVisibleToUser
    }

    override fun startHere(view: View) {
        super.startHere(view)
        presenter = EventPresenter(this)

        tv_date.setText(dateFormat.format(System.currentTimeMillis()))
        refreshTimeLine(dateFormat.format(System.currentTimeMillis()))
        setListener()
        var linearLayoutManager = LinearLayoutManager(context)
        adapter = ActionListAdapter(context, records)
        action_list_view.layoutManager = linearLayoutManager
        action_list_view.adapter = adapter
        playback_control.visibility = View.GONE
        adapter?.setOnItemClicked(onItemClicked)

        // 获取当前时间节点以前的事件云内容
        App.data.accessInfo?.let {
            if (devInfo == null) return@let
            presenter.setAccessId(it.accessId)
            presenter.setAccessToken(it.accessToken)
            presenter.setProductId(it.productId)
            presenter.setDeviceName(devInfo!!.deviceName)
            try2GetRecord(Date())
        }
        palayback_video.surfaceTextureListener = this
    }

    private fun try2GetRecord(date: Date) {
        records.clear()
        App.data.accessInfo?.let {
            presenter.getEventsData(date)
            tv_status.visibility = View.VISIBLE
            tv_status.setText(R.string.loading)
        }
    }

    private fun try2GetVideoDateData() {
        App.data.accessInfo?.let { accessInfo ->
            devInfo?.let {
                VideoBaseService(accessInfo.accessId, accessInfo.accessToken).getIPCDateData(accessInfo.productId, it.deviceName, this)
            }
        }
    }

    private fun refreshTimeLine(timeStr: String?) {
        if (TextUtils.isEmpty(timeStr)) return
        time_line.currentDayTime = SimpleDateFormat(CalendarView.SECOND_DATE_FORMAT_PATTERN).parse(timeStr)?: return

        App.data.accessInfo?.let {
            if (devInfo == null) return@let
            VideoBaseService(it.accessId, it.accessToken).getIPCTimeData(it.productId, devInfo!!.deviceName, timeStr!!, this)
        }
    }

    private fun showCalendarDialog(dataList: MutableList<String>) {
        var dlg = CalendarDialog(context, dataList)
        dlg.show()
        dlg.setOnClickedListener(object : OnClickedListener {
            override fun onOkClicked(checkedDates: MutableList<String>?) {
                checkedDates?.let {
                    if (it.size <= 0) return@let

                    tv_date.setText(CommonUtils.dateConvertionWithSplit(it.get(0))) // 当前列表有数据时，有且仅有一个元素，所以可以直接去第一个位置的元素
                    refreshTimeLine(CommonUtils.dateConvertionWithSplit(it.get(0)))

                    var selectDate = SimpleDateFormat(CalendarView.SECOND_DATE_FORMAT_PATTERN).parse(
                        CommonUtils.dateConvertionWithSplit(it.get(0)))
                    try2GetRecord(selectDate)
                    adapter?.let {
                        it.index = -1
                    }
                }
            }

            override fun onOkClickedCheckedDateWithoutData() {
                ToastDialog(context, ToastDialog.Type.WARNING, getString(R.string.checked_date_no_video), 2000).show()
            }

            override fun onOkClickedWithoutDateChecked() {
                ToastDialog(context, ToastDialog.Type.WARNING, getString(R.string.checked_date_first), 2000).show()
            }
        })
    }

    private fun setListener() {
        layout_select_date.setOnClickListener { try2GetVideoDateData() }
        iv_left_go.setOnClickListener { time_line.last() }
        iv_right_go.setOnClickListener { time_line.next() }
        time_line.setTimelineChangeListener(timeLineViewChangeListener)
        playback_control.setOnClickListener {  }
        video_seekbar.setOnSeekBarChangeListener(onSeekBarChangeListener)
        pause_tip_layout.setOnClickListener { iv_start.performClick() }

        iv_start.setOnClickListener {
            if (player.isPlaying) {
                player.pause()
                iv_start.setImageResource(R.mipmap.start)
                pause_tip_layout.visibility = View.VISIBLE
                seekBarJob?.cancel()
            } else {
                player.start()
                iv_start.setImageResource(R.mipmap.stop)
                pause_tip_layout.visibility = View.GONE
                startJobRereshTimeAndProgress()
            }
        }
    }

    private var onCompletionListener = object: IMediaPlayer.OnCompletionListener {
        override fun onCompletion(mp: IMediaPlayer?) {
            iv_start?.setImageResource(R.mipmap.start)
            pause_tip_layout?.visibility = View.VISIBLE
            seekBarJob?.cancel()
            video_seekbar.progress = video_seekbar.max
            player.seekTo(1)
        }
    }

    private var onItemClicked = object : ActionListAdapter.OnItemClicked {
        override fun onItemVideoClicked(pos: Int) {
            adapter?.let {
                it.index = pos
                it.notifyDataSetChanged()

                var endtime = ""
                if (it.list.get(pos).endTime != 0L) {
                    endtime = (it.list.get(pos).endTime).toString()
                } else {
                    endtime = (System.currentTimeMillis() / 1000).toString()
                }
                var url = String.format(URL_FORMAT, presenter.getBaseUrl(),
                    (it.list.get(pos).startTime).toString(),
                    endtime)
                playVideo(url, 0)
            }
        }
    }

    private var onErrorListener = IMediaPlayer.OnErrorListener { mp, what, extra ->
        video_seekbar.progress = 0
        video_seekbar.max = 0
        tv_current_pos.text = "00:00:00"
        tv_all_time.text = "00:00:00"
        iv_start.setImageResource(R.mipmap.start)
        pause_tip_layout.visibility = View.GONE
        Toast.makeText(context, getString(R.string.no_data), Toast.LENGTH_SHORT).show()
        iv_start.isClickable = false
        true
    }

    private var onSeekBarChangeListener = object : SeekBar.OnSeekBarChangeListener {
        override fun onStartTrackingTouch(seekBar: SeekBar?) {
            updateSeekBarAble = false
        }
        override fun onStopTrackingTouch(seekBar: SeekBar?) {
            seekBar?.let {
                player.seekTo(it.progress.toLong() * 1000)
            }
            updateSeekBarAble = true
        }
        override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {}
    }

    private var timeLineViewChangeListener = object : TimeLineViewChangeListener {
        override fun onChange(date: Date?, timeLineView: TimeLineView?) {
            if (timeLineView == null) return
            date?.let {
                for (j in 0 until timeLineView!!.timeBlockInfos.size) {
                    var blockTime = timeLineView!!.timeBlockInfos!!.get(j)
                    if (blockTime.startTime.time <= date.time && (blockTime.endTime.time >= date.time || blockTime.endTime.time == 0L)) {
                        var endtime = ""
                        if (blockTime.endTime.time != 0L) {
                            endtime = (blockTime.endTime.time / 1000).toString()
                        } else {
                            endtime = (System.currentTimeMillis() / 1000).toString()
                        }
                        var url = String.format(URL_FORMAT, baseUrl,
                            (blockTime.startTime.time / 1000).toString(),
                            endtime)

                        var offest = date.time - blockTime.startTime.time
                        playVideo(url, offest)
                        return@onChange
                    }
                }
            }
        }
    }

    private fun playVideo(url: String, offset: Long) {
        App.data.accessInfo?.let {
            var expireDate = Date((Date().time + 60 * 60 * 1000))
            var time = expireDate.time / 1000
            VideoBaseService(it.accessId, it.accessToken).getVideoUrl(url, time, object : VideoCallback{
                override fun fail(msg: String?, reqCode: Int) {
                    ToastDialog(context, ToastDialog.Type.WARNING, msg ?: "", 2000).show()
                }

                override fun success(response: String?, reqCode: Int) {
                    var json = JSONObject.parseObject(response)
                    json?.let {
                        it.getJSONObject("Response")?.let {
                            var eventResp = JSONObject.parseObject(it.toJSONString(), SignedUrlResponse::class.java)
                            eventResp?.let {
                                var url2Play = URLDecoder.decode(it.signedVideoURL)
                                startVideo(url2Play, offset)
                            }
                        }
                    }
                }
            })
        }
    }

    private fun startVideo(url: String, offset: Long) {
        if (TextUtils.isEmpty(url)) return
        player.reset()
        player.setSurface(this.surface)
        player.dataSource = url
        player.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "enable-accurate-seek", 1)
        player.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "fflags", "fastseek")
        player.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "enable-accurate-seek", 1)
        player.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "probesize", 1024 * 10)
        player.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec",1)
        player.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec-auto-rotate", 1)
        player.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec-handle-resolution-change", 1)
        seekBarJob?.cancel()
        player.prepareAsync()
        player.setOnErrorListener(onErrorListener)
        player.setOnInfoListener(onInfoListener)
        player.setOnCompletionListener(onCompletionListener)
        player.setOnPreparedListener {
            var realOffset = offset
            if (realOffset >= it.duration) {
                realOffset = it.duration
            }
            tv_current_pos.text = CommonUtils.formatTime(realOffset)
            tv_all_time.text = CommonUtils.formatTime(it.duration)
            video_seekbar.max = (it.duration / 1000).toInt()
            startJobRereshTimeAndProgress()
            iv_start.isClickable = true
            it.start()
            it.seekTo(realOffset)
            launch(Dispatchers.Main) {
                delay(10)
                if (!isShowing) {
                    iv_start.performClick()
                }
            }
        }
    }

    private fun startJobRereshTimeAndProgress() {
        seekBarJob = launch(Dispatchers.Main) {
            while (isActive) {
                delay(1000)
                tv_current_pos.text = CommonUtils.formatTime(player.currentPosition)
                if (updateSeekBarAble) {  //非拖拽中可以自动刷新进度
                    video_seekbar.progress = (player.currentPosition / 1000).toInt()
                }
            }
        }
    }

    private var onInfoListener = object : IMediaPlayer.OnInfoListener {
        override fun onInfo(mp: IMediaPlayer?, what: Int, extra: Int): Boolean {
            mp?.let {
                if (it.isPlaying) {
                    iv_start?.setImageResource(R.mipmap.stop)
                    pause_tip_layout?.visibility = View.GONE
                    return true
                }
            }
            iv_start?.setImageResource(R.mipmap.start)
            pause_tip_layout?.visibility = View.VISIBLE
            return true
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        cancel()
    }

    override fun eventReady(events: MutableList<ActionRecord>) {
        if (events == null || events.size <= 0) {
            launch(Dispatchers.Main) {
                tv_status.setText(R.string.no_data)
            }
            return
        }

        launch(Dispatchers.Main) {
            tv_status.visibility = View.GONE
            records.addAll(events)
            adapter?.notifyDataSetChanged()
        }
    }

    override fun fail(msg: String?, reqCode: Int) {
        ToastDialog(context, ToastDialog.Type.WARNING, msg ?: "", 2000).show()
    }

    override fun success(response: String?, reqCode: Int) {
        var resp = JSONObject.parseObject(response, BaseResponse::class.java)
        if (resp == null || resp.Response == null) return

        when (reqCode) {
            VideoRequestCode.video_describe_date_time -> {
                var history = JSONObject.parseObject(resp.Response?.data, VideoHistory::class.java)
                history?.let { videoHistory ->
                    baseUrl = videoHistory.VideoURL
                    videoHistory.TimeList?.let {
                        if (it.size <= 0) return@let

                        var dataList = CommonUtils.formatTimeData(it)
                        time_line.setTimeLineTimeDay(Date(dataList.get(0).startTime.time))
                        time_line.timeBlockInfos = dataList
                        time_line.invalidate()

                        var endtime = ""
                        if (dataList.get(0).endTime.time != 0L) {
                            endtime = (dataList.get(0).endTime.time / 1000).toString()
                        } else {
                            endtime = (System.currentTimeMillis() / 1000).toString()
                        }
                        var url = String.format(URL_FORMAT, baseUrl,
                            (dataList.get(0).startTime.time / 1000).toString(),
                            endtime)
                        playVideo(url, 0)
                    }
                }
            }

            VideoRequestCode.video_describe_date -> {
                var dataList = JSONArray.parseArray(resp.Response?.data, String::class.java)
                dataList?.let {
                    if (it.size == 0) return@let

                    for (i in 0 until it.size) {
                        it[i] = it.get(i).replace("-", "")
                    }
                    showCalendarDialog(it)
                    return
                }

                ToastDialog(context, ToastDialog.Type.WARNING, getString(R.string.no_data), 2000).show()
            }
        }
    }

    override fun videoViewNeeResize(marginStart: Int, marginEnd: Int) {
        var videoLayoutParams = palayback_video.layoutParams as ConstraintLayout.LayoutParams
        videoLayoutParams.marginStart = marginStart
        videoLayoutParams.marginEnd = marginEnd
        palayback_video.layoutParams = videoLayoutParams
    }

    override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture?, width: Int, height: Int) {}
    override fun onSurfaceTextureDestroyed(surface: SurfaceTexture?): Boolean { return false }
    override fun onSurfaceTextureUpdated(surface: SurfaceTexture?) {}
    override fun onSurfaceTextureAvailable(surface: SurfaceTexture?, width: Int, height: Int) {
        surface?.let {
            this.surface = Surface(surface)
            player.setSurface(this.surface)
        }
    }

    override fun onPause() {
        super.onPause()

        player?.let {
            if (it.isPlaying) {
                iv_start.performClick()
            }
        }
    }
}