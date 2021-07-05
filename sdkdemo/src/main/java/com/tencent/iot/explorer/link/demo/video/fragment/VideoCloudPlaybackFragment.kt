package com.tencent.iot.explorer.link.demo.video.fragment

import android.media.MediaPlayer
import android.net.Uri
import android.text.TextUtils
import android.view.View
import android.widget.SeekBar
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.alibaba.fastjson.JSONArray
import com.alibaba.fastjson.JSONObject
import com.tencent.iot.explorer.link.demo.App
import com.tencent.iot.explorer.link.core.demo.R
import com.tencent.iot.explorer.link.demo.core.entity.BaseResponse
import com.tencent.iot.explorer.link.demo.core.entity.VideoHistory
import com.tencent.iot.explorer.link.demo.video.response.SignedUrlResponse
import com.tencent.iot.explorer.link.demo.video.adapter.ActionListAdapter
import com.tencent.iot.explorer.link.demo.video.dialog.CalendarDialog
import com.tencent.iot.explorer.link.demo.video.dialog.CalendarDialog.OnClickedListener
import com.tencent.iot.explorer.link.demo.video.dialog.ToastDialog
import com.tencent.iot.explorer.link.demo.video.entity.ActionRecord
import com.tencent.iot.explorer.link.demo.video.entity.DevInfo
import com.tencent.iot.explorer.link.demo.video.mvp.presenter.EventPresenter
import com.tencent.iot.explorer.link.demo.video.mvp.view.EventView
import com.tencent.iot.explorer.link.demo.common.util.CommonUtils
import com.tencent.iot.explorer.link.demo.common.customView.CalendarView
import com.tencent.iot.explorer.link.demo.common.customView.timeline.TimeLineView
import com.tencent.iot.explorer.link.demo.common.customView.timeline.TimeLineViewChangeListener
import com.tencent.iot.video.link.callback.VideoCallback
import com.tencent.iot.video.link.consts.VideoRequestCode
import com.tencent.iot.video.link.service.VideoBaseService
import kotlinx.android.synthetic.main.activity_video_preview.*
import kotlinx.android.synthetic.main.fragment_video_cloud_playback.*
import kotlinx.coroutines.*
import java.net.URLDecoder
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class VideoCloudPlaybackFragment: VideoPlaybackBaseFragment(), EventView, VideoCallback {
    var devInfo: DevInfo? = null
    private var baseUrl = ""
    private var adapter : ActionListAdapter? = null
    private var records : MutableList<ActionRecord> = ArrayList()
    private lateinit var presenter: EventPresenter
    private var URL_FORMAT = "%s?starttime_epoch=%s&endtime_epoch=%s"
    private var seekBarJob : Job? = null

    override fun startHere(view: View) {
        super.startHere(view)
        presenter = EventPresenter(this)

        tv_date.setText(dateFormat.format(System.currentTimeMillis()))
        setListener()
        var linearLayoutManager = LinearLayoutManager(context)
        adapter = ActionListAdapter(context, records)
        action_list_view.layoutManager = linearLayoutManager
        action_list_view.adapter = adapter
        playback_control.visibility = View.GONE
        adapter?.setOnItemClicked(onItemClicked)

        // 获取当前时间节点以前的事件云内容
        records.clear()
        App.data.accessInfo?.let {
            if (devInfo == null) return@let
            presenter.setAccessId(it.accessId)
            presenter.setAccessToken(it.accessToken)
            presenter.setProductId(it.productId)
            presenter.setDeviceName(devInfo!!.deviceName)
            presenter.getCurrentDayEventsData()
        }
    }

    private fun try2GetRecord(date: Date) {
        records.clear()
        App.data.accessInfo?.let {
            presenter.getEventsData(date)
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
        palayback_video.setOnInfoListener(onInfoListener)
        video_seekbar.setOnSeekBarChangeListener(onSeekBarChangeListener)
        palayback_video.setOnErrorListener(onErrorListener)

        palayback_video.setOnCompletionListener {
            iv_start.setImageResource(R.mipmap.start)
            pause_tip_layout.visibility = View.VISIBLE
        }

        iv_start.setOnClickListener {
            if (palayback_video.isPlaying) {
                palayback_video.pause()
                iv_start.setImageResource(R.mipmap.start)
                pause_tip_layout.visibility = View.VISIBLE
                seekBarJob?.cancel()
            } else {
                palayback_video.start()
                iv_start.setImageResource(R.mipmap.stop)
                pause_tip_layout.visibility = View.GONE
                startJobRereshTimeAndProgress()
            }
        }
    }

    private var onItemClicked = object : ActionListAdapter.OnItemClicked {
        override fun onItemVideoClicked(pos: Int) {
            adapter?.let {
                it.index = pos
                it.notifyDataSetChanged()

                var url = String.format(URL_FORMAT, baseUrl,
                    (it.list.get(pos).startTime).toString(),
                    (it.list.get(pos).endTime).toString())
                playVideo(url, 0)
            }
        }
    }

    private var onErrorListener = object : MediaPlayer.OnErrorListener {
        override fun onError(mp: MediaPlayer?, what: Int, extra: Int): Boolean {
            video_seekbar.progress = 0
            video_seekbar.max = 0
            tv_current_pos.setText("00:00:00")
            tv_all_time.setText("00:00:00")
            iv_start.setImageResource(R.mipmap.start)
            pause_tip_layout.visibility = View.GONE
            Toast.makeText(context, getString(R.string.no_data), Toast.LENGTH_SHORT).show()
            iv_start.isClickable = false
            return true
        }
    }

    private var onSeekBarChangeListener = object : SeekBar.OnSeekBarChangeListener {
        override fun onStartTrackingTouch(seekBar: SeekBar?) {}
        override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
            if (fromUser) {  // 是用户操作的，调整视频到指定的时间点
                palayback_video.seekTo(progress * 1000)
                return
            }
        }
    }

    private var timeLineViewChangeListener = object : TimeLineViewChangeListener {
        override fun onChange(date: Date?, timeLineView: TimeLineView?) {
            if (timeLineView == null) return
            date?.let {
                for (j in 0 until timeLineView!!.timeBlockInfos.size) {
                    var blockTime = timeLineView!!.timeBlockInfos!!.get(j)
                    if (blockTime.startTime.time <= date.time && blockTime.endTime.time >= date.time) {
                        var url = String.format(URL_FORMAT, baseUrl,
                            (blockTime.startTime.time / 1000).toString(),
                            (blockTime.endTime.time / 1000).toString())

                        var offest = date.time - blockTime.startTime.time
                        playVideo(url, offest)
                        return@onChange
                    }
                }

                // 如果对应时间段没有视频内容
                palayback_video.setVideoURI(Uri.parse(""))
            }
        }
    }

    private fun playVideo(url: String, offset: Long) {
        App.data.accessInfo?.let {
            var expireDate = Date((Date().time + 60 * 60 * 1000))
            var time = expireDate.time / 1000
            VideoBaseService(it.accessId, it.accessToken).getVideoUrl(url, time, object : VideoCallback{
                override fun fail(msg: String?, reqCode: Int) {
                    ToastDialog(context, ToastDialog.Type.WARNING, msg?:"", 2000).show()
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
        palayback_video.setVideoURI(Uri.parse(url))
        seekBarJob?.cancel()
        palayback_video.setOnPreparedListener {
            var realOffset = offset
            if (realOffset >= it.duration) {
                realOffset = it.duration.toLong()
            }
            tv_current_pos.setText(CommonUtils.formatTime(realOffset))
            tv_all_time.setText(CommonUtils.formatTime(it.duration.toLong()))
            video_seekbar.max = it.duration / 1000
            startJobRereshTimeAndProgress()
            iv_start.isClickable = true
            it.start()
            it.seekTo(realOffset.toInt())
        }
    }

    private fun startJobRereshTimeAndProgress() {
        seekBarJob = launch {
            while (isActive) {
                delay(1000)
                tv_current_pos.setText(CommonUtils.formatTime(palayback_video.currentPosition.toLong()))
                video_seekbar.progress = palayback_video.currentPosition / 1000
            }
        }
    }

    private var onInfoListener = object : MediaPlayer.OnInfoListener {
        override fun onInfo(mp: MediaPlayer?, what: Int, extra: Int): Boolean {
            mp?.let {
                if (it.isPlaying) {
                    iv_start.setImageResource(R.mipmap.stop)
                    pause_tip_layout.visibility = View.GONE
                    return true
                }
            }
            iv_start.setImageResource(R.mipmap.start)
            pause_tip_layout.visibility = View.VISIBLE
            return true
        }
    }

    override fun eventReady(events: MutableList<ActionRecord>) {
        launch(Dispatchers.Main) {
            records.addAll(events)
            adapter?.notifyDataSetChanged()
        }
    }

    override fun fail(msg: String?, reqCode: Int) {
        ToastDialog(context, ToastDialog.Type.WARNING, msg?:"", 2000).show()
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

                        var url = String.format(URL_FORMAT, baseUrl,
                            (dataList.get(0).startTime.time / 1000).toString(),
                            (dataList.get(0).endTime.time / 1000).toString())

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
}