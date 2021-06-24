package com.tencent.iot.explorer.link.kitlink.util;

import com.tencent.iot.explorer.link.R;
import com.tencent.iot.explorer.link.T;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

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

    public static String utc2Local(String utcTime) {
        //UTC时间格式
        SimpleDateFormat utcFormater = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        utcFormater.setTimeZone(TimeZone.getTimeZone("UTC"));
        Date gpsUTCDate = null;
        try {
            gpsUTCDate = utcFormater.parse(utcTime);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        SimpleDateFormat localFormater = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//当地时间格式
        localFormater.setTimeZone(TimeZone.getDefault());
        if (gpsUTCDate == null) {
            return localFormater.format(System.currentTimeMillis());
        }

        String localTime = localFormater.format(gpsUTCDate.getTime());
        return localTime;
    }
}
