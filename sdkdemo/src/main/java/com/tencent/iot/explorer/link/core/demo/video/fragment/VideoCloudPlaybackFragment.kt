package com.tencent.iot.explorer.link.core.demo.video.fragment

import android.media.MediaPlayer
import android.net.Uri
import android.os.Handler
import android.util.Log
import android.view.View
import android.widget.MediaController
import android.widget.SeekBar
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.LinearLayoutManager
import com.alibaba.fastjson.JSONArray
import com.alibaba.fastjson.JSONObject
import com.tencent.iot.explorer.link.core.demo.App
import com.tencent.iot.explorer.link.core.demo.R
import com.tencent.iot.explorer.link.core.demo.entity.BaseResponse
import com.tencent.iot.explorer.link.core.demo.entity.TimeBlock
import com.tencent.iot.explorer.link.core.demo.entity.VideoHistory
import com.tencent.iot.explorer.link.core.demo.fragment.BaseFragment
import com.tencent.iot.explorer.link.core.demo.response.SignedUrlResponse
import com.tencent.iot.explorer.link.core.demo.video.adapter.ActionListAdapter
import com.tencent.iot.explorer.link.core.demo.video.dialog.CalendarDialog
import com.tencent.iot.explorer.link.core.demo.video.dialog.CalendarDialog.OnClickedListener
import com.tencent.iot.explorer.link.core.demo.video.dialog.ToastDialog
import com.tencent.iot.explorer.link.core.demo.video.entity.ActionRecord
import com.tencent.iot.explorer.link.core.demo.video.entity.DevInfo
import com.tencent.iot.explorer.link.core.demo.video.mvp.presenter.EventPresenter
import com.tencent.iot.explorer.link.core.demo.video.mvp.view.EventView
import com.tencent.iot.explorer.link.core.demo.view.CalendarView
import com.tencent.iot.explorer.link.core.demo.view.timeline.TimeBlockInfo
import com.tencent.iot.explorer.link.core.demo.view.timeline.TimeLineView
import com.tencent.iot.explorer.link.core.demo.view.timeline.TimeLineViewChangeListener
import com.tencent.iot.video.link.callback.VideoCallback
import com.tencent.iot.video.link.service.VideoBaseService
import kotlinx.android.synthetic.main.activity_date_ipc.*
import kotlinx.android.synthetic.main.activity_ipc.*
import kotlinx.android.synthetic.main.activity_video_preview.*
import kotlinx.android.synthetic.main.fragment_video_cloud_playback.*
import kotlinx.android.synthetic.main.fragment_video_cloud_playback.tv_date
import kotlinx.coroutines.*
import java.lang.Runnable
import java.net.URLDecoder
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class VideoCloudPlaybackFragment : BaseFragment(), EventView, CoroutineScope by MainScope() {
    private var dateFormat = SimpleDateFormat(CalendarView.SECOND_DATE_FORMAT_PATTERN, Locale.getDefault())
    var devInfo: DevInfo? = null
    private var baseUrl = ""
    private var adapter : ActionListAdapter? = null
    private var records : MutableList<ActionRecord> = ArrayList()
    private var handler = Handler()
    private var job : Job? = null
    @Volatile
    private var portrait = true
    private lateinit var presenter: EventPresenter
    private var URL_FORMAT = "%s?starttime_epoch=%s&endtime_epoch=%s"
    private var seekBarJob : Job? = null

    override fun onDestroy() {
        super.onDestroy()
        cancel()
    }

    override fun getContentView(): Int {
        return R.layout.fragment_video_cloud_playback
    }

    override fun startHere(view: View) {
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

    private fun try2GetRecord(date : Date) {
        records.clear()
        App.data.accessInfo?.let {
            if (devInfo == null) return@let
            presenter.getEventsData(date)
        }
    }

    private fun try2GetVideoDateData() {
        App.data.accessInfo?.let {
            if (devInfo == null) return@let
            VideoBaseService(it.accessId, it.accessToken).getIPCDateData(it.productId, devInfo!!.deviceName,
                object : VideoCallback {
                override fun fail(msg: String?, reqCode: Int) {
                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                }

                override fun success(response: String?, reqCode: Int) {
                    var resp = JSONObject.parseObject(response, BaseResponse::class.java)
                    resp?.let {
                        if (it.Response == null) return@let
                        var dataList = JSONArray.parseArray(it.Response!!.data, String::class.java)
                        dataList?.let {
                            if (it.size == 0) {
                                ToastDialog(context, ToastDialog.Type.WARNING, getString(R.string.no_data), 2000).show()
                                return@let
                            }
                            for (i in 0 until it.size) {
                                it[i] = it.get(i).replace("-", "")
                            }
                            showCalendarDialog(it)
                        }?:let {
                            ToastDialog(context, ToastDialog.Type.WARNING, getString(R.string.no_data), 2000).show()
                        }
                    }
                }
            })
        }
    }

    private fun formatTimeData(allTimeBlock: MutableList<TimeBlock>) : MutableList<TimeBlockInfo> {
        var dateList = ArrayList<TimeBlockInfo>()

        var i = 0
        while (i < allTimeBlock.size) {
            var start = Date(allTimeBlock.get(i).StartTime * 1000)
            while (i + 1 < allTimeBlock.size &&
                ((allTimeBlock.get(i).EndTime + 60) >= allTimeBlock.get(i + 1).StartTime)) {  // 上一次的结束时间和下一次的开始时间相差一分钟之内
                i++
            }
            var end = Date(allTimeBlock.get(i).EndTime * 1000)

            var item = TimeBlockInfo()
            item.startTime = start
            item.endTime = end
            dateList.add(item)
            i++
        }

        return dateList
    }

    private fun refreshTimeLine(timeStr : String?) {
        App.data.accessInfo?.let {
            if (devInfo == null || timeStr == null) return@let
            VideoBaseService(it.accessId, it.accessToken).getIPCTimeData(it.productId, devInfo!!.deviceName, timeStr, object : VideoCallback {
                override fun fail(msg: String?, reqCode: Int) {
                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                }

                override fun success(response: String?, reqCode: Int) {
                    var resp = JSONObject.parseObject(response, BaseResponse::class.java)
                    resp?.let {
                        var history = JSONObject.parseObject(it.Response?.data, VideoHistory::class.java)
                        history?.let {
                            baseUrl = it.VideoURL
                            it.TimeList?.let {
                                if (it.size <= 0) return@let
                                var selectDate = SimpleDateFormat(CalendarView.SECOND_DATE_FORMAT_PATTERN).parse(timeStr)
                                if (selectDate == null) return@let
                                time_line.currentDayTime = selectDate

                                var dataList = formatTimeData(it)
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
                }
            })
        }
    }

    private fun showCalendarDialog(dataList: MutableList<String>) {
        var dlg = CalendarDialog(context, dataList)
        dlg.show()
        dlg.setOnClickedListener(object : OnClickedListener {
            override fun onOkClicked(checkedDates: MutableList<String>?) {
                checkedDates?.let {
                    if (it.size <= 0) return@let
                    tv_date.setText(dateConvertionWithSplit(it.get(0))) // 当前列表有数据时，有且仅有一个元素，所以可以直接去第一个位置的元素
                    refreshTimeLine(dateConvertionWithSplit(it.get(0)))

                    var selectDate = SimpleDateFormat(CalendarView.SECOND_DATE_FORMAT_PATTERN).parse(dateConvertionWithSplit(it.get(0)))
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
        layout_select_date.setOnClickListener {
            try2GetVideoDateData()
        }

        iv_left_go.setOnClickListener {
            time_line.last()
        }

        iv_right_go.setOnClickListener {
            time_line.next()
        }

        time_line.setTimelineChangeListener(timeLineViewChangeListener)
        layout_video.setOnClickListener {
            if (playback_control.visibility == View.VISIBLE) {
                playback_control.visibility = View.GONE
                job?.let {
                    it.cancel()
                }
            } else {
                playback_control.visibility = View.VISIBLE
                job = launch {
                    delay(5 * 1000)
                    playback_control.visibility = View.GONE
                }
            }
        }

        playback_control_orientation.setOnClickListener {
            portrait = !portrait
            onOrientationChangedListener?.onOrientationChanged(portrait)
            var moreSpace = 12
            var marginWidth = 0
            if (portrait) {
                layout_control.visibility = View.VISIBLE
                v_space.visibility = View.VISIBLE
            } else {
                layout_control.visibility = View.GONE
                v_space.visibility = View.GONE
                moreSpace = 32
                marginWidth = 73
            }

            var btnLayoutParams = playback_control_orientation.layoutParams as ConstraintLayout.LayoutParams
            btnLayoutParams.bottomMargin = dp2px(moreSpace)
            playback_control_orientation.layoutParams = btnLayoutParams

            var videoLayoutParams = palayback_video.layoutParams as ConstraintLayout.LayoutParams
            videoLayoutParams.marginStart = dp2px(marginWidth)
            videoLayoutParams.marginEnd = dp2px(marginWidth)
            palayback_video.layoutParams = videoLayoutParams
        }

        playback_control.setOnClickListener {  }
        palayback_video.setOnInfoListener(onInfoListener)
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

        video_seekbar.setOnSeekBarChangeListener(onSeekBarChangeListener)
        palayback_video.setOnErrorListener(onErrorListener)
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
        override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
            if (fromUser) {  // 是用户操作的，调整视频到指定的时间点
                palayback_video.seekTo(progress * 1000)
                return
            }
        }

        override fun onStartTrackingTouch(seekBar: SeekBar?) {}
        override fun onStopTrackingTouch(seekBar: SeekBar?) {}
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
                        var value = it.getJSONObject("Response")
                        value?.let {
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
        val uri: Uri = Uri.parse(url)
        palayback_video.setVideoURI(uri)
        seekBarJob?.cancel()
        palayback_video.setOnPreparedListener {
            var realOffset = offset
            if (realOffset >= it.duration) {
                realOffset = it.duration.toLong()
            }
            tv_current_pos.setText(formatTime(realOffset))
            tv_all_time.setText(formatTime(it.duration.toLong()))
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
                tv_current_pos.setText(formatTime(palayback_video.currentPosition.toLong()))
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
                } else {
                    iv_start.setImageResource(R.mipmap.start)
                    pause_tip_layout.visibility = View.VISIBLE
                }
            }?:let {
                iv_start.setImageResource(R.mipmap.start)
                pause_tip_layout.visibility = View.VISIBLE
            }
            return true
        }
    }

    private fun formatTime(time: Long): String {
        var hours = time / (1000 * 60 * 60)
        var leftMin = time % (1000 * 60 * 60)
        return String.format("%02d:%02d:%02d", hours, leftMin / (1000 * 60), (leftMin / 1000) % 60)
    }

    private fun dateConvertionWithSplit(str: String): String? {
        var dateString = ""
        try {
            var parse = SimpleDateFormat(CalendarView.DATE_FORMAT_PATTERN).parse(str)
            parse.let {
                dateString = SimpleDateFormat(CalendarView.SECOND_DATE_FORMAT_PATTERN).format(parse)
            }
        } catch (e: ParseException) { }
        return dateString
    }

    override fun eventReady(events: MutableList<ActionRecord>) {
        handler.post(Runnable {
            records.addAll(events)
            adapter?.notifyDataSetChanged()
        })
    }

    interface OnOrientationChangedListener {
        fun onOrientationChanged(portrait : Boolean)
    }

    private var onOrientationChangedListener : OnOrientationChangedListener? = null

    fun setOnOrientationChangedListener(onOrientationChangedListener : OnOrientationChangedListener) {
        this.onOrientationChangedListener = onOrientationChangedListener
    }
}