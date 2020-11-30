package com.tencent.iot.explorer.link;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.tencent.android.tpush.NotificationAction;
import com.tencent.android.tpush.XGPushBaseReceiver;
import com.tencent.android.tpush.XGPushClickedResult;
import com.tencent.android.tpush.XGPushRegisterResult;
import com.tencent.android.tpush.XGPushShowedResult;
import com.tencent.android.tpush.XGPushTextMessage;
import com.tencent.iot.explorer.link.core.auth.callback.MyCallback;
import com.tencent.iot.explorer.link.core.auth.entity.DeviceEntity;
import com.tencent.iot.explorer.link.core.auth.response.BaseResponse;
import com.tencent.iot.explorer.link.core.link.entity.TRTCParamsEntity;
import com.tencent.iot.explorer.link.core.log.L;
import com.tencent.iot.explorer.link.kitlink.activity.HelpWebViewActivity;
import com.tencent.iot.explorer.link.kitlink.consts.CommonField;
import com.tencent.iot.explorer.link.kitlink.util.HttpRequest;
import com.tencent.iot.explorer.trtc.model.RoomKey;
import com.tencent.iot.explorer.trtc.ui.audiocall.TRTCAudioCallActivity;
import com.tencent.iot.explorer.trtc.ui.videocall.TRTCVideoCallActivity;

public class MessageReceiver extends XGPushBaseReceiver {
    private static final String TAG = MessageReceiver.class.getSimpleName();

    public static final String UPDATE_LISTVIEW_ACTION = "com.qq.xgdemo.activity.UPDATE_LISTVIEW";

    private void show(Context context, String text) {
//		Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
    }

    // 通知展示
    @Override
    public void onNotificationShowedResult(Context context,
                                           XGPushShowedResult notifiShowedRlt) {
        if (context == null || notifiShowedRlt == null) {
            return;
        }
        App.Companion.getData().setNotificationId(notifiShowedRlt.getNotifactionId());
        Intent viewIntent = new Intent(UPDATE_LISTVIEW_ACTION);
        context.sendBroadcast(viewIntent);
        show(context, context.getString(R.string.new_msg_be_showed) + notifiShowedRlt.toString());
        Log.d(TAG, context.getString(R.string.new_msg_be_showed) + notifiShowedRlt.toString() + context.getString(R.string.push_channel) + notifiShowedRlt.getPushChannel());
    }

    @Override
    public void onUnregisterResult(Context context, int errorCode) {
        if (context == null) {
            return;
        }
        String text = "";
        if (errorCode == XGPushBaseReceiver.SUCCESS) {
            text = context.getString(R.string.unregister_success);//"反注册成功";
        } else {
            text = context.getString(R.string.unregister_failed) + errorCode; //"反注册失败"
        }
        Log.d(TAG, text);
        show(context, text);

    }

    @Override
    public void onSetTagResult(Context context, int errorCode, String tagName) {
        if (context == null) {
            return;
        }
        String text = "";
        if (errorCode == XGPushBaseReceiver.SUCCESS) {
            text = "\"" + tagName + "\"" + context.getResources().getString(R.string.success_set);
        } else {
            text = "\"" + tagName + "\"" + context.getResources().getString(R.string.failed_set_with_error_code) + errorCode;
        }
        Log.d(TAG, text);
        show(context, text);

    }

    @Override
    public void onDeleteTagResult(Context context, int errorCode, String tagName) {
        if (context == null) {
            return;
        }
        String text = "";
        if (errorCode == XGPushBaseReceiver.SUCCESS) {
            text = "\"" + tagName + "\"" + context.getResources().getString(R.string.success_delete);
        } else {
            text = "\"" + tagName + "\"" + context.getResources().getString(R.string.failed_delete_with_error_code) + errorCode;
        }
        Log.d(TAG, text);
        show(context, text);

    }

    @Override
    public void onSetAccountResult(Context context, int i, String s) {

    }

    @Override
    public void onDeleteAccountResult(Context context, int i, String s) {

    }

