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
Java_com_tencent_iot_video_link_XP2P_startServiceWithPeername(JNIEnv *env, jclass clazz,
                                                              jstring peername) {
    const char *pname = env->GetStringUTFChars(peername, 0);
    LOGI("------------ startServiceWithPeername start------------\n");
    startServiceWithPeername(pname);
    LOGI("------------ startServiceWithPeername end------------");
}

extern "C"
JNIEXPORT jstring JNICALL
Java_com_tencent_iot_video_link_XP2P_delegateHttpFlv(JNIEnv *env, jclass clazz) {
    LOGI("------------ delegateHttpFlv start------------\n");
    return env->NewStringUTF(delegateHttpFlv().c_str());
}

extern "C"
JNIEXPORT void JNICALL
Java_com_tencent_iot_video_link_XP2P_setDeviceInfo(JNIEnv *env, jclass clazz, jstring pro_id, jstring dev_name) {
    const char *id = env->GetStringUTFChars(pro_id, 0);
    const char *name = env->GetStringUTFChars(dev_name, 0);
    LOGI("setDeviceInfo:id %s,name %s\n", id, name);
    int rc = setDeviceInfo(id, name);
    LOGI("------------ setDeviceInfo end:%d------------", rc);
}

extern "C"
JNIEXPORT void JNICALL
Java_com_tencent_iot_video_link_XP2P_setQcloudApiCred(JNIEnv *env, jclass clazz, jstring api_id, jstring api_key) {
    const char *id = env->GetStringUTFChars(api_id, 0);
    const char *key = env->GetStringUTFChars(api_key, 0);
    LOGI("setQcloudApiCred:id %s,key %s\n", id, key);
    int rc = setQcloudApiCred(id, key);
    LOGI("------------ setQcloudApiCred end:%d------------", rc);
}

extern "C"
JNIEXPORT void JNICALL
Java_com_tencent_iot_video_link_XP2P_runSendService(JNIEnv *env, jclass clazz) {
    runSendService();
}

extern "C"
JNIEXPORT void JNICALL
Java_com_tencent_iot_video_link_XP2P_dataSend(JNIEnv *env, jclass clazz, jbyteArray data, jint len) {
    jbyte* dataPtr = env->GetByteArrayElements(data, 0);
    LOGI("dataSend:len %lu\n", len);
    int rc = dataSend((uint8_t *)dataPtr, (size_t)len);
    LOGI("------------ dataSend end:%d------------", rc);
}

extern "C"
JNIEXPORT void JNICALL
Java_com_tencent_iot_video_link_XP2P_tt(JNIEnv *env, jclass clazz, jbyteArray data, jint len) {
    // TODO: implement tt()
}