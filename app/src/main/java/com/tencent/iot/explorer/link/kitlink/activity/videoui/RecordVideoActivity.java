package com.tencent.iot.explorer.link.kitlink.activity.videoui;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.media.AudioFormat;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.tencent.iot.explorer.link.App;
import com.tencent.iot.explorer.link.T;
import com.tencent.iot.explorer.link.core.utils.Utils;
import com.tencent.iot.explorer.link.customview.dialog.PermissionDialog;
import com.tencent.iot.explorer.link.kitlink.activity.BaseActivity;
import com.tencent.iot.explorer.link.R;
import com.tencent.iot.explorer.link.kitlink.consts.CommonField;
import com.tencent.iot.explorer.link.kitlink.util.VideoUtils;
import com.tencent.iot.explorer.link.rtc.model.IntentParams;
import com.tencent.iot.explorer.link.rtc.model.RoomKey;
import com.tencent.iot.explorer.link.rtc.model.TRTCCallStatus;
import com.tencent.iot.explorer.link.rtc.model.TRTCCalling;
import com.tencent.iot.explorer.link.rtc.model.TRTCCallingParamsCallback;
import com.tencent.iot.explorer.link.rtc.model.TRTCUIManager;
import com.tencent.iot.explorer.link.rtc.model.UserInfo;
import com.tencent.iot.thirdparty.flv.FLVListener;
import com.tencent.iot.thirdparty.flv.FLVPacker;
import com.tencent.iot.video.link.encoder.AudioEncoder;
import com.tencent.iot.video.link.encoder.VideoEncoder;
import com.tencent.iot.video.link.entity.DeviceStatus;
import com.tencent.iot.video.link.listener.OnEncodeListener;
import com.tencent.iot.video.link.param.AudioEncodeParam;
import com.tencent.iot.video.link.param.MicParam;
import com.tencent.iot.video.link.param.VideoEncodeParam;
import com.tencent.iot.video.link.consts.CallingType;
import com.tencent.iot.video.link.consts.CameraConstants;
import com.tencent.iot.video.link.util.CameraUtils;
import com.tencent.xnet.XP2P;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import tv.danmaku.ijk.media.player.IjkMediaPlayer;

import static com.tencent.iot.video.link.consts.LogConst.RTC_TAG;

public class RecordVideoActivity extends BaseActivity implements TextureView.SurfaceTextureListener, OnEncodeListener, SurfaceHolder.Callback {

    private static Timer bitRateTimer;
    private TextView mStatusView;
    private LinearLayout mHangupLl;
    private LinearLayout mDialingLl;

    public static final int TYPE_BEING_CALLED = 1;
    public static final int TYPE_CALL         = 2;

    public static final String PARAM_TYPE                = "type";
    public static final String PARAM_IS_VIDEO            = "is_video";
    public static final String PARAM_BEINGCALL_USER      = "beingcall_user_model";
    public static final String PARAM_OTHER_INVITING_USER = "other_inviting_user_model";

    private static final String TAG = RecordVideoActivity.class.getSimpleName();

    private SurfaceView surfaceView;
    private SurfaceHolder holder;
    private Camera camera;
    private Button btnSwitch;
    private Handler handler = new Handler();
    private IjkMediaPlayer player;
    private volatile Surface surface;
    private TextureView playView;
    private TextView tvTcpSpeed;
    private TextView tvVCache;
    private TextView tvACache;
    private TextView tvVideoWH;
    private volatile long basePts = 0;
    private long startShowVideoTime = 0L;

    private volatile boolean isPause = true;
    private volatile boolean isRenderView = false;

    private volatile long lastAudioCache5Time = 0L;
    private volatile long lastPlayerSpeed0Time = 0L;

    private final FLVListener flvListener =
            data -> {
//                Log.e(TAG, "===== dataLen:" + data.length);
                if (!isPause) {
                    XP2P.dataSend(TRTCUIManager.getInstance().deviceId, data, data.length);
                }
            };

    private AudioEncoder audioEncoder;
    private VideoEncoder videoEncoder;
    private FLVPacker flvPacker;
    private volatile boolean startEncodeVideo = false;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private TimerTask enterRoomTask = null;

    private int vw = 320;
    private int vh = 240;
    private int mFrameDrop = -1;
    private float mFrameSpeed = 1.5f;

