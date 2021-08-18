package com.tencent.iot.explorer.link.rtc.consts;

public class Common {
    public static final String TRTC_ACTION = "action";

    public static final String TRTC_PARAM = "params";

    public static final String TRTC_SUB_TYPE = "SubType";

    public static final String TRTC_REPORT = "Report";

    public static final String TRTC_REPORT_LOW = "report";

    public static final String TRTC_METHOD = "method";

    public static final String TRTC_AUDIO_CALL_STATUS = "_sys_audio_call_status";

    public static final String TRTC_VIDEO_CALL_STATUS = "_sys_video_call_status";

    public static final String TRTC_USERID = "_sys_userid";

    public static final String TRTC_AGENT = "_sys_user_agent";

    public static final String TRTC_EXTRA_INFO = "_sys_extra_info";

    public static final String TRTC_REJECT_USERID = "rejectUserId";

    public static final String TRTC_DEVICE_CHANGE = "DeviceChange";

    public static final int TRTC_STATUS_NONE = -1;

    /**
     * 空闲或拒绝
     */
    public static final int TRTC_STATUS_FREE_OR_REJECT = 0;

    /**
     * 呼叫中
     */
    public static final int TRTC_STATUS_CALL = 1;

    /**
     * 通话中
     */
    public static final int TRTC_STATUS_CALLING = 2;
}
