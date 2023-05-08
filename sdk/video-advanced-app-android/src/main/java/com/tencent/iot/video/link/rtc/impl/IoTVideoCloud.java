package com.tencent.iot.video.link.rtc.impl;

import android.app.Activity;
import android.content.Context;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.SurfaceHolder;

import com.tencent.iot.video.link.consts.CameraConstants;
import com.tencent.iot.video.link.rtc.IoTVideoCloudListener;
import com.tencent.iot.video.link.rtc.IoTVideoParams;
import com.tencent.iot.video.link.rtc.RTCParams;
import com.tencent.iot.video.link.util.CameraUtils;
import com.tencent.rtmp.ui.TXCloudVideoView;
import com.tencent.trtc.TRTCCloud;
import com.tencent.trtc.TRTCCloudDef;
import com.tencent.trtc.TRTCCloudListener;
import com.tencent.xnet.XP2P;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.tencent.trtc.TRTCCloudDef.TRTC_GSENSOR_MODE_DISABLE;
import static com.tencent.trtc.TRTCCloudDef.TRTC_GSENSOR_MODE_UIAUTOLAYOUT;
import static com.tencent.trtc.TRTCCloudDef.TRTC_VIDEO_STREAM_TYPE_BIG;

/**
 * 视频/语音通话的具体实现
 */
public class IoTVideoCloud implements com.tencent.xnet.XP2PCallback {
    private static final String TAG = IoTVideoCloud.class.getSimpleName();
    private Context mContext;
    private TRTCCloud mRTCCloud;
    private boolean mIsUseFrontCamera;
    private IoTVideoCloudListener mIoTVideoCloudListener;
    private IoTVideoParams mVideoParams;
    private boolean isP2P = true;

    private static IoTVideoCloud instance = null;

    private Camera camera;
    private SurfaceHolder holder;

    /**
     *  创建 IoTVideoCloud 实例（单例模式）
     *  @param context 仅适用于 Android 平台，用于调用 Androud System API。
     */
    public static synchronized IoTVideoCloud sharedInstance(Context context) {
        if (instance == null) {
            instance = new IoTVideoCloud(context);
        }
        return instance;
    }

    private IoTVideoCloud(Context context) {
        mContext = context;
        mRTCCloud = TRTCCloud.sharedInstance(mContext);
        mRTCCloud.setListener(mRTCCloudListener);
    }

    /**
     *  链接对端
     *  @param params p2p模式 必传参数xp2pinfo、productid、devicename    rtc模式 必传参数RTCParams
     */
    public void startAppWith(IoTVideoParams params) {

        if (params == null) {
            Log.e(TAG, "请在设置params参数。根据参数选择p2p模式/rtc模式");
            return;
        }
        mVideoParams = params;
        if (TextUtils.isEmpty(params.getXp2pInfo())){
            isP2P = false;
        }
        if (isP2P) {
            XP2P.setCallback(this);
            String id = String.format("%s/%s", params.getProductId(), params.getDeviceName());
            XP2P.startService(id, params.getProductId(), params.getDeviceName());
            XP2P.setParamsForXp2pInfo(id, "", "", params.getXp2pInfo());
        } else {
            enterRoom(params.getRtcParams());
        }
    }

    /**
     * rtc 进房
     */
    private void enterRoom(RTCParams RTCParams) {
        if (RTCParams == null) return;

        mVideoParams.setRtcParams(RTCParams);
        // 进房前需要设置一下关键参数
        TRTCCloudDef.TRTCVideoEncParam encParam = new TRTCCloudDef.TRTCVideoEncParam();
        encParam.videoResolution = TRTCCloudDef.TRTC_VIDEO_RESOLUTION_320_240;
        encParam.videoFps = 15;
        encParam.videoBitrate = 250;
        encParam.videoResolutionMode = TRTCCloudDef.TRTC_VIDEO_RESOLUTION_MODE_PORTRAIT;
        encParam.enableAdjustRes = true;
        mRTCCloud.setVideoEncoderParam(encParam);

        Log.i(TAG, "enterRTCRoom: " + RTCParams.getUserId() + " room:" + RTCParams.getStrRoomId());
        TRTCCloudDef.TRTCParams TRTCParams = new TRTCCloudDef.TRTCParams(RTCParams.getSdkAppId(),
                RTCParams.getUserId(), RTCParams.getUserSig(), RTCParams.getStrRoomId(), "", "");
        TRTCParams.role = TRTCCloudDef.TRTCRoleAnchor;
        mRTCCloud.enableAudioVolumeEvaluation(300);
        mRTCCloud.setGSensorMode(TRTC_GSENSOR_MODE_DISABLE);
        mRTCCloud.setAudioRoute(TRTCCloudDef.TRTC_AUDIO_ROUTE_SPEAKER);
        mRTCCloud.startLocalAudio(TRTCCloudDef.TRTC_AUDIO_QUALITY_SPEECH);
        // 收到来电，开始监听 trtc 的消息
        mRTCCloud.setListener(mRTCCloudListener);
        mRTCCloud.muteLocalAudio(true);
        mRTCCloud.enterRoom(TRTCParams, TRTCCloudDef.TRTC_APP_SCENE_VIDEOCALL);
    }