    /**
     * 拨号相关成员变量
     */
    private UserInfo              mSponsorUserInfo;                      // 被叫方
    private int                   mCallType;
    private boolean               mIsVideo;  //是否为视频对话，true为视频 false为音频

    private boolean isFirst = true;

    private PermissionDialog permissionDialog = null;
    private boolean requestCameraPermission = false;
    private boolean requestRecordAudioPermission = false;

    private long lastClickTime = 0L;
    //两次点击间隔不少于1000ms
    private static final int FAST_CLICK_DELAY_TIME = 1000;

    /**
     * 主动拨打给某个用户
     *
     * @param context
     */
    public static void startCallSomeone(Context context, String beingCallUserId, boolean isVideo) {
        Intent starter = new Intent(context, RecordVideoActivity.class);
        starter.putExtra(PARAM_TYPE, TYPE_CALL);
        starter.putExtra(PARAM_IS_VIDEO, isVideo);
        UserInfo beingCallUserInfo = new UserInfo();
        beingCallUserInfo.setUserId(beingCallUserId);
        starter.putExtra(PARAM_BEINGCALL_USER, beingCallUserInfo);
        context.startActivity(starter);
        TRTCUIManager.getInstance().callStatus = TRTCCallStatus.TYPE_CALLING.getValue();
        TRTCUIManager.getInstance().isP2PCall = true;
    }

    /**
     * 作为用户被叫
     *
     * @param context
     * @param beingCallUserId
     */
    public static void startBeingCall(Context context, String beingCallUserId, boolean isVideo) {
        Intent starter = new Intent(context, RecordVideoActivity.class);
        starter.putExtra(PARAM_TYPE, TYPE_BEING_CALLED);
        starter.putExtra(PARAM_IS_VIDEO, isVideo);
        UserInfo beingCallUserInfo = new UserInfo();
        beingCallUserInfo.setUserId(beingCallUserId);
        starter.putExtra(PARAM_BEINGCALL_USER, beingCallUserInfo);
        starter.putExtra(PARAM_OTHER_INVITING_USER, new IntentParams(new ArrayList<UserInfo>()));
        starter.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(starter);
        TRTCUIManager.getInstance().callStatus = TRTCCallStatus.TYPE_CALLING.getValue();
        TRTCUIManager.getInstance().isP2PCall = true;
    }

    @Override
    public int getContentView() {
        return R.layout.activity_record_video;
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregistVideoOverBrodcast();
        removeIsEnterRoom60secondsTask();
        isPause = true;
        stopRecord();
        executor.shutdown();
        XP2P.stopSendService(TRTCUIManager.getInstance().deviceId, null);
        TRTCUIManager.getInstance().deviceId = "";
        releaseCamera(camera);
        if (player != null) {
            mHandler.removeMessages(MSG_UPDATE_HUD);
            player.release();
            player = null;
        }
    }

    @Override
    public void initView() {
        setContentView(R.layout.activity_record_video);
        surfaceView = findViewById(R.id.camera_view);
        holder = surfaceView.getHolder();
        holder.addCallback(this);
        btnSwitch = findViewById(R.id.btn_switch);
        playView = findViewById(R.id.v_play);
        playView.setSurfaceTextureListener(this);
        mStatusView = findViewById(R.id.tv_status);
        mHangupLl = findViewById(R.id.ll_hangup);
        mDialingLl = findViewById(R.id.ll_dialing);
        tvTcpSpeed = findViewById(R.id.tv_tcp_speed);
        tvVCache = findViewById(R.id.tv_v_cache);
        tvACache = findViewById(R.id.tv_a_cache);
        tvVideoWH = findViewById(R.id.tv_v_width_height);

        registVideoOverBrodcast();
        vw = App.Companion.getData().getResolutionWidth();
        vh = App.Companion.getData().getResolutionHeight();
        mFrameDrop = App.Companion.getData().getFrameDrop();
        mFrameSpeed = App.Companion.getData().getFrameSpeed();

        initAudioEncoder();
        initVideoEncoder();

        TRTCUIManager.getInstance().addCallingParamsCallback(new TRTCCallingParamsCallback() {
            @Override
            public void joinRoom(Integer callingType, String deviceId, RoomKey roomKey) {   //设备方接听了电话
                runOnUiThread(() -> {
                    if (isFirst) { //由于android设备端目前上报了两次status=2，所以会回调两次，这里暂时规避下
                        mStatusView.setVisibility(View.GONE);
                        showCallingView();
                        isFirst = false;
                    }
                });
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

            }
        });
        checkAndRequestPermission();
    }

