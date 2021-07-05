package com.tencent.iot.explorer.link.demo.common.util

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import com.tencent.iot.explorer.link.demo.R
import com.tencent.iot.explorer.link.demo.core.entity.TimeBlock
import com.tencent.iot.explorer.link.demo.video.dialog.ToastDialog
import com.tencent.iot.explorer.link.demo.common.customView.CalendarView
import com.tencent.iot.explorer.link.demo.common.customView.timeline.TimeBlockInfo
import java.io.File
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

        fun formatTime(time: Long): String {
            var hours = time / (1000 * 60 * 60)
            var leftMin = time % (1000 * 60 * 60)
            return String.format("%02d:%02d:%02d", hours, leftMin / (1000 * 60), (leftMin / 1000) % 60)
        }

        fun refreshVideoList(ctx: Context, path: String?) {
            path?.let {
                var file = File(it)
                if (!file.exists()) return@let

                var localContentValues = getVideoContentValues(file, System.currentTimeMillis())
                ctx.contentResolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, localContentValues)
                ctx.sendBroadcast(Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(file)))
                ToastDialog(ctx, ToastDialog.Type.SUCCESS, ctx.getString(R.string.capture_successed), 1500).show()
            }
        }

        private fun getVideoContentValues(paramFile: File, paramLong: Long): ContentValues {
            var localContentValues = ContentValues()
            localContentValues.put("title", paramFile.getName())
            localContentValues.put("_display_name", paramFile.getName())
            localContentValues.put("mime_type", "video/mp4")
//        localContentValues.put("datetaken", paramLong)
//        localContentValues.put("date_modified", paramLong)
//        localContentValues.put("date_added", paramLong)
//        localContentValues.put("_data", paramFile.getAbsolutePath())
//        localContentValues.put("_size", paramLong)
            return localContentValues
        }
    }
}