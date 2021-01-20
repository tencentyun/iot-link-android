#include <jni.h>
#include <string>
#include "appWrapper.h"
#include "httpProxy.h"

#include  <android/log.h>
#define  TAG    "XP2P-LOG-TAG"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO,TAG,__VA_ARGS__)
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR,TAG,__VA_ARGS__)


extern "C"
JNIEXPORT jint JNICALL
Java_com_tencent_iot_video_link_XP2P_startServiceWithXp2pInfo(JNIEnv *env, jclass clazz,
                                                              jstring peername) {
    const char *pname = env->GetStringUTFChars(peername, 0);
    LOGI("------------ startServiceWithXp2pInfo Start------------\n");
    return startServiceWithXp2pInfo(pname);
}

extern "C"
JNIEXPORT jstring JNICALL
Java_com_tencent_iot_video_link_XP2P_delegateHttpFlv(JNIEnv *env, jclass clazz) {
    LOGI("------------ DelegateHttpFlv ------------\n");
    return env->NewStringUTF(delegateHttpFlv().c_str());
}

extern "C"
JNIEXPORT jint JNICALL
Java_com_tencent_iot_video_link_XP2P_setDeviceInfo(JNIEnv *env, jclass clazz, jstring pro_id, jstring dev_name) {
    LOGI("------------ SetDeviceInfo Start------------");
    const char *id = env->GetStringUTFChars(pro_id, 0);
    const char *name = env->GetStringUTFChars(dev_name, 0);
    int rc = setDeviceInfo(id, name);
    return rc;
}

extern "C"
JNIEXPORT jint JNICALL
Java_com_tencent_iot_video_link_XP2P_setQcloudApiCred(JNIEnv *env, jclass clazz, jstring api_id, jstring api_key) {
    LOGI("------------ SetQcloudApiCred Start------------");
    const char *id = env->GetStringUTFChars(api_id, 0);
    const char *key = env->GetStringUTFChars(api_key, 0);
    int rc = setQcloudApiCred(id, key);
    return rc;
}

extern "C"
JNIEXPORT void JNICALL
Java_com_tencent_iot_video_link_XP2P_runSendService(JNIEnv *env, jclass clazz) {
    runSendService();
    LOGI("------------ RunSendService End------------");
}

extern "C"
JNIEXPORT jint JNICALL
Java_com_tencent_iot_video_link_XP2P_dataSend(JNIEnv *env, jclass clazz, jbyteArray data, jint len) {
    LOGI("------------ DataSend Start------------");
    jbyte* dataPtr = env->GetByteArrayElements(data, 0);
    int rc = dataSend((uint8_t *)dataPtr, (size_t)len);
    LOGI("------------ DataSend End------------");
    return rc;
}

extern "C"
JNIEXPORT void JNICALL
Java_com_tencent_iot_video_link_XP2P_stopService(JNIEnv *env, jclass clazz) {
    stopService();
    LOGI("------------ StopService End------------");
}

extern "C"
JNIEXPORT jint JNICALL
Java_com_tencent_iot_video_link_XP2P_setXp2pInfoAttributes(JNIEnv *env, jclass clazz, jstring attributes) {
    LOGI("------------ SetXp2pInfoAttributes Start------------");
    const char *_attributes = env->GetStringUTFChars(attributes, 0);
    return setXp2pInfoAttributes(_attributes);
}

extern "C"
JNIEXPORT jint JNICALL
Java_com_tencent_iot_video_link_XP2P_stopSendService(JNIEnv *env, jclass clazz, jbyteArray data) {
    LOGI("------------ StopSendService Start------------");
    return stopSendService(nullptr);
}

extern "C"
JNIEXPORT void JNICALL
Java_com_tencent_iot_video_link_XP2P_setCallback(JNIEnv *env, jclass clazz, jobject callback) {
    LOGI("------------ SetCallback Start------------");
    setJavaCallback(env, callback, "fail");
}