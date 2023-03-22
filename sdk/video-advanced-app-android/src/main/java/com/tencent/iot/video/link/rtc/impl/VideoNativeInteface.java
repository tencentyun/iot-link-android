package com.tencent.iot.video.link.rtc.impl;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import com.tencent.iot.video.link.rtc.XP2PCallback;
import com.tencent.iot.video.link.rtc.RoomKey;
import com.tencent.rtmp.ui.TXCloudVideoView;
import com.tencent.trtc.TRTCCloud;
import com.tencent.trtc.TRTCCloudDef;
import com.tencent.trtc.TRTCCloudListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static com.tencent.trtc.TRTCCloudDef.TRTC_VIDEO_STREAM_TYPE_BIG;

/**
 * 视频/语音通话的具体实现
 */
public class VideoNativeInteface {
    private static final String TAG = VideoNativeInteface.class.getSimpleName();
    private Context mContext;

    /**
     * 底层SDK调用实例
     */
    private TRTCCloud mRTCCloud;
    private boolean mIsUseFrontCamera;
    private XP2PCallback mXP2PCallback;

    private static VideoNativeInteface instance = null;

    public static synchronized VideoNativeInteface getInstance() {
        if (instance == null) {
            instance = new VideoNativeInteface();
        }
        return instance;
    }

    public void initWithRoomKey(Context context, RoomKey roomKey) {
        this.mContext = context;
        mRTCCloud = TRTCCloud.sharedInstance(mContext);
        mRTCCloud.setListener(mRTCCloudListener);
        enterRTCRoom(roomKey);
    }

    public void setCallback(XP2PCallback mXP2PCallback) {
        this.mXP2PCallback = mXP2PCallback;
    }

    public TRTCCloudListener getmRTCCloudListener() {
        return mRTCCloudListener;
    }

