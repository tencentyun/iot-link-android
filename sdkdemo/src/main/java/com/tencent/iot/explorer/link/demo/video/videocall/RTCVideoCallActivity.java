package com.tencent.iot.explorer.link.demo.video.videocall;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.Group;

import com.alibaba.fastjson.JSON;
import com.tencent.iot.explorer.link.demo.R;
import com.tencent.iot.explorer.link.demo.video.videocall.videolayout.TRTCVideoLayout;
import com.tencent.iot.explorer.link.demo.video.videocall.videolayout.TRTCVideoLayoutManager;
import com.tencent.iot.video.link.rtc.IoTVideoCloudListener;
import com.tencent.iot.video.link.rtc.IoTVideoParams;
import com.tencent.iot.video.link.rtc.RTCParams;
import com.tencent.iot.video.link.rtc.UserInfo;
import com.tencent.iot.video.link.rtc.impl.IoTVideoCloud;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 用于展示视频通话的主界面，通话的接听和拒绝就是在这个界面中完成的。
 *
 * @author guanyifeng
 */
public class RTCVideoCallActivity extends AppCompatActivity {

    private static final String TAG = RTCVideoCallActivity.class.getSimpleName();

    public static final int TYPE_BEING_CALLED = 1;
    public static final int TYPE_CALL         = 2;

    public static final String PARAM_TYPE                = "type";
    public static final String PARAM_SELF_INFO           = "self_info";

    private ImageView mMuteImg;
    private LinearLayout mMuteLl;
    private ImageView mHangupImg;
    private LinearLayout mHangupLl;
    private ImageView mHandsfreeImg;
    private LinearLayout mHandsfreeLl;
    private ImageView mSwitchCameraImg;
    private LinearLayout mSwitchCameraLl;
    private ImageView mDialingImg;
    private LinearLayout mDialingLl;
    private TRTCVideoLayoutManager mLayoutManagerTrtc;
    private Group mInvitingGroup;
    private LinearLayout mImgContainerLl;
    private TextView mTimeTv;
    private TextView mStatusView;
    private ImageView mSponsorAvatarImg;
    private TextView mSponsorUserNameTv;
    private Group                  mSponsorGroup;
    private Runnable mTimeRunnable;
    private int                    mTimeCount;
    private Handler mTimeHandler;
    private HandlerThread mTimeHandlerThread;

    /**
     * 拨号相关成员变量
     */
    private UserInfo mSelfModel;
    private List<UserInfo> mCallUserInfoList = new ArrayList<>(); // 呼叫方
    private Map<String, UserInfo> mCallUserModelMap = new HashMap<>();
    private List<UserInfo> mOtherInvitingUserInfoList;
    private int                   mCallType;
    private boolean               isHandsFree       = true;
    private boolean               isMuteMic         = false;
    private boolean               mIsFrontCamera    = true;
    private volatile boolean      mIsEnterRoom      = false;
    private Map<String, Boolean> mUserOfflineMap = new HashMap<>();
    private String mRtc_uid = null;
    private long showVideoTime ;
    private long startShowVideoTime ;
    private boolean showTip = false;

