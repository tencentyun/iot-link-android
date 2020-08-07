package com.tencent.iot.explorer.link.kitlink.util;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DateUtils {
    public static String getFormatDate(Date date) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss");
        return format.format(date);
    }
}