    /**
     * RTC的监听器
     */
    private TRTCCloudListener mRTCCloudListener = new TRTCCloudListener() {
        @Override
        public void onError(int errCode, String errMsg, Bundle extraInfo) {
            Log.e(TAG, "errorCode = " + errCode + ", errMsg: " + errMsg + ", extraInfo: " + extraInfo.toString());
            if (mXP2PCallback != null) {
                mXP2PCallback.onError(errCode, errMsg);
            }
        }

        @Override
        public void onEnterRoom(long result) {
            if (mXP2PCallback != null) {
                mXP2PCallback.onConnect(result);
            }
        }

        @Override
        public void onExitRoom(int reason) {
            if (mXP2PCallback != null) {
                mXP2PCallback.onRelease(reason);
            }
        }

        @Override
        public void onRemoteUserEnterRoom(String userId) {
            if (mXP2PCallback != null) {
                mXP2PCallback.onUserEnter(userId);
            }
        }

        @Override
        public void onRemoteUserLeaveRoom(String userId, int reason) {
            stopRemoteView(userId);
            if (mXP2PCallback != null) {
                mXP2PCallback.onUserLeave(userId);
            }
        }

        @Override
        public void onUserVideoAvailable(String userId, boolean available) {
            if (mXP2PCallback != null) {
                mXP2PCallback.onUserVideoAvailable(userId, available);
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
            if (mXP2PCallback != null) {
                mXP2PCallback.onUserVoiceVolume(volumeMaps);
            }
        }

        @Override
        public void onRecvCustomCmdMsg(String userId, int cmdID, int seq, byte[] message) {
            String msg = new String(message);
            if (mXP2PCallback != null && !msg.isEmpty()) {
                mXP2PCallback.onRecvCustomCmdMsg(userId, msg);
            }
        }

        @Override
        public void onFirstVideoFrame(String userId, int streamType, int width, int height) {
            if (mXP2PCallback != null) {
                mXP2PCallback.onFirstVideoFrame(userId, width, height);
            }
        }
    };

    /**
     * rtc 进房
     */
    public void enterRTCRoom(RoomKey roomKey) {
        if (roomKey == null) return;

        // 进房前需要设置一下关键参数
        TRTCCloudDef.TRTCVideoEncParam encParam = new TRTCCloudDef.TRTCVideoEncParam();
        encParam.videoResolution = TRTCCloudDef.TRTC_VIDEO_RESOLUTION_320_240;
        encParam.videoFps = 15;
        encParam.videoBitrate = 250;
        encParam.videoResolutionMode = TRTCCloudDef.TRTC_VIDEO_RESOLUTION_MODE_PORTRAIT;
        encParam.enableAdjustRes = true;
        mRTCCloud.setVideoEncoderParam(encParam);

        Log.i(TAG, "enterTRTCRoom: " + roomKey.getUserId() + " room:" + roomKey.getStrRoomId());
        TRTCCloudDef.TRTCParams TRTCParams = new TRTCCloudDef.TRTCParams(roomKey.getSdkAppId(),
                roomKey.getUserId(), roomKey.getUserSig(), roomKey.getStrRoomId(), "", "");
        TRTCParams.role = TRTCCloudDef.TRTCRoleAnchor;
        mRTCCloud.enableAudioVolumeEvaluation(300);
        mRTCCloud.setAudioRoute(TRTCCloudDef.TRTC_AUDIO_ROUTE_SPEAKER);
        mRTCCloud.startLocalAudio(TRTCCloudDef.TRTC_AUDIO_QUALITY_SPEECH);
        // 收到来电，开始监听 trtc 的消息
        mRTCCloud.setListener(mRTCCloudListener);
        mRTCCloud.muteLocalAudio(true);
        mRTCCloud.enterRoom(TRTCParams, TRTCCloudDef.TRTC_APP_SCENE_VIDEOCALL);
    }

    /**
     * 断开链接
     */
    public void release() {
        mRTCCloud.stopLocalPreview();
        closeCamera();
        mRTCCloud.stopLocalAudio();
        mRTCCloud.exitRoom();
    }

    public boolean sendMsgToPeer(String msg) {
        if (mRTCCloud != null) {
            return mRTCCloud.sendCustomCmdMsg(1, msg.getBytes(), true, true);
        }
        return false;
    }

    public void openCamera(boolean isFrontCamera, TXCloudVideoView txCloudVideoView) {
        if (txCloudVideoView == null) {
            return;
        }
        mIsUseFrontCamera = isFrontCamera;
        mRTCCloud.muteLocalVideo(0, true);
        mRTCCloud.startLocalPreview(isFrontCamera, txCloudVideoView);
    }

    public void closeCamera() {
        mRTCCloud.stopLocalPreview();
    }

    public void sendStreamToServer() {
        mRTCCloud.muteLocalAudio(false);
        mRTCCloud.muteLocalVideo(0, false);
    }

    public void startRemoteView(String userId, TXCloudVideoView txCloudVideoView) {
        if (txCloudVideoView == null) {
            return;
        }
        mRTCCloud.startRemoteView(userId, TRTC_VIDEO_STREAM_TYPE_BIG, txCloudVideoView);
    }

    private void stopRemoteView(String userId) {
        mRTCCloud.stopRemoteView(userId, TRTC_VIDEO_STREAM_TYPE_BIG);
    }

    public void switchCamera(boolean isFrontCamera) {
        if (mIsUseFrontCamera == isFrontCamera) {
            return;
        }
        mIsUseFrontCamera = isFrontCamera;
        mRTCCloud.switchCamera();
    }

    public void setMicMute(boolean isMute) {
        mRTCCloud.muteLocalAudio(isMute);
    }

    public void setHandsFree(boolean isHandsFree) {
        if (isHandsFree) {
            mRTCCloud.setAudioRoute(TRTCCloudDef.TRTC_AUDIO_ROUTE_SPEAKER);
        } else {
            mRTCCloud.setAudioRoute(TRTCCloudDef.TRTC_AUDIO_ROUTE_EARPIECE);
        }
    }

}
