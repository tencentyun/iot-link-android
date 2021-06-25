package com.tencent.iot.explorer.link.core.demo.video.utils

import android.content.Context
import android.os.Environment
import com.tencent.iot.explorer.link.core.demo.R
import com.tencent.iot.explorer.link.core.demo.entity.TimeBlock
import com.tencent.iot.explorer.link.core.demo.view.CalendarView
import com.tencent.iot.explorer.link.core.demo.view.timeline.TimeBlockInfo
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

class CommonUtils {
    companion object {
        var FILE_NAME_DATE_FORMAT = "yyyy_MM_dd_HH_mm_ss"

        fun generateFileDefaultPath(): String {
            val sdf = SimpleDateFormat(FILE_NAME_DATE_FORMAT)
            var fileName = "${sdf.format(Date())}.mp4"
            var path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).absolutePath
            if (path.endsWith("/")) {
                path = path.substring(0, path.length - 1)
            }
            return "${path}/${fileName}"
        }

        fun getWeekDay(context: Context) : String {
            var calendar = Calendar.getInstance()
            calendar.setTimeInMillis(System.currentTimeMillis())
            var day = calendar.get(Calendar.DAY_OF_WEEK)

            var retStr = context.getString(R.string.week)
            when(day % 7) {
                0 -> retStr += context.getString(R.string.saturday)
                1 -> retStr += context.getString(R.string.sunday)
                2 -> retStr += context.getString(R.string.monday)
                3 -> retStr += context.getString(R.string.tuesday)
                4 -> retStr += context.getString(R.string.wednesday)
                5 -> retStr += context.getString(R.string.thursday)
                6 -> retStr += context.getString(R.string.friday)
            }
            return retStr
        }

        fun dateConvertionWithSplit(str: String): String? {
            var dateString = ""
            try {
                var parse = SimpleDateFormat(CalendarView.DATE_FORMAT_PATTERN).parse(str)
                parse.let {
                    dateString = SimpleDateFormat(CalendarView.SECOND_DATE_FORMAT_PATTERN).format(parse)
                }
            } catch (e: ParseException) { }
            return dateString
        }

        fun formatTimeData(allTimeBlock: MutableList<TimeBlock>) : MutableList<TimeBlockInfo> {
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
    }
}