    /**
     * 拨号的回调
     */
    private IoTVideoCloudListener mIoTVideoCloudListener = new IoTVideoCloudListener() {
        @Override
        public void onError(int code, String msg) { //发生了错误，报错并退出该页面
            stopCameraAndFinish();
        }

        @Override
        public void onConnect(long result) {

        }

        @Override
        public void onRelease(int reason) {

        }

        @Override
        public void onUserEnter(final String userId) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mRtc_uid = userId;
                }
            });
        }

        @Override
        public void onUserLeave(final String userId) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    //1. 回收界面元素
                    mLayoutManagerTrtc.recyclerCloudViewView(userId);
                    //2. 删除用户model
                    UserInfo userInfo = mCallUserModelMap.remove(userId);
                    if (userInfo != null) {
                        mCallUserInfoList.remove(userInfo);
                    }
                    Toast.makeText(getApplicationContext(), "对方已挂断", Toast.LENGTH_LONG).show();
                    stopCameraAndFinish();
                }
            });
        }

        @Override
        public void onUserVideoAvailable(final String userId, final boolean isVideoAvailable) {
            //有用户的视频开启了
            TRTCVideoLayout layout = mLayoutManagerTrtc.findCloudViewView(userId);
            if (layout != null) {
                layout.setVideoAvailable(isVideoAvailable);
                if (isVideoAvailable) {
                    IoTVideoCloud.sharedInstance(RTCVideoCallActivity.this).startRemoteView(userId, layout.getVideoView());
                }
            }
        }

        @Override
        public void onUserVoiceVolume(Map<String, Integer> volumeMap) {
            for (Map.Entry<String, Integer> entry : volumeMap.entrySet()) {
                String userId = entry.getKey();
                TRTCVideoLayout layout = mLayoutManagerTrtc.findCloudViewView(userId);
                if (layout != null) {
                    layout.setAudioVolumeProgress(entry.getValue());
                }
            }
        }

        @Override
        public void onRecvCustomCmdMsg(String rtc_uid, String message) {
            if (!message.isEmpty() && message.equals("1001")) { // 设备接听了，信令可双方协商自定义
                showCallingView();
                startShowVideoTime = System.currentTimeMillis();
                if (mRtc_uid != null && !mRtc_uid.isEmpty()) {
                    //1.将对方用户mtrtc_uid添加到屏幕上
                    UserInfo model = new UserInfo();
                    model.setUserId(mRtc_uid);
                    model.userName = mRtc_uid;
                    model.userAvatar = "";
                    mCallUserInfoList.add(model);
                    mCallUserModelMap.put(model.getUserId(), model);
                    TRTCVideoLayout videoLayout = addUserToManager(model);
                    if (videoLayout == null) {
                        return;
                    }
                    videoLayout.setVideoAvailable(false);
                    IoTVideoCloud.sharedInstance(RTCVideoCallActivity.this).startLocalStream("");
                } else {
                    Toast.makeText(getApplicationContext(), "对方未进房", Toast.LENGTH_LONG).show();
                }
            }
        }

        @Override
        public void getVideoPacketWithID(String id, byte[] data, int len) {

        }

        @Override
        public String reviceDeviceMsgWithID(String id, byte[] data) {
            return "";
        }

        @Override
        public void onFirstVideoFrame(String userId, int width, int height) {
            if (!showTip  && startShowVideoTime > 0) {
                showVideoTime = System.currentTimeMillis() - startShowVideoTime;
                Toast.makeText(getApplicationContext(), String.format("出图: %s(ms)", showVideoTime), Toast.LENGTH_LONG).show();
                showTip = true;
            }
        }

        @Override
        public void reviceEventMsgWithID(String id, int eventType) {

        }
    };

    /**
     * 主动拨打给某个用户
     *
     * @param context
     * @param RTCParams
     */
    public static void startCallSomeone(Context context, RTCParams RTCParams) {
        Intent starter = new Intent(context, RTCVideoCallActivity.class);
        starter.putExtra(PARAM_TYPE, TYPE_CALL);
        starter.putExtra(PARAM_SELF_INFO, JSON.toJSONString(RTCParams));
        context.startActivity(starter);
    }

    /**
     * 作为用户被叫
     *
     * @param context
     * @param RTCParams
     */
    public static void startBeingCall(Context context, RTCParams RTCParams) {
        Intent starter = new Intent(context, RTCVideoCallActivity.class);
        starter.putExtra(PARAM_TYPE, TYPE_BEING_CALLED);
        starter.putExtra(PARAM_SELF_INFO, JSON.toJSONString(RTCParams));
        starter.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(starter);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 应用运行时，保持不锁屏、全屏化
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.trtccalling_videocall_activity_call_main);

        initView();
        initData();
        initListener();
    }

    private void initView() {
        mMuteImg = (ImageView) findViewById(R.id.iv_mute);
        mMuteLl = (LinearLayout) findViewById(R.id.ll_mute);
        mHangupImg = (ImageView) findViewById(R.id.iv_hangup);
        mHangupLl = (LinearLayout) findViewById(R.id.ll_hangup);
        mHandsfreeImg = (ImageView) findViewById(R.id.iv_handsfree);
        mHandsfreeLl = (LinearLayout) findViewById(R.id.ll_handsfree);
        mSwitchCameraImg = (ImageView) findViewById(R.id.iv_switch_camera);
        mSwitchCameraLl = (LinearLayout) findViewById(R.id.ll_switch_camera);
        mDialingImg = (ImageView) findViewById(R.id.iv_dialing);
        mDialingLl = (LinearLayout) findViewById(R.id.ll_dialing);
        mLayoutManagerTrtc = (TRTCVideoLayoutManager) findViewById(R.id.trtc_layout_manager);
        mInvitingGroup = (Group) findViewById(R.id.group_inviting);
        mImgContainerLl = (LinearLayout) findViewById(R.id.ll_img_container);
        mTimeTv = (TextView) findViewById(R.id.tv_time);
        mSponsorAvatarImg = (ImageView) findViewById(R.id.iv_sponsor_avatar);
        mSponsorUserNameTv = (TextView) findViewById(R.id.tv_sponsor_user_name);
        mSponsorGroup = (Group) findViewById(R.id.group_sponsor);
        mStatusView = (TextView) findViewById(R.id.tv_status);
    }

    private void initData() {
        // 初始化从外界获取的数据
        Intent intent = getIntent();
        String roomKeyStr = intent.getStringExtra(PARAM_SELF_INFO);
        if (TextUtils.isEmpty(roomKeyStr)) {
            finish();
        }
        RTCParams RTCParams = JSON.parseObject(roomKeyStr, RTCParams.class);
        mSelfModel = new UserInfo();
        mSelfModel.setUserId(RTCParams.getUserId());
        //自己的资料
        mCallType = intent.getIntExtra(PARAM_TYPE, TYPE_BEING_CALLED);
        // 初始化成员变量
        IoTVideoParams videoParams = new IoTVideoParams();
        videoParams.setRtcParams(RTCParams);
        IoTVideoCloud.sharedInstance(this).startAppWith(videoParams);
        IoTVideoCloud.sharedInstance(this).setListener(mIoTVideoCloudListener);
        mTimeHandlerThread = new HandlerThread("time-count-thread");
        mTimeHandlerThread.start();
        mTimeHandler = new Handler(mTimeHandlerThread.getLooper());
        //calltype主叫和被叫处理
        if (mCallType == TYPE_BEING_CALLED) {
            // 被叫 展示等待对方回应的视图
            showWaitingResponseView();
        } else {
            // 主叫 展示邀请对方的视图
            showInvitingView();
        }
    }

    private void initListener() {
        mMuteLl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isMuteMic = !isMuteMic;
                IoTVideoCloud.sharedInstance(RTCVideoCallActivity.this).muteLocalAudio(isMuteMic);
                mMuteImg.setActivated(isMuteMic);
            }
        });
        mHandsfreeLl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isHandsFree = !isHandsFree;
                IoTVideoCloud.sharedInstance(RTCVideoCallActivity.this).setHandsFree(isHandsFree);
                mHandsfreeImg.setActivated(isHandsFree);
            }
        });
        mSwitchCameraLl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mIsFrontCamera = !mIsFrontCamera;
                IoTVideoCloud.sharedInstance(RTCVideoCallActivity.this).changeCameraPositon(mIsFrontCamera);
                mSwitchCameraImg.setActivated(mIsFrontCamera);
            }
        });
        mMuteImg.setActivated(isMuteMic);
        mHandsfreeImg.setActivated(isHandsFree);
        mSwitchCameraImg.setActivated(mIsFrontCamera);
    }

    @Override
    public void onBackPressed() {
        stopCameraAndFinish();
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopTimeCount();
        mTimeHandlerThread.quit();
    }

    private void stopCameraAndFinish() {
        IoTVideoCloud.sharedInstance(this).stopAppService("");
        finish();
    }

    /**
     * 等待接听界面
     */
    public void showWaitingResponseView() {
        //1. 展示自己的画面
        mLayoutManagerTrtc.setMySelfUserId(mSelfModel.getUserId());
        TRTCVideoLayout videoLayout = addUserToManager(mSelfModel);
        if (videoLayout == null) {
            return;
        }
        videoLayout.setVideoAvailable(true);
        IoTVideoCloud.sharedInstance(this).openCamera(true, videoLayout.getVideoView());

        //2. 展示对方的头像和蒙层
        mSponsorGroup.setVisibility(View.VISIBLE);
        mSponsorGroup.setVisibility(View.INVISIBLE);

        //3. 展示电话对应界面
        mHangupLl.setVisibility(View.VISIBLE);
        mDialingLl.setVisibility(View.VISIBLE);
        mHandsfreeLl.setVisibility(View.GONE);
        mMuteLl.setVisibility(View.GONE);
        //3. 设置对应的listener
        mHangupLl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopCameraAndFinish();
            }
        });
        mDialingLl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }

    /**
     * 展示邀请列表
     */
    public void showInvitingView() {
        //1. 展示自己的界面
        mLayoutManagerTrtc.setMySelfUserId(mSelfModel.getUserId());
        TRTCVideoLayout videoLayout = addUserToManager(mSelfModel);
        if (videoLayout == null) {
            return;
        }
        videoLayout.setVideoAvailable(true);
        IoTVideoCloud.sharedInstance(this).openCamera(true, videoLayout.getVideoView());
        mHangupLl.setVisibility(View.VISIBLE);
        mHangupLl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopCameraAndFinish();
            }
        });
        mDialingLl.setVisibility(View.GONE);
        mHandsfreeLl.setVisibility(View.VISIBLE);
        mMuteLl.setVisibility(View.VISIBLE);
        //3. 隐藏中间他们也在界面
        hideOtherInvitingUserView();
        //4. sponsor画面也隐藏
        mSponsorGroup.setVisibility(View.GONE);
    }

    /**
     * 展示通话中的界面
     */
    public void showCallingView() {
        //1. 蒙版消失
        mSponsorGroup.setVisibility(View.GONE);
        //2. 底部状态栏
        mHangupLl.setVisibility(View.VISIBLE);
        mDialingLl.setVisibility(View.GONE);
        mHandsfreeLl.setVisibility(View.VISIBLE);
        mMuteLl.setVisibility(View.VISIBLE);

        mHangupLl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopCameraAndFinish();
            }
        });
        showTimeCount();
        hideOtherInvitingUserView();
        mStatusView.setText(R.string.trtccalling_dialed_is_busy_vedio);
        mStatusView.setVisibility(View.INVISIBLE);
    }

    private void showTimeCount() {
        if (mTimeRunnable != null) {
            return;
        }
        mTimeCount = 0;
        mTimeTv.setText(getShowTime(mTimeCount));
        if (mTimeRunnable == null) {
            mTimeRunnable = new Runnable() {
                @Override
                public void run() {
                    mTimeCount++;
                    if (mTimeTv != null) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mTimeTv.setText(getShowTime(mTimeCount));
                            }
                        });
                    }
                    mTimeHandler.postDelayed(mTimeRunnable, 1000);
                }
            };
        }
        mTimeHandler.postDelayed(mTimeRunnable, 1000);
    }

    private void stopTimeCount() {
        mTimeHandler.removeCallbacks(mTimeRunnable);
        mTimeRunnable = null;
    }

    private String getShowTime(int count) {
        return getString(R.string.trtccalling_called_time_format, count / 60, count % 60);
    }

    private void hideOtherInvitingUserView() {
        mInvitingGroup.setVisibility(View.GONE);
    }

    private TRTCVideoLayout addUserToManager(UserInfo userInfo) {
        TRTCVideoLayout layout = mLayoutManagerTrtc.allocCloudVideoView(userInfo.getUserId());
        if (layout == null) {
            return null;
        }
        return layout;
    }
}
