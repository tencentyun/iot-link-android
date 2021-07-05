package com.tencent.iot.explorer.link.demo.common.customView.timeline;

import java.util.Date;

public class TimeBlockInfo {
    private Date startTime;
    private Date endTime;

    public TimeBlockInfo(){}

    public TimeBlockInfo(Date startTime, Date endTime){
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }
}
