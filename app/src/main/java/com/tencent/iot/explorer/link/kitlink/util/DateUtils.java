package com.tencent.iot.explorer.link.kitlink.util;

import com.tencent.iot.explorer.link.R;
import com.tencent.iot.explorer.link.T;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class DateUtils {
    public static String getFormatDate(Date date) {
        SimpleDateFormat format = new SimpleDateFormat(T.getContext().getString(R.string.date_format));
        return format.format(date);
    }

    public static String getFormatDateWithoutTime(Date date) {
        SimpleDateFormat format = new SimpleDateFormat(T.getContext().getString(R.string.date_without_time_format));
        return format.format(date);
    }

    public static Date getDateAfter(Date date, int day) {
        Calendar now =Calendar.getInstance();
        now.setTime(date);
        now.set(Calendar.DATE, now.get(Calendar.DATE) + day);
        return now.getTime();
    }

    /**
     * 把毫秒值转化为yyyy-MM-dd
     *
     * @param currentTimeMillis
     * @return
     */
    public static String forString(long currentTimeMillis) {
        return forString(currentTimeMillis, null);
    }

    /**
     * 把毫秒值转化为format格式的日期
     *
     * @param currentTimeMillis
     * @return
     */
    public static String forString(long currentTimeMillis, String format) {
        Date d = new Date(currentTimeMillis);
        if ((format == null) || format.equals("")) {
            format = "yyyy-MM-dd";
        }
        DateFormat df = new SimpleDateFormat(format);
        String str = df.format(d);
        return str;
    }
}