    private void initAudioEncoder() {
        MicParam micParam = new MicParam.Builder()
                .setAudioSource(MediaRecorder.AudioSource.VOICE_COMMUNICATION)
                .setSampleRateInHz(16000) // 采样率
                .setChannelConfig(AudioFormat.CHANNEL_IN_MONO)
                .setAudioFormat(AudioFormat.ENCODING_PCM_16BIT) // PCM
                .build();
        AudioEncodeParam audioEncodeParam = new AudioEncodeParam.Builder().build();
        audioEncoder = new AudioEncoder(micParam, audioEncodeParam,true, true);
        audioEncoder.setOnEncodeListener(this);
    }

    private void initVideoEncoder() {
        VideoEncodeParam videoEncodeParam = new VideoEncodeParam.Builder().setSize(vw, vh)
                                                                          .setFrameRate(App.Companion.getData().getFrameRate())
                                                                          .setBitRate(vw*vh).build();
        videoEncoder = new VideoEncoder(videoEncodeParam);
        videoEncoder.setEncoderListener(this);
    }

    public class AdapterBitRateTask extends TimerTask {
        @Override
        public void run() {
            System.out.println("检测时间到:" +new Date());
            if (isPause) return;


            int bufsize = XP2P.getStreamBufSize(TRTCUIManager.getInstance().deviceId);
//        return String.format(Locale.US, "buf=>%d<=", bufsize);

//            videoEncoder.setVideoBitRate(10000);
//            RecordVideoActivity.this.videoEncoder.setVideoBitRate(10000);

            int p2p_wl_avg = XP2P.getAvgMaxMin(bufsize);

            int now_video_rate = RecordVideoActivity.this.videoEncoder.getVideoBitRate();

            Log.e(TAG,"send_bufsize==" + bufsize + ",now_video_rate==" + now_video_rate + ",avg_index==" + p2p_wl_avg);

            // 降码率
            // 当发现p2p的水线超过一定值时，降低视频码率，这是一个经验值，一般来说要大于 [视频码率/2]
            // 实测设置为 80%视频码率 到 120%视频码率 比较理想
            // 在10组数据中，获取到平均值，并将平均水位与当前码率比对。

            int video_rate_byte = (now_video_rate / 8) * 3 / 4;
            if (p2p_wl_avg > video_rate_byte) {

                videoEncoder.setVideoBitRate(video_rate_byte*8);

            }else if (p2p_wl_avg <  (now_video_rate / 8) / 3) {

                // 升码率
                // 测试发现升码率的速度慢一些效果更好
                // p2p水线经验值一般小于[视频码率/2]，网络良好的情况会小于 [视频码率/3] 甚至更低
                videoEncoder.setVideoBitRate(now_video_rate + (now_video_rate-p2p_wl_avg*8)/5);
            }
        }
    }

    private void startBitRateAdapter() {

        XP2P.resetAvg();
        isPause = false;
        bitRateTimer = new Timer();
        bitRateTimer.schedule(new AdapterBitRateTask(),3000,1000);
    }

    private void stopBitRateAdapter() {
        if (bitRateTimer != null) {
            bitRateTimer.cancel();
            bitRateTimer = null;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 102 || requestCode == 103) {
            permissionDialog.dismiss();
            permissionDialog = null;
            if (!requestCameraPermission || !requestRecordAudioPermission) {
                checkAndRequestPermission();
            } else {
                initData();
            }
        }
    }