    /**
     *  设置事件回调
     *  @param mIoTVideoCloudListener 获得来自 SDK 的各类事件通知（比如：错误码，警告码，音视频状态参数等）。
     */
    public void setListener(IoTVideoCloudListener mIoTVideoCloudListener) {
        this.mIoTVideoCloudListener = mIoTVideoCloudListener;
    }

    /**
     *  停止推送本地音视频流
     */
    public void stopLocalStream() {
        mRTCCloud.stopLocalPreview();
        mRTCCloud.stopLocalAudio();
    }

    /**
     *  p2p拉流获取对端播放器url， rtc不需要获取播放器url，在内部实现播放。
     *  @param deviceName 拉流设备的名称。
     */
    public String startRemoteStream(String deviceName) {
        if (isP2P) {
            String id = String.format("%s/%s", mVideoParams.getProductId(), mVideoParams.getDeviceName());
            return XP2P.delegateHttpFlv(id);
        }
        return "";
    }

    /**
     *  断开链接
     *  @param deviceName 断开链接设备的名称。
     */
    public void stopAppService(String deviceName) {
        stopLocalStream();
        closeCamera();
        mRTCCloud.exitRoom();
    }

    /**
     *  使用通道发送自定义消息给链接中的设备
     *  @param deviceName 设备的名称。
     *  @param msg 待发送的消息，rtc单个消息的最大长度被限制为 1KB。
     *             p2p 可以为任意格式字符或二进制数据(格式必须为`action=user_define&cmd=xxx`,需要传输的数据跟在`cmd=`后面)，长度由cmd_len提供，建议在16KB以内，否则会影响实时性。
     *  @param timeout_us rtc忽略此参数。 p2p命令超时时间，单位为微秒，值为0时采用默认超时(7500ms左右) 。
     */
    public boolean sendCustomCmdMsg(String deviceName, String msg, long timeout_us) {
        if (mRTCCloud != null) {
            return mRTCCloud.sendCustomCmdMsg(1, msg.getBytes(), true, true);
        }
        return false;
    }

    /**
     *  打开摄像头预览，可提前看到本端画面
     *  @param isFrontCamera true：前置摄像头；false：后置摄像头。
     *  @param txCloudVideoView 承载视频画面的控件。
     */
    public void openCamera(boolean isFrontCamera, TXCloudVideoView txCloudVideoView) {
        if (txCloudVideoView == null) {
            return;
        }
        mIsUseFrontCamera = isFrontCamera;
        mRTCCloud.muteLocalVideo(0, true);
        mRTCCloud.startLocalPreview(isFrontCamera, txCloudVideoView);
    }

    /**
     *  关闭摄像头预览
     */
    public void closeCamera() {
        mRTCCloud.stopLocalPreview();
    }

    /**
     *  开始推送本地音视频流
     *  @param deviceName 要推给某个设备的名称。
     */
    public void startLocalStream(String deviceName) {
        mRTCCloud.muteLocalAudio(false);
        mRTCCloud.muteLocalVideo(0, false);
    }

    /**
     *  开始渲染远端音视频流
     *  @param userId 渲染设备的userid。
     *  @param txCloudVideoView 承载视频画面的控件。
     */
    public void startRemoteView(String userId, TXCloudVideoView txCloudVideoView) {
        if (txCloudVideoView == null) {
            return;
        }
        mRTCCloud.startRemoteView(userId, TRTC_VIDEO_STREAM_TYPE_BIG, txCloudVideoView);
    }

    /**
     *  设置是否适配重力感应
     *  @param enable true：适配重力感应；false：不适配重力感应。
     */
    public void setEnableGSensor(boolean enable) {
        if (enable) {
            mRTCCloud.setGSensorMode(TRTC_GSENSOR_MODE_UIAUTOLAYOUT);
        } else {
            mRTCCloud.setGSensorMode(TRTC_GSENSOR_MODE_DISABLE);
        }
    }

    /**
     *  切换前后摄像头
     *  @param isFrontCamera true：前置摄像头；false：后置摄像头。
     */
    public void changeCameraPositon(boolean isFrontCamera) {
        if (mIsUseFrontCamera == isFrontCamera) {
            return;
        }
        mIsUseFrontCamera = isFrontCamera;
        mRTCCloud.switchCamera();
    }

    /**
     *  暂停/恢复发布本地的音频流
     *  @param mute true：静音；false：恢复。
     */
    public void muteLocalAudio(boolean mute) {
        mRTCCloud.muteLocalAudio(mute);
    }

