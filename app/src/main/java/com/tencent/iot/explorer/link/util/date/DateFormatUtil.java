package com.tencent.iot.explorer.link.util.date;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 日期格式处理
 */
public class DateFormatUtil {

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
     * 把毫秒值转化为yyyy-MM-dd
     *
     * @param currentTimeMillis
     * @return
     */
    public static String forString(String currentTimeMillis) {
        try {
            return forString(Long.valueOf(currentTimeMillis) * 1000, null);
        } catch (Exception e) {
            e.printStackTrace();
            return forString(0, null);
        }

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

    /**
     * 取得当前时间戳（精确到秒）
     */
    public static long getTime() {
        long time = System.currentTimeMillis();
        return time;
    }

    /**
     * 毫秒转为时/分/秒
     */
    public static String toHMS(long currentTimeMillis) {
        long ls = currentTimeMillis / 1000;
        String s = (ls % 60) + "秒";
        int im = (int) (ls / 60);
        String m = "";
        if (im > 0) {
            m = (im % 60) + "分钟";
        }
        String h = "";
        int ih = im / 60;
        if (ih > 0) {
            h = ih + "小时";
        }
        return h + m + s;
    }

    /**
     * 把时间转化在long
     *
     * @param date
     * @param format
     * @return
     */
    public static long stringToLong(String date, String format) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(format);
            return sdf.parse(date).getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return 0;
    }
}