    private void checkAndRequestPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED && !requestCameraPermission) {
            // 查看请求camera权限的时间是否大于48小时
            String cameraJsonString = Utils.INSTANCE.getStringValueFromXml(this, CommonField.PERMISSION_CAMERA, CommonField.PERMISSION_CAMERA);
            long lasttime = 0L;
            if (cameraJsonString != null) {
                JSONObject cameraJson = JSON.parseObject(cameraJsonString);
                lasttime = cameraJson.getLong(CommonField.PERMISSION_CAMERA);
            }
            if (cameraJsonString != null && lasttime > 0 && System.currentTimeMillis() / 1000 - lasttime < 48 * 60 * 60) {
                initData();
                if (mSponsorUserInfo == null) {
                    stopCameraAndFinish();
                    return;
                }
                TRTCUIManager.getInstance().refuseEnterRoom(TRTCCalling.TYPE_VIDEO_CALL, mSponsorUserInfo.getUserId());
                stopCameraAndFinish();
                T.show(getString(com.tencent.iot.explorer.link.R.string.permission_of_camera_refuse));
                return;
            }
            if (permissionDialog == null) {
                permissionDialog = new PermissionDialog(this, com.tencent.iot.explorer.link.R.mipmap.permission_camera ,getString(com.tencent.iot.explorer.link.R.string.permission_camera_lips), getString(com.tencent.iot.explorer.link.R.string.permission_camera_trtc));
                permissionDialog.show();
                requestCameraPermission = true;
            }
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 102);

            // 记录请求camera权限的时间
            JSONObject json = new JSONObject();
            json.put(CommonField.PERMISSION_CAMERA, System.currentTimeMillis() / 1000);
            Utils.INSTANCE.setXmlStringValue(this, CommonField.PERMISSION_CAMERA, CommonField.PERMISSION_CAMERA, json.toJSONString());
            return;
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_DENIED && !requestRecordAudioPermission) {
            // 查看请求mic权限的时间是否大于48小时
            String micJsonString = Utils.INSTANCE.getStringValueFromXml(this, CommonField.PERMISSION_MIC, CommonField.PERMISSION_MIC);
            long lasttime = 0L;
            if (micJsonString != null) {
                JSONObject micJson = JSON.parseObject(micJsonString);
                lasttime = micJson.getLong(CommonField.PERMISSION_MIC);
            }
            if (micJsonString != null && lasttime > 0 && System.currentTimeMillis() / 1000 - lasttime < 48 * 60 * 60) {
                boolean calling = initData();
                if (mSponsorUserInfo == null) {
                    stopCameraAndFinish();
                    return;
                }
                TRTCUIManager.getInstance().refuseEnterRoom(TRTCCalling.TYPE_VIDEO_CALL, mSponsorUserInfo.getUserId());
                stopCameraAndFinish();
                T.show(getString(com.tencent.iot.explorer.link.R.string.permission_of_camera_mic_refuse));
                return;
            }
            if (permissionDialog == null) {
                permissionDialog = new PermissionDialog(this, com.tencent.iot.explorer.link.R.mipmap.permission_mic ,getString(com.tencent.iot.explorer.link.R.string.permission_mic_lips), getString(com.tencent.iot.explorer.link.R.string.permission_camera_trtc));
                permissionDialog.show();
                requestRecordAudioPermission = true;
            }
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, 103);

            // 记录请求mic权限的时间
            JSONObject json = new JSONObject();
            json.put(CommonField.PERMISSION_MIC, System.currentTimeMillis() / 1000);
            Utils.INSTANCE.setXmlStringValue(this, CommonField.PERMISSION_MIC, CommonField.PERMISSION_MIC, json.toJSONString());
            return;
        }
        initData();
    }

    private boolean initData() {
        // 初始化从外界获取的数据
        Intent intent = getIntent();
        //自己的资料
        mCallType = intent.getIntExtra(PARAM_TYPE, TYPE_BEING_CALLED);
        mIsVideo = intent.getBooleanExtra(PARAM_IS_VIDEO, true);
        mSponsorUserInfo = (UserInfo) intent.getSerializableExtra(PARAM_BEINGCALL_USER);
        if (mCallType == TYPE_BEING_CALLED) {
            // 作为被叫
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mStatusView.setText(mIsVideo ? com.tencent.iot.explorer.link.rtc.R.string.trtccalling_customer_calling_vedio : com.tencent.iot.explorer.link.rtc.R.string.trtccalling_customer_calling_audio);
                    mStatusView.setVisibility(View.VISIBLE);
                }
            });
            showWaitingResponseView();
            return false;
        } else {
            // 主叫方
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mStatusView.setText(mIsVideo ? com.tencent.iot.explorer.link.rtc.R.string.trtccalling_waiting_to_hear_vedio : com.tencent.iot.explorer.link.rtc.R.string.trtccalling_waiting_to_hear_audio);
                    mStatusView.setVisibility(View.VISIBLE);
                }
            });
            showInvitingView();
            return true;
        }
    }

    @Override
    public void setListener() {
        btnSwitch.setOnClickListener(v -> {
            if (System.currentTimeMillis() - lastClickTime >= FAST_CLICK_DELAY_TIME) {
                switchCamera();
                lastClickTime = System.currentTimeMillis();
            }
        });
    }

    private void stopCameraAndFinish() {
        finish();
        TRTCUIManager.getInstance().isCalling = false;
        TRTCUIManager.getInstance().callStatus = TRTCCallStatus.TYPE_IDLE_OR_REFUSE.getValue();
        TRTCUIManager.getInstance().removeCallingParamsCallback();
        TRTCUIManager.getInstance().isP2PCall = false;
    }

    /**
     * app被叫等待接听界面
     */
    public void showWaitingResponseView() {
        if (mIsVideo) { // 需要绘制视频本地和对端画面
            openCamera();
        } else { // 需要绘制音频本地和对端画面
            btnSwitch.setVisibility(View.INVISIBLE);
            surfaceView.setVisibility(View.INVISIBLE);
        }

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
                TRTCUIManager.getInstance().refuseEnterRoom(mIsVideo?TRTCCalling.TYPE_VIDEO_CALL:TRTCCalling.TYPE_AUDIO_CALL, mSponsorUserInfo.getUserId());
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
                TRTCUIManager.getInstance().didAcceptJoinRoom(mIsVideo?TRTCCalling.TYPE_VIDEO_CALL:TRTCCalling.TYPE_AUDIO_CALL, mSponsorUserInfo.getUserId());
            }
        });
    }

    /**
     * app主动呼叫界面
     */
    public void showInvitingView() {
        if (mIsVideo) { // 需要绘制视频本地和对端画面
            openCamera();
        } else { // 需要绘制音频本地和对端画面
            btnSwitch.setVisibility(View.INVISIBLE);
            surfaceView.setVisibility(View.INVISIBLE);
        }
        //1. 展示自己的界面
        mHangupLl.setVisibility(View.VISIBLE);
        mHangupLl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mSponsorUserInfo == null) {
                    stopCameraAndFinish();
                    return;
                }
                TRTCUIManager.getInstance().refuseEnterRoom(mIsVideo?TRTCCalling.TYPE_VIDEO_CALL:TRTCCalling.TYPE_AUDIO_CALL, mSponsorUserInfo.getUserId());
                stopCameraAndFinish();
            }
        });
        mDialingLl.setVisibility(View.GONE);
    }

    /**
     * 展示通话中的界面
     */
    public void showCallingView() {

        handler.postDelayed(() -> {
            if (mIsVideo) { // 需要绘制视频本地和对端画面
                play(CallingType.TYPE_VIDEO_CALL);
            } else { // 需要绘制音频本地和对端画面
                surfaceView.setVisibility(View.INVISIBLE);
                play(CallingType.TYPE_AUDIO_CALL);
            }
        }, 1000);
        //2. 底部状态栏
        mHangupLl.setVisibility(View.VISIBLE);
        mDialingLl.setVisibility(View.GONE);

        mHangupLl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TRTCUIManager.getInstance().refuseEnterRoom(mIsVideo?TRTCCalling.TYPE_VIDEO_CALL:TRTCCalling.TYPE_AUDIO_CALL, mSponsorUserInfo.getUserId());
                //p2p挂断需要处理
                stopCameraAndFinish();
            }
        });
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        if (surface != null) {
            this.surface = new Surface(surface);
        }
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) { }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) { return false; }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {
        isRenderView = true;
        if (startShowVideoTime > 0L) {
            long showVideoTime = System.currentTimeMillis() - startShowVideoTime;
            startShowVideoTime = 0L;
            Log.i(RTC_TAG, "onSurfaceTextureUpdated, first show video time: " + showVideoTime);
        }
    }

    private void play(int callType) {
        if (player != null) {
            player.stop();
            player.setDisplay(null);
            player.release();
        } else {
            startShowVideoTime = System.currentTimeMillis();
        }
        player = new IjkMediaPlayer();
        player.reset();
        mHandler.sendEmptyMessageDelayed(MSG_UPDATE_HUD, 500);
        if (!mIsVideo) {
            player.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "analyzeduration", 1000);
            player.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "probesize", 64);
        } else {
//            player.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "analyzeduration", 1000000);
            player.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "probesize", 25 * 1024);
        }
        player.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "framedrop", mFrameDrop);
        player.setOption(IjkMediaPlayer.OPT_CATEGORY_CODEC, "skip_frame", 32);//保留关键帧跳帧
        player.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "packet-buffering", 0);
        player.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "start-on-prepared", 1);
        player.setOption(IjkMediaPlayer.OPT_CATEGORY_CODEC, "threads", 1);
        player.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "sync-av-start", 0);
        player.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec",1);
        player.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec-auto-rotate", 1);

        player.setFrameSpeed(mFrameSpeed);
        player.setMaxPacketNum(2);
        player.setSurface(surface);
        String url = XP2P.delegateHttpFlv(TRTCUIManager.getInstance().deviceId) + "ipc.flv?action=live";
        Toast.makeText(this, url, Toast.LENGTH_LONG).show();
        Log.e(TAG, "======" + url);
        try {
            player.setDataSource(url);
        } catch (IOException e) {
            e.printStackTrace();
        }
        player.prepareAsync();
        player.start();
        Log.e(TAG, "*====== player frameDrop: " + mFrameDrop + ", frameSpeed: " + mFrameSpeed);

        // 开始推流
        XP2P.runSendService(TRTCUIManager.getInstance().deviceId, "channel=0", false);
        handler.postDelayed(() -> startRecord(callType), 1000);
    }

    private void startRecord(int callType) {
        if (callType == CallingType.TYPE_VIDEO_CALL) {
            startEncodeVideo = true;
        }
        audioEncoder.start();

        startBitRateAdapter();
    }

    private void stopRecord() {
//        isRenderView = false;
        if (audioEncoder != null) {
            audioEncoder.stop();
        }
        if (videoEncoder != null) {
            videoEncoder.stop();
        }
        startEncodeVideo = false;

        stopBitRateAdapter();
    }

    /**
     * 打开相机
     */
    private void openCamera() {
        releaseCamera(camera);
        camera = Camera.open(facing);
        //获取相机参数
        Camera.Parameters parameters = camera.getParameters();
        parameters.setRecordingHint(true);
        parameters.setPreviewFrameRate(App.Companion.getData().getFrameRate());

        //设置预览格式（也就是每一帧的视频格式）YUV420下的NV21
        parameters.setPreviewFormat(ImageFormat.NV21);

        if (this.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
        }

        int cameraIndex = -1;
        if (facing == CameraConstants.facing.BACK) {
            cameraIndex = Camera.CameraInfo.CAMERA_FACING_BACK;
        } else if (facing == CameraConstants.facing.FRONT) {
            cameraIndex = Camera.CameraInfo.CAMERA_FACING_FRONT;
            camera.setDisplayOrientation(180);
        }

        try {
            camera.setDisplayOrientation(CameraUtils.getDisplayOrientation(this, cameraIndex));
        } catch (Exception e) {
            e.printStackTrace();
        }


        Camera.Size previewSize = getCameraPreviewSize(parameters);
        //设置预览图像分辨率
        parameters.setPreviewSize(vw, vh);
        //设置帧率
        parameters.setPreviewFrameRate(App.Companion.getData().getFrameRate());

        //配置camera参数
        camera.setParameters(parameters);
        try {
            camera.setPreviewDisplay(holder);
        } catch (IOException e) {
            e.printStackTrace();
        }
        //设置监听获取视频流的每一帧
        camera.setPreviewCallback(new Camera.PreviewCallback() {
            @Override
            public void onPreviewFrame(byte[] data, Camera camera) {
                if (startEncodeVideo && videoEncoder != null) {
                    videoEncoder.encoderH264(data, facing == CameraConstants.facing.FRONT);
                }
            }
        });
        //调用startPreview()用以更新preview的surface
        camera.startPreview();
    }

    private void checkoutIsEnterRoom60seconds(String message) {
        if (enterRoomTask == null) {
            enterRoomTask = new TimerTask(){
                public void run(){
                    //60秒重连失败退出，进入了就取消timertask
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
                            Log.e(TAG, "*========stop send video data over 60");
                            TRTCUIManager.getInstance().refuseEnterRoom(mIsVideo?TRTCCalling.TYPE_VIDEO_CALL:TRTCCalling.TYPE_AUDIO_CALL, mSponsorUserInfo.getUserId());
                            stopCameraAndFinish();
                        }
                    });
                }
            };
            Timer timer = new Timer();
            timer.schedule(enterRoomTask, 60000);
        }
    }

    private void removeIsEnterRoom60secondsTask() {
        if (enterRoomTask != null) {
            enterRoomTask.cancel();
            enterRoomTask = null;
        }
    }

    /**
     * 获取设备支持的最大分辨率
     */
    private Camera.Size getCameraPreviewSize(Camera.Parameters parameters) {
        List<Camera.Size> list = parameters.getSupportedPreviewSizes();
        Camera.Size needSize = null;
        for (Camera.Size size : list) {
            Log.e(TAG, "****========== " + size.width + " " + size.height);
            if (needSize == null) {
                needSize = size;
                continue;
            }
            if (size.width >= needSize.width) {
                if (size.height > needSize.height) {
                    needSize = size;
                }
            }
        }
        return needSize;
    }

    // 默认摄像头方向
    private int facing = CameraConstants.facing.BACK;

    private void switchCamera() {
        if (facing == CameraConstants.facing.BACK) {
            facing = CameraConstants.facing.FRONT;
        } else {
            facing = CameraConstants.facing.BACK;
        }
        openCamera();
    }

    /**
     * 关闭相机
     */
    public void releaseCamera(Camera camera) {
        if (camera != null) {
            camera.setPreviewCallback(null);
            camera.stopPreview();
            camera.release();
            camera = null;
        }
    }

    public void updateDashboard() {
        long videoCachedDuration = player.getVideoCachedDuration();
        long audioCachedDuration = player.getAudioCachedDuration();
        long videoCachedBytes = player.getVideoCachedBytes();
        long audioCachedBytes = player.getAudioCachedBytes(); // (long)Math.random()*10;//
        long tcpSpeed = player.getTcpSpeed();

        float vdps = player.getVideoDecodeFramesPerSecond();
        float vfps = player.getVideoOutputFramesPerSecond();

        if (audioCachedDuration > 5000) { //当前有大于5s的音频缓存
            Log.e(RTC_TAG, "audioCachedDuration:-----" + audioCachedDuration);
            if (lastAudioCache5Time == 0L) { //之前有清空过lastAudioCache5Time时间，首出图后，再记录一下当前的时间
                if (isRenderView) {
                    lastAudioCache5Time = System.currentTimeMillis();
                }
            } else { //前一次更新时没有清空过lastAudioCache5Time时间，计算并打印一下持续的时间。
                long tempTime1 = System.currentTimeMillis() - lastAudioCache5Time;
                if (tempTime1 > 999) { //当前有大于5s的音频缓存,已经持续了20s，算作网络不好重新reset P2P服务
                    Log.e(RTC_TAG, "lastAudioCache5Time:" + lastAudioCache5Time + ", audioCachedDuration" + audioCachedDuration + ", tempTime" + tempTime1 + ", need to reset P2P");
                    Toast.makeText(getApplicationContext(), "a_cache触发清缓存", Toast.LENGTH_LONG).show();
                    lastAudioCache5Time = 0;
                    if (player != null) {
                        player.flushCache();
                    }
                }
            }
        } else { //小于等于5s的音频缓存，清空一下记录的时间
            lastAudioCache5Time = 0;
            Log.e(RTC_TAG, "lastAudioCache5Time:" + lastAudioCache5Time + ", audioCachedDuration" + audioCachedDuration + ", reset 0");
        }

        if (tcpSpeed == 0) {//没有速度了，可能是播放器没有数据了
            if (lastPlayerSpeed0Time == 0) { //记录一下首次没有速度的时间
                if (isRenderView) {
                    lastPlayerSpeed0Time = System.currentTimeMillis();
                }
            } else { //前一次更新时没有清空过lastPlayerSpeed0Time时间，计算并打印一下持续的时间。
                long tempTime2 = System.currentTimeMillis() - lastPlayerSpeed0Time;
                if (tempTime2 > 5*1000) { //当前播放器没有速度，已经持续了20s，算作网络不好重新reset P2P服务
                    Log.e(RTC_TAG, "lastPlayerSpeed0Time:" + lastPlayerSpeed0Time + ", tcpSpeed" + tcpSpeed + ", tempTime" + tempTime2 + ", need to reset P2P");
                    Toast.makeText(getApplicationContext(), "tcpSpeed触发重连", Toast.LENGTH_LONG).show();
                    lastPlayerSpeed0Time = 0;
                    isPause = true;
                    flvPacker = null;
                    stopRecord();
                    VideoUtils.sendNeedResetP2PBroadcast(App.Companion.getActivity(), 100);
                }
            }
        } else {
            lastPlayerSpeed0Time = 0;
            Log.e(RTC_TAG, "lastPlayerSpeed0Time:" + lastPlayerSpeed0Time + ", tcpSpeed" + tcpSpeed + ", reset 0");
        }

        tvACache.setText(String.format(Locale.US, "%s, %s",
                Utils.INSTANCE.formatedDurationMilli(audioCachedDuration),
                Utils.INSTANCE.formatedSize(audioCachedBytes)));
        tvVCache.setText(String.format(Locale.US, "%s, %s",
                Utils.INSTANCE.formatedDurationMilli(videoCachedDuration),
                Utils.INSTANCE.formatedSize(videoCachedBytes)));
        tvTcpSpeed.setText(String.format(Locale.US, "%s",
                Utils.INSTANCE.formatedSpeed(tcpSpeed, 1000)));
        tvVideoWH.setText(player.getVideoWidth() + " x " + player.getVideoHeight());
        Log.i(RTC_TAG, String.format(Locale.US, "player fps : %.2f / %.2f", vdps, vfps));
    }

    private static final int MSG_UPDATE_HUD = 1;

    BroadcastReceiver recevier = new BroadcastReceiver() {
        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        @Override
        public void onReceive(Context context, Intent intent) {
            int refreshTag = intent.getIntExtra(VideoUtils.VIDEO_RESUME, 0);

            Log.d(TAG, "refreshTag: " + refreshTag);
            if (refreshTag == 2) {//p2p链路断开2
                isPause = true;
                flvPacker = null;
                stopRecord();
//                XP2P.stopSendService(TRTCUIManager.getInstance().deviceId, null);
                checkoutIsEnterRoom60seconds("通话结束...");
            }
            if (refreshTag == 1) { //p2p连接成功1

                initAudioEncoder();
                initVideoEncoder();

                if (mIsVideo) { // 需要绘制视频本地和对端画面
                    play(CallingType.TYPE_VIDEO_CALL);
                } else { // 需要绘制音频本地和对端画面
                    surfaceView.setVisibility(View.INVISIBLE);
                    play(CallingType.TYPE_AUDIO_CALL);
                }
                removeIsEnterRoom60secondsTask();
            }
        }
    };

    private void registVideoOverBrodcast() {
        Log.e(TAG, "registVideoOverBrodcast");
        LocalBroadcastManager broadcastManager = LocalBroadcastManager.getInstance(RecordVideoActivity.this);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.intent.action.CART_BROADCAST");
        broadcastManager.registerReceiver(recevier, intentFilter);
    }

    private void unregistVideoOverBrodcast() {
        Log.e(TAG, "unregistVideoOverBrodcast");
        LocalBroadcastManager broadcastManager = LocalBroadcastManager.getInstance(RecordVideoActivity.this);
        broadcastManager.unregisterReceiver(recevier);
    }

    @Override
    public void onAudioEncoded(byte[] datas, long pts, long seq) {
        if (executor.isShutdown()) return;
        executor.submit(() -> {
            if (!isPause) {
                if (flvPacker == null) {
                    flvPacker = new FLVPacker(flvListener, true, true);
                    basePts = pts;
                }
                flvPacker.encodeFlv(datas, FLVPacker.TYPE_AUDIO, pts - basePts);
            }
        });
    }

    @Override
    public void onVideoEncoded(byte[] datas, long pts, long seq) {
        if (executor.isShutdown()) return;
        executor.submit(() -> {
            if (!isPause) {
                if (flvPacker == null) {
                    flvPacker = new FLVPacker(flvListener, true, true);
                    basePts = pts;
                }
                flvPacker.encodeFlv(datas, FLVPacker.TYPE_VIDEO, pts - basePts);
            }
        });
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        openCamera();
        Log.d(TAG, "surface created.");
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        Log.d(TAG, "surface changed.");
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        Log.d(TAG, "surface destroyed.");
    }

    private static class MyHandler extends Handler {
        private final WeakReference<RecordVideoActivity> mActivity;

        public MyHandler(RecordVideoActivity mActivity) {
            this.mActivity = new WeakReference<>(mActivity);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            RecordVideoActivity activity = mActivity.get();
            if (activity != null) {
                if (msg.what == MSG_UPDATE_HUD) {
                    activity.updateDashboard();
                    removeMessages(MSG_UPDATE_HUD);
                    sendEmptyMessageDelayed(MSG_UPDATE_HUD, 500);
                }
            }
        }
    }
    private final Handler mHandler = new MyHandler(this);
}