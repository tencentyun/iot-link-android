package com.tencent.iot.explorer.link.kitlink.activity.videoui;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.tencent.iot.explorer.link.kitlink.activity.BaseActivity;
import com.tencent.iot.explorer.link.R;
import com.tencent.iot.explorer.link.kitlink.activity.rtcui.videocall.videolayout.TRTCVideoLayout;
import com.tencent.iot.explorer.link.rtc.model.IntentParams;
import com.tencent.iot.explorer.link.rtc.model.RoomKey;
import com.tencent.iot.explorer.link.rtc.model.TRTCCallStatus;
import com.tencent.iot.explorer.link.rtc.model.TRTCCalling;
import com.tencent.iot.explorer.link.rtc.model.TRTCCallingParamsCallback;
import com.tencent.iot.explorer.link.rtc.model.TRTCUIManager;
import com.tencent.iot.explorer.link.rtc.model.UserInfo;
import com.tencent.iot.explorer.link.rtc.model.impl.TRTCCallingImpl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RecordVideoActivity extends BaseActivity {
    private TextView mStatusView;
    private LinearLayout mHangupLl;
    private LinearLayout mDialingLl;

    public static final int TYPE_BEING_CALLED = 1;
    public static final int TYPE_CALL         = 2;

    public static final String PARAM_TYPE                = "type";
    public static final String PARAM_SELF_INFO           = "self_info";
    public static final String PARAM_USER                = "user_model";
    public static final String PARAM_BEINGCALL_USER      = "beingcall_user_model";
    public static final String PARAM_OTHER_INVITING_USER = "other_inviting_user_model";

    /**
     * 拨号相关成员变量
     */
    private UserInfo              mSelfModel;
    private List<UserInfo> mCallUserInfoList = new ArrayList<>(); // 呼叫方
    private Map<String, UserInfo> mCallUserModelMap = new HashMap<>();
    private UserInfo              mSponsorUserInfo;                      // 被叫方
    private List<UserInfo> mOtherInvitingUserInfoList;
    private int                   mCallType;
    private TRTCCallingImpl mTRTCCalling;

    /**
     * 主动拨打给某个用户
     *
     * @param context
     * @param roomKey
     */
    public static void startCallSomeone(Context context, RoomKey roomKey, String beingCallUserId) {
        Intent starter = new Intent(context, RecordVideoActivity.class);
        starter.putExtra(PARAM_TYPE, TYPE_CALL);
        starter.putExtra(PARAM_SELF_INFO, JSON.toJSONString(roomKey));
        UserInfo beingCallUserInfo = new UserInfo();
        beingCallUserInfo.setUserId(beingCallUserId);
        starter.putExtra(PARAM_BEINGCALL_USER, beingCallUserInfo);
        context.startActivity(starter);
        TRTCUIManager.getInstance().callStatus = TRTCCallStatus.TYPE_CALLING.getValue();
    }

    /**
     * 作为用户被叫
     *
     * @param context
     * @param beingCallUserId
     */
    public static void startBeingCall(Context context, RoomKey roomKey, String beingCallUserId) {
        Intent starter = new Intent(context, RecordVideoActivity.class);
        starter.putExtra(PARAM_TYPE, TYPE_BEING_CALLED);
        starter.putExtra(PARAM_SELF_INFO, JSON.toJSONString(roomKey));
        UserInfo beingCallUserInfo = new UserInfo();
        beingCallUserInfo.setUserId(beingCallUserId);
        starter.putExtra(PARAM_BEINGCALL_USER, beingCallUserInfo);
        starter.putExtra(PARAM_OTHER_INVITING_USER, new IntentParams(new ArrayList<UserInfo>()));
        starter.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(starter);
        TRTCUIManager.getInstance().callStatus = TRTCCallStatus.TYPE_CALLING.getValue();
    }

    @Override
    public int getContentView() {
        return R.layout.activity_record_video;
    }

    @Override
    public void initView() {
        mStatusView = (TextView) findViewById(R.id.tv_status);
        mHangupLl = (LinearLayout) findViewById(R.id.ll_hangup);
        mDialingLl = (LinearLayout) findViewById(R.id.ll_dialing);
        initData();

        TRTCUIManager.getInstance().addCallingParamsCallback(new TRTCCallingParamsCallback() {
            @Override
            public void joinRoom(Integer callingType, String deviceId, RoomKey roomKey) {   //接听电话

            }

            @Override
            public void exitRoom() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mStatusView.setText(com.tencent.iot.explorer.link.rtc.R.string.trtccalling_customer_hand_up);
                        mStatusView.setVisibility(View.VISIBLE);
                    }
                });
                stopCameraAndFinish();
            }

            @Override
            public void userBusy() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mStatusView.setText(com.tencent.iot.explorer.link.rtc.R.string.trtccalling_customer_busy);
                        mStatusView.setVisibility(View.VISIBLE);
                    }
                });
                stopCameraAndFinish();
            }

            @Override
            public void otherUserAccept() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mStatusView.setText(com.tencent.iot.explorer.link.rtc.R.string.trtccalling_other_customer_accpet);
                        mStatusView.setVisibility(View.VISIBLE);
                    }
                });
                stopCameraAndFinish();
            }

            @Override
            public void userOffline(String deviceId) {
//                mUserOfflineMap.put(deviceId, true);
            }
        });

    }

    private boolean initData() {
        // 初始化成员变量
//        mTimeHandlerThread = new HandlerThread("time-count-thread");
//        mTimeHandlerThread.start();
//        mTimeHandler = new Handler(mTimeHandlerThread.getLooper());
        // 初始化从外界获取的数据
        Intent intent = getIntent();

        String roomKeyStr = intent.getStringExtra(PARAM_SELF_INFO);
        if (TextUtils.isEmpty(roomKeyStr)) {
            finish();
        }
        RoomKey roomKey = JSON.parseObject(roomKeyStr, RoomKey.class);
        mSelfModel = new UserInfo();
        mSelfModel.setUserId(roomKey.getUserId());
        //自己的资料
        mCallType = intent.getIntExtra(PARAM_TYPE, TYPE_BEING_CALLED);
        mSponsorUserInfo = (UserInfo) intent.getSerializableExtra(PARAM_BEINGCALL_USER);
        if (mCallType == TYPE_BEING_CALLED) {
            // 作为被叫
            IntentParams params = (IntentParams) intent.getSerializableExtra(PARAM_OTHER_INVITING_USER);
            if (params != null) {
                mOtherInvitingUserInfoList = params.mUserInfos;
            }
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mStatusView.setText(com.tencent.iot.explorer.link.rtc.R.string.trtccalling_customer_calling_vedio);
                    mStatusView.setVisibility(View.VISIBLE);
                }
            });
            showWaitingResponseView();
            return false;
        } else {
            // 主叫方
            if (roomKey != null) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mStatusView.setText(com.tencent.iot.explorer.link.rtc.R.string.trtccalling_waiting_to_hear_vedio);
                        mStatusView.setVisibility(View.VISIBLE);
                    }
                });
                showInvitingView();
            }
            return true;
        }
    }

    @Override
    public void setListener() {

    }

    private void stopCameraAndFinish() {
        finish();
        TRTCUIManager.getInstance().isCalling = false;
        TRTCUIManager.getInstance().deviceId = "";
        TRTCUIManager.getInstance().callStatus = TRTCCallStatus.TYPE_IDLE_OR_REFUSE.getValue();
        TRTCUIManager.getInstance().removeCallingParamsCallback();
    }

    /**
     * 等待接听界面
     */
    public void showWaitingResponseView() {

        //3. 展示电话对应界面
        mHangupLl.setVisibility(View.VISIBLE);
        mDialingLl.setVisibility(View.VISIBLE);
        //4. 设置对应的listener
        mHangupLl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mSponsorUserInfo == null) {
                    stopCameraAndFinish();
                    return;
                }
                TRTCUIManager.getInstance().refuseEnterRoom(TRTCCalling.TYPE_VIDEO_CALL, mSponsorUserInfo.getUserId());
                stopCameraAndFinish();
            }
        });
        mDialingLl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mSponsorUserInfo == null) {
                    stopCameraAndFinish();
                    return;
                }
                TRTCUIManager.getInstance().didAcceptJoinRoom(TRTCCalling.TYPE_VIDEO_CALL, mSponsorUserInfo.getUserId());
            }
        });
    }

    /**
     * 主动呼叫界面
     */
    public void showInvitingView() {
        //1. 展示自己的界面
        mHangupLl.setVisibility(View.VISIBLE);
        mHangupLl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mSponsorUserInfo == null) {
                    stopCameraAndFinish();
                    return;
                }
                TRTCUIManager.getInstance().refuseEnterRoom(TRTCCalling.TYPE_VIDEO_CALL, mSponsorUserInfo.getUserId());
                stopCameraAndFinish();
            }
        });
        mDialingLl.setVisibility(View.GONE);
    }

    /**
     * 展示通话中的界面
     */
    public void showCallingView() {
        //2. 底部状态栏
        mHangupLl.setVisibility(View.VISIBLE);
        mDialingLl.setVisibility(View.GONE);

        mHangupLl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mTRTCCalling.exitRoom();
                stopCameraAndFinish();
            }
        });
    }
}