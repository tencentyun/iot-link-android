package com.tencent.iot.video.link.rtc.impl;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import com.tencent.iot.video.link.rtc.RTCCalling;
import com.tencent.iot.video.link.rtc.RTCCallingDelegate;
import com.tencent.iot.video.link.rtc.RoomKey;
import com.tencent.liteav.beauty.TXBeautyManager;
import com.tencent.rtmp.ui.TXCloudVideoView;
import com.tencent.trtc.TRTCCloud;
import com.tencent.trtc.TRTCCloudDef;
import com.tencent.trtc.TRTCCloudListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * 视频/语音通话的具体实现
 */
public class RTCCallingImpl {
    private static final String TAG            = RTCCallingImpl.class.getSimpleName();
    /**
     * 超时时间，单位秒
     */
    public static final  int    TIME_OUT_COUNT = 30;
    private final Context mContext;

    /**
     * 底层SDK调用实例
     */
    private TRTCCloud mRTCCloud;
    private boolean mIsUseFrontCamera;
    private RTCCallingDelegate mRTCCallingDelegate;

    public void setTRTCCallingDelegate(RTCCallingDelegate mRTCCallingDelegate) {
        this.mRTCCallingDelegate = mRTCCallingDelegate;
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
            if (mRTCCallingDelegate != null) {
                mRTCCallingDelegate.onError(errCode, errMsg);
            }
        }

        @Override
        public void onEnterRoom(long result) {
            if (mRTCCallingDelegate != null) {
                mRTCCallingDelegate.onEnterRoom(result);
            }
        }

        @Override
        public void onExitRoom(int reason) {
            if (mRTCCallingDelegate != null) {
                mRTCCallingDelegate.onExitRoom(reason);
            }
        }

        @Override
        public void onRemoteUserEnterRoom(String userId) {
            if (mRTCCallingDelegate != null) {
                mRTCCallingDelegate.onUserEnter(userId);
            }
        }

        @Override
        public void onRemoteUserLeaveRoom(String userId, int reason) {
            if (mRTCCallingDelegate != null) {
                mRTCCallingDelegate.onUserLeave(userId);
            }
        }

        @Override
        public void onUserVideoAvailable(String userId, boolean available) {
            if (mRTCCallingDelegate != null) {
                mRTCCallingDelegate.onUserVideoAvailable(userId, available);
            }
        }

        @Override
        public void onUserAudioAvailable(String userId, boolean available) {
            if (mRTCCallingDelegate != null) {
                mRTCCallingDelegate.onUserAudioAvailable(userId, available);
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
            if (mRTCCallingDelegate != null) {
                mRTCCallingDelegate.onUserVoiceVolume(volumeMaps);
            }
        }

        @Override
        public void onRecvCustomCmdMsg(String userId, int cmdID, int seq, byte[] message) {
            if (mRTCCallingDelegate != null) {
                mRTCCallingDelegate.onRecvCustomCmdMsg(userId, cmdID, seq, message);
            }
        }

        @Override
        public void onFirstVideoFrame(String userId, int streamType, int width, int height) {
            if (mRTCCallingDelegate != null) {
                mRTCCallingDelegate.onFirstVideoFrame(userId, streamType, width, height);
            }
        }
    };


    public RTCCallingImpl(Context context) {
        mContext = context;
        mRTCCloud = TRTCCloud.sharedInstance(context);
        mRTCCloud.setListener(mRTCCloudListener);
    }

    public void destroy() {
        //必要的清除逻辑
        mRTCCloud.stopLocalPreview();
        mRTCCloud.stopLocalAudio();
        mRTCCloud.exitRoom();
    }

    /**
     * trtc 退房
     */
    public void exitRoom() {
        mRTCCloud.stopLocalPreview();
        mRTCCloud.stopLocalAudio();
        mRTCCloud.exitRoom();
    }

    /**
     * rtc 进房
     */
    public void enterRTCRoom(RoomKey roomKey) {
        if (roomKey == null) return;

        if (roomKey.getCallType() == RTCCalling.TYPE_VIDEO_CALL) {
            // 开启基础美颜
            TXBeautyManager txBeautyManager = mRTCCloud.getBeautyManager();
            // 自然美颜
            txBeautyManager.setBeautyStyle(1);
            txBeautyManager.setBeautyLevel(6);
            // 进房前需要设置一下关键参数
            TRTCCloudDef.TRTCVideoEncParam encParam = new TRTCCloudDef.TRTCVideoEncParam();
            encParam.videoResolution = TRTCCloudDef.TRTC_VIDEO_RESOLUTION_320_240;
            encParam.videoFps = 15;
            encParam.videoBitrate = 250;
            encParam.videoResolutionMode = TRTCCloudDef.TRTC_VIDEO_RESOLUTION_MODE_PORTRAIT;
            encParam.enableAdjustRes = true;
            mRTCCloud.setVideoEncoderParam(encParam);
        }
        Log.i(TAG, "enterTRTCRoom: " + roomKey.getUserId() + " room:" + roomKey.getStrRoomId());
        TRTCCloudDef.TRTCParams TRTCParams = new TRTCCloudDef.TRTCParams(roomKey.getSdkAppId(),
                roomKey.getUserId(), roomKey.getUserSig(), roomKey.getStrRoomId(), "", "");
        TRTCParams.role = TRTCCloudDef.TRTCRoleAnchor;
        mRTCCloud.enableAudioVolumeEvaluation(300);
        mRTCCloud.setAudioRoute(TRTCCloudDef.TRTC_AUDIO_ROUTE_SPEAKER);
        mRTCCloud.startLocalAudio(TRTCCloudDef.TRTC_AUDIO_QUALITY_MUSIC);
        // 收到来电，开始监听 trtc 的消息
        mRTCCloud.setListener(mRTCCloudListener);
        mRTCCloud.muteLocalAudio(true);
        mRTCCloud.enterRoom(TRTCParams, TRTCCloudDef.TRTC_APP_SCENE_VIDEOCALL);
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

    public void startRemoteView(String userId, TXCloudVideoView txCloudVideoView) {
        if (txCloudVideoView == null) {
            return;
        }
        mRTCCloud.startRemoteView(userId, txCloudVideoView);
        mRTCCloud.muteLocalAudio(false);
        mRTCCloud.muteLocalVideo(0, false);
    }
    public void muteLocalVideo() {
        mRTCCloud.muteLocalAudio(false);
        mRTCCloud.muteLocalVideo(0, false);
    }

    public void stopRemoteView(String userId) {
        mRTCCloud.stopRemoteView(userId);
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