    // 通知点击回调 actionType=1为该消息被清除，actionType=0为该消息被点击
    @Override
    public void onNotificationClickedResult(Context context, XGPushClickedResult message) {
        if (context == null || message == null) {
            return;
        }
        String text = "";
        if (message.getActionType() == NotificationAction.clicked.getType()) {
            // 通知在通知栏被点击啦。。。。。
            // APP自己处理点击的相关动作
            // 这个动作可以在activity的onResume也能监听，请看第3点相关内容
            text = context.getString(R.string.notification_opened) + message; // "通知被打开 :"
        } else if (message.getActionType() == NotificationAction.delete.getType()) {
            // 通知被清除啦。。。。
            // APP自己处理通知被清除后的相关动作
            text = context.getString(R.string.notification_removed) + message; // "通知被清除 :"
        }

        // APP自主处理的过程。。。
        Log.d(TAG, text);
        show(context, text);

        checkMsgWithAction(context, message.getCustomContent());
    }

    @Override
    public void onRegisterResult(Context context, int errorCode, XGPushRegisterResult message) {

        if (context == null || message == null) {
            return;
        }
        String text = "";
        if (errorCode == XGPushBaseReceiver.SUCCESS) {
            // 在这里拿token
            String token = message.getToken();
            text = context.getString(R.string.register_success) + token; // "注册成功1. token："
        } else {
            text = message + context.getString(R.string.register_failed) + errorCode; // "注册失败，错误码："
        }
        Log.d(TAG, text);
        show(context, text);
    }

    // 消息透传
    @Override
    public void onTextMessage(Context context, XGPushTextMessage message) {
        String text = context.getString(R.string.recv_msg) + message.toString();//"收到消息:"
        // APP自主处理消息的过程...
        Log.d(TAG, text);
        show(context, text);
        // checkMsgWithAction(context, message.getCustomContent());
    }

    private void checkMsgWithAction(Context context, String msg) {
        if (TextUtils.isEmpty(msg)) {
            return;
        }

        JSONObject msgJson = JSON.parseObject(msg);
        if (msgJson.containsKey(CommonField.MSG_TYPE) &&
                msgJson.getString(CommonField.MSG_TYPE).equals(
                        PushedMessageType.FEEDBACK.getValueStr())) {
            Intent intent = new Intent(App.Companion.getActivity(), HelpWebViewActivity.class);
            App.Companion.getActivity().startActivity(intent);
        } else if (msgJson.containsKey(CommonField.TRTC_AUDIO_CALL_STATUS)) {
            final int videoCallStatus = msgJson.getInteger(CommonField.TRTC_VIDEO_CALL_STATUS);
            final int audioCallStatus = msgJson.getInteger(CommonField.TRTC_AUDIO_CALL_STATUS);
            final String userId = msgJson.getString(CommonField.TRTC_USR_ID);
            // 调用 CallDevice 接口
//            HttpRequest.Companion.getInstance().trtcCallDevice(userId, new MyCallback() {
//                @Override
//                public void fail(String msg, int reqCode) {
//                    L.INSTANCE.e(msg);
//                }
//                @Override
//                public void success(BaseResponse response, int reqCode) {
//                    if (response.isSuccess()) {
//                        JSONObject json = JSON.parseObject(response.getData().toString());
//                        if (json == null || !json.containsKey(CommonField.TRTC_PARAMS)) return;
//                        String data = json.getString(CommonField.TRTC_PARAMS);
//                        if (TextUtils.isEmpty(data)) return;
//
//                        TRTCParamsEntity params = JSON.parseObject(data, TRTCParamsEntity.class);
//                        RoomKey roomKey = new RoomKey();
//                        roomKey.setAppId(params.getSdkAppId());
//                        roomKey.setUserId(params.getUserId());
//                        roomKey.setUserSig(params.getUserSig());
//                        // 根据 callType, 拉起语音或者视频的被呼页面
//                        if (videoCallStatus == 1) {
//                            // 视频通话
//                            TRTCAudioCallActivity.startBeingCall(App.Companion.getActivity(), roomKey, userId);
//                        } else if (audioCallStatus == 1) {
//                            // 语音通话
//                            TRTCAudioCallActivity.startBeingCall(App.Companion.getActivity(), roomKey, userId);
//                        }
//                    }
//                }
//            });
        }
    }
}
