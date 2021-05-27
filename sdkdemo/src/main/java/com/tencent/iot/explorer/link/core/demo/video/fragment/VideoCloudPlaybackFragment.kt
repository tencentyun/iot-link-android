package com.tencent.iot.explorer.link.core.demo.video.fragment

import android.view.View
import com.tencent.iot.explorer.link.core.demo.R
import com.tencent.iot.explorer.link.core.demo.fragment.BaseFragment
import com.tencent.iot.explorer.link.core.demo.video.dialog.CalendarDialog
import com.tencent.iot.explorer.link.core.demo.video.dialog.CalendarDialog.OnClickedListener
import com.tencent.iot.explorer.link.core.demo.video.dialog.ToastDialog
import com.tencent.iot.explorer.link.core.demo.view.CalendarView
import kotlinx.android.synthetic.main.fragment_video_cloud_playback.*
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

class VideoCloudPlaybackFragment : BaseFragment() {
    var dateFormat = SimpleDateFormat(CalendarView.SECOND_DATE_FORMAT_PATTERN, Locale.getDefault());

    override fun getContentView(): Int {
        return R.layout.fragment_video_cloud_playback
    }

    override fun startHere(view: View) {
        tv_date.setText(dateFormat.format(System.currentTimeMillis()))

        setListener()
    }

    override fun onResume() {
        super.onResume()
    }

    private fun setListener() {
        layout_select_date.setOnClickListener {
            var testDateSet = arrayListOf("20210527", "20210526", "20210525", "20210522", "20210510", "20210426")
            var dlg = CalendarDialog(context, testDateSet)
            dlg.show()
            dlg.setOnClickedListener(object : OnClickedListener {
                override fun onOkClicked(checkedDates: MutableList<String>?) {
                    checkedDates?.let {
                        if (it.size <= 0) return@let
                        tv_date.setText(dateConvertionWithSplit(it.get(0))) // 当前列表有数据时，有且仅有一个元素，所以可以直接去第一个位置的元素
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

    private fun dateConvertionWithoutSplit(str: String): String? {
        var dateString = ""
        try {
            var parse = SimpleDateFormat(CalendarView.SECOND_DATE_FORMAT_PATTERN).parse(str)
            parse.let {
                dateString = SimpleDateFormat(CalendarView.DATE_FORMAT_PATTERN).format(parse)
            }
        } catch (e: ParseException) { }
        return dateString
    }

}