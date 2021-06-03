package com.tencent.iot.explorer.link.core.demo.video.fragment

import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.alibaba.fastjson.JSONArray
import com.alibaba.fastjson.JSONObject
import com.tencent.iot.explorer.link.core.demo.App
import com.tencent.iot.explorer.link.core.demo.R
import com.tencent.iot.explorer.link.core.demo.entity.BaseResponse
import com.tencent.iot.explorer.link.core.demo.entity.TimeBlock
import com.tencent.iot.explorer.link.core.demo.entity.VideoHistory
import com.tencent.iot.explorer.link.core.demo.fragment.BaseFragment
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
import kotlinx.android.synthetic.main.fragment_video_cloud_playback.*
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class VideoCloudPlaybackFragment : BaseFragment(), EventView {
    var dateFormat = SimpleDateFormat(CalendarView.SECOND_DATE_FORMAT_PATTERN, Locale.getDefault())
    var devInfo: DevInfo? = null
    var baseUrl = ""
    var adapter : ActionListAdapter? = null
    var records : MutableList<ActionRecord> = ArrayList()
    lateinit var presenter: EventPresenter

    override fun getContentView(): Int {
        return R.layout.fragment_video_cloud_playback
    }

    override fun startHere(view: View) {
        presenter = EventPresenter(this)

        tv_date.setText(dateFormat.format(System.currentTimeMillis()))
        setListener()
        var linearLayoutManager = LinearLayoutManager(context)
        adapter = ActionListAdapter(records)
        action_list_view.layoutManager = linearLayoutManager
        action_list_view.adapter = adapter

        // 获取当前时间节点以前的事件云内容
        records.clear()
        App.data.accessInfo?.let {
            if (devInfo == null) return@let
            presenter.getCurrentDayEventsData(it.accessId, it.accessToken, it.productId, devInfo!!.deviceName)
        }
    }

    private fun try2GetRecord(date : Date) {
        records.clear()
        App.data.accessInfo?.let {
            if (devInfo == null) return@let
            presenter.getEventsData(it.accessId, it.accessToken, it.productId, date, devInfo!!.deviceName)
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
                                time_line.setTimeLineTimeDay(dataList.get(0).startTime)
                                time_line.timeBlockInfos = dataList
                                time_line.invalidate()
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
    }

    var timeLineViewChangeListener = object : TimeLineViewChangeListener {
        override fun onChange(date: Date?, timeLineView: TimeLineView?) {
            date?.let {

            }
        }
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
        records.addAll(events)
        adapter?.notifyDataSetChanged()
    }

}