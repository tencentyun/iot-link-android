package com.tencent.iot.explorer.link.kitlink.util;

import com.tencent.iot.explorer.link.App;
import com.tencent.iot.explorer.link.R;
import com.tencent.iot.explorer.link.util.T;

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
}