    /**
     *  是否免提
     *  @param isHandsFree true：免提（扬声器）；false：听筒。
     */
    public void setHandsFree(boolean isHandsFree) {
        if (isHandsFree) {
            mRTCCloud.setAudioRoute(TRTCCloudDef.TRTC_AUDIO_ROUTE_SPEAKER);
        } else {
            mRTCCloud.setAudioRoute(TRTCCloudDef.TRTC_AUDIO_ROUTE_EARPIECE);
        }
    }

    /**
     * RTC的监听器
     */
    private TRTCCloudListener mRTCCloudListener = new TRTCCloudListener() {
        @Override
        public void onError(int errCode, String errMsg, Bundle extraInfo) {
            Log.e(TAG, "errorCode = " + errCode + ", errMsg: " + errMsg + ", extraInfo: " + extraInfo.toString());
            if (mIoTVideoCloudListener != null) {
                mIoTVideoCloudListener.onError(errCode, errMsg);
            }
        }

        @Override
        public void onEnterRoom(long result) {
            if (mIoTVideoCloudListener != null) {
                mIoTVideoCloudListener.onConnect(result);
            }
        }

        @Override
        public void onExitRoom(int reason) {
            if (mIoTVideoCloudListener != null) {
                mIoTVideoCloudListener.onRelease(reason);
            }
        }

        @Override
        public void onRemoteUserEnterRoom(String userId) {
            if (mIoTVideoCloudListener != null) {
                mIoTVideoCloudListener.onUserEnter(userId);
            }
        }

        @Override
        public void onRemoteUserLeaveRoom(String userId, int reason) {
            mRTCCloud.stopRemoteView(userId, TRTC_VIDEO_STREAM_TYPE_BIG);
            if (mIoTVideoCloudListener != null) {
                mIoTVideoCloudListener.onUserLeave(userId);
            }
        }

        @Override
        public void onUserVideoAvailable(String userId, boolean available) {
            if (mIoTVideoCloudListener != null) {
                mIoTVideoCloudListener.onUserVideoAvailable(userId, available);
            }
        }

        @Override
        public void onUserVoiceVolume(ArrayList<TRTCCloudDef.TRTCVolumeInfo> userVolumes, int totalVolume) {
            Map<String, Integer> volumeMaps = new HashMap<>();
            for (TRTCCloudDef.TRTCVolumeInfo info : userVolumes) {
                String userId = "";
                if (info.userId == null) {
                    userId = "";
                } else {
                    userId = info.userId;
                }
                volumeMaps.put(userId, info.volume);
            }
            if (mIoTVideoCloudListener != null) {
                mIoTVideoCloudListener.onUserVoiceVolume(volumeMaps);
            }
        }

        @Override
        public void onRecvCustomCmdMsg(String userId, int cmdID, int seq, byte[] message) {
            String msg = new String(message);
            if (mIoTVideoCloudListener != null && !msg.isEmpty()) {
                mIoTVideoCloudListener.onRecvCustomCmdMsg(userId, msg);
            }
        }

        @Override
        public void onFirstVideoFrame(String userId, int streamType, int width, int height) {
            if (mIoTVideoCloudListener != null) {
                mIoTVideoCloudListener.onFirstVideoFrame(userId, width, height);
            }
        }
    };

    // 默认摄像头方向
    private int facing = CameraConstants.facing.BACK;

    /**
     * 打开相机
     */
    private void openCamera(Activity activity) {
        releaseCamera(camera);
        camera = Camera.open(facing);
        //获取相机参数
        Camera.Parameters parameters = camera.getParameters();
        parameters.setRecordingHint(true);
//        parameters.setPreviewFrameRate(15);  //设置帧率

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
            camera.setDisplayOrientation(CameraUtils.getDisplayOrientation(activity, cameraIndex));
        } catch (Exception e) {
            e.printStackTrace();
        }

        Camera.Size previewSize = getCameraPreviewSize(parameters);
        //设置预览图像分辨率
        parameters.setPreviewSize(320, 240);
        //设置帧率
        parameters.setPreviewFrameRate(15);

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
//                if (startEncodeVideo && videoEncoder != null) {
//                    videoEncoder.encoderH264(data, facing == CameraConstants.facing.FRONT);
//                }
            }
        });
        //调用startPreview()用以更新preview的surface
        camera.startPreview();
    }

    /**
     * 关闭相机
     */
    private void releaseCamera(Camera camera) {
        if (camera != null) {
            camera.setPreviewCallback(null);
            camera.stopPreview();
            camera.release();
            camera = null;
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

    // XP2PCallback

    @Override
    public void fail(String msg, int errorCode) {

    }

    @Override
    public void commandRequest(String id, String msg) {

    }

    @Override
    public void xp2pEventNotify(String id, String msg, int event) {

    }

    @Override
    public void avDataRecvHandle(String id, byte[] data, int len) {

    }

    @Override
    public void avDataCloseHandle(String id, String msg, int errorCode) {

    }

    @Override
    public String onDeviceMsgArrived(String id, byte[] data, int len) {
        return null;
    }
}
