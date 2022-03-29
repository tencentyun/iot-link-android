package com.tencent.iot.explorer.link.kitlink.activity.videoui;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.SurfaceTexture;
import android.os.Handler;
import android.util.Log;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.tencent.iot.explorer.link.T;
import com.tencent.iot.explorer.link.core.log.L;
import com.tencent.iot.explorer.link.core.utils.Utils;
import com.tencent.iot.explorer.link.customview.dialog.PermissionDialog;
import com.tencent.iot.explorer.link.kitlink.activity.BaseActivity;
import com.tencent.iot.explorer.link.R;
import com.tencent.iot.explorer.link.kitlink.consts.CommonField;
import com.tencent.iot.explorer.link.rtc.model.IntentParams;
import com.tencent.iot.explorer.link.rtc.model.RoomKey;
import com.tencent.iot.explorer.link.rtc.model.TRTCCallStatus;
import com.tencent.iot.explorer.link.rtc.model.TRTCCalling;
import com.tencent.iot.explorer.link.rtc.model.TRTCCallingParamsCallback;
import com.tencent.iot.explorer.link.rtc.model.TRTCUIManager;
import com.tencent.iot.explorer.link.rtc.model.UserInfo;
import com.tencent.iot.thirdparty.flv.FLVListener;
import com.tencent.iot.video.link.recorder.CallingType;
import com.tencent.iot.video.link.recorder.OnRecordListener;
import com.tencent.iot.video.link.recorder.VideoRecorder;
import com.tencent.iot.video.link.recorder.opengles.view.CameraView;
import com.tencent.xnet.XP2P;

import java.io.IOException;
import java.util.ArrayList;

import tv.danmaku.ijk.media.player.IjkMediaPlayer;

public class RecordVideoActivity extends BaseActivity implements TextureView.SurfaceTextureListener {
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

    private CameraView cameraView;
    private Button btnSwitch;
    private String path; // 保存源文件的路径
    private Handler handler = new Handler();
    private IjkMediaPlayer player;
    private volatile Surface surface;
    private TextureView playView;
    private final FLVListener flvListener =
            data -> XP2P.dataSend(TRTCUIManager.getInstance().deviceId, data, data.length);
    private final VideoRecorder videoRecorder = new VideoRecorder(flvListener);

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
        cameraView.closeCamera();
        videoRecorder.cancel();
        videoRecorder.stop();
        if (player != null) {
            player.release();
            player = null;
        }
    }

    @Override
    public void initView() {
        path = getFilesDir().getAbsolutePath();
        setContentView(R.layout.activity_record_video);
        cameraView = findViewById(R.id.camera_view);
        btnSwitch = findViewById(R.id.btn_switch);
        videoRecorder.attachCameraView(cameraView);
        playView = findViewById(R.id.v_play);
        playView.setSurfaceTextureListener(this);
        mStatusView = (TextView) findViewById(R.id.tv_status);
        mHangupLl = (LinearLayout) findViewById(R.id.ll_hangup);
        mDialingLl = (LinearLayout) findViewById(R.id.ll_dialing);
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
        btnSwitch.setOnClickListener(v -> cameraView.switchCamera());
    }

    private void stopCameraAndFinish() {
        finish();
        TRTCUIManager.getInstance().isCalling = false;
        TRTCUIManager.getInstance().deviceId = "";
        TRTCUIManager.getInstance().callStatus = TRTCCallStatus.TYPE_IDLE_OR_REFUSE.getValue();
        TRTCUIManager.getInstance().removeCallingParamsCallback();
        TRTCUIManager.getInstance().isP2PCall = false;
    }

    /**
     * app被叫等待接听界面
     */
    public void showWaitingResponseView() {
        if (mIsVideo) { // 需要绘制视频本地和对端画面
            cameraView.openCamera();
        } else { // 需要绘制音频本地和对端画面
            btnSwitch.setVisibility(View.INVISIBLE);
            cameraView.setVisibility(View.INVISIBLE);
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
            cameraView.openCamera();
        } else { // 需要绘制音频本地和对端画面
            btnSwitch.setVisibility(View.INVISIBLE);
            cameraView.setVisibility(View.INVISIBLE);
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
        if (mIsVideo) { // 需要绘制视频本地和对端画面
            play(CallingType.TYPE_VIDEO_CALL);
        } else { // 需要绘制音频本地和对端画面
            cameraView.setVisibility(View.INVISIBLE);
            play(CallingType.TYPE_AUDIO_CALL);
        }
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
    public void onSurfaceTextureUpdated(SurfaceTexture surface) { }

    private void play(int callType) {
        player = new IjkMediaPlayer();
        player.reset();
        player.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "analyzeduration", 1000);
        player.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "packet-buffering", 0);
        player.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "start-on-prepared", 1);
        player.setOption(IjkMediaPlayer.OPT_CATEGORY_CODEC, "threads", 1);
        player.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "sync-av-start", 0);
        player.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec",1);
        player.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec-auto-rotate", 1);
        player.setSurface(surface);
        String url = XP2P.delegateHttpFlv(TRTCUIManager.getInstance().deviceId) + "ipc.flv?action=live";
        Log.e(TAG, "======" + url);
        try {
            player.setDataSource(url);
        } catch (IOException e) {
            e.printStackTrace();
        }
        player.prepareAsync();
        player.start();
        handler.post(() -> startRecord(callType));
    }

    private OnRecordListener onRecordListener = new OnRecordListener() {
        @Override
        public void onRecordStart() { }
        @Override
        public void onRecordTime(long time) { }
        @Override
        public void onRecordComplete(String path) { }
        @Override
        public void onRecordCancel() { }
        @Override
        public void onRecordError(Exception e) { }
    };

    private void startRecord(int callType) {
        videoRecorder.start(callType, onRecordListener);
    }
}