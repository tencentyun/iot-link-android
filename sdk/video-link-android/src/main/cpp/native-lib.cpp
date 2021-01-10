#include <jni.h>
#include <string>
#include "appWrapper.h"
#include "httpProxy.h"

#include  <android/log.h>
#define  TAG    "ARCHURLOG"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO,TAG,__VA_ARGS__)
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR,TAG,__VA_ARGS__)


extern "C"
JNIEXPORT void JNICALL
Java_com_tencent_iot_video_link_XP2P_startServiceWithPeername(JNIEnv *env, jobject thiz,
                                                              jstring peername) {
    const char *pname = env->GetStringUTFChars(peername, 0);
    LOGI("------------ startServiceWithPeername start------------\n");
    startServiceWithPeername(pname);
    LOGI("------------ startServiceWithPeername end------------");
}

extern "C"
JNIEXPORT jstring JNICALL
Java_com_tencent_iot_video_link_XP2P_delegateHttpFlv(JNIEnv *env, jobject thiz) {
    LOGI("------------ delegateHttpFlv start------------\n");
    return env->NewStringUTF(delegateHttpFlv().c_str());
}