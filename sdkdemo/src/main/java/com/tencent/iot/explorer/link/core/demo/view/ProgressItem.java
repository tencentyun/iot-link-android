package com.tencent.iot.explorer.link.core.demo.view;

public class ProgressItem {

    public int progressItemPercentage;
    public int progressItemPercentageEnd;
    public int startHour;
    public int startMin;
    public int startSec;
    public int endHour;
    public int endMin;
    public int endSec;
    public long startTimeMillis;
    public long endTimeMillis;

    public static int getProgressItemPercentage(ProgressItem item) {
        return item.startHour * 60 + item.startMin;
    }

    public static int getProgressItemPercentageEnd(ProgressItem item) {
        return item.endHour * 60 + item.endMin;
    }
}
