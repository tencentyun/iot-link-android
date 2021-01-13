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
Java_com_tencent_iot_video_link_XP2P_startServiceWithXp2pInfo(JNIEnv *env, jclass clazz,
                                                              jstring peername) {
    const char *pname = env->GetStringUTFChars(peername, 0);
    LOGI("------------ startServiceWithXp2pInfo Start------------\n");
    startServiceWithXp2pInfo(pname);
    LOGI("------------ startServiceWithXp2pInfo End------------");
}

extern "C"
JNIEXPORT jstring JNICALL
Java_com_tencent_iot_video_link_XP2P_delegateHttpFlv(JNIEnv *env, jclass clazz) {
    LOGI("------------ DelegateHttpFlv ------------\n");
    return env->NewStringUTF(delegateHttpFlv().c_str());
}

extern "C"
JNIEXPORT void JNICALL
Java_com_tencent_iot_video_link_XP2P_setDeviceInfo(JNIEnv *env, jclass clazz, jstring pro_id, jstring dev_name) {
    const char *id = env->GetStringUTFChars(pro_id, 0);
    const char *name = env->GetStringUTFChars(dev_name, 0);
    int rc = setDeviceInfo(id, name);
    LOGI("------------ SetDeviceInfo End:%d------------", rc);
}

extern "C"
JNIEXPORT void JNICALL
Java_com_tencent_iot_video_link_XP2P_setQcloudApiCred(JNIEnv *env, jclass clazz, jstring api_id, jstring api_key) {
    const char *id = env->GetStringUTFChars(api_id, 0);
    const char *key = env->GetStringUTFChars(api_key, 0);
    int rc = setQcloudApiCred(id, key);
    LOGI("------------ SetQcloudApiCred End:%d------------", rc);
}

extern "C"
JNIEXPORT void JNICALL
Java_com_tencent_iot_video_link_XP2P_runSendService(JNIEnv *env, jclass clazz) {
    runSendService();
    LOGI("------------ RunSendService End------------");
}

extern "C"
JNIEXPORT void JNICALL
Java_com_tencent_iot_video_link_XP2P_dataSend(JNIEnv *env, jclass clazz, jbyteArray data, jint len) {
    jbyte* dataPtr = env->GetByteArrayElements(data, 0);
    int rc = dataSend((uint8_t *)dataPtr, (size_t)len);
    LOGI("------------ DataSend End:%d------------", rc);
}

extern "C"
JNIEXPORT void JNICALL
Java_com_tencent_iot_video_link_XP2P_stopService(JNIEnv *env, jclass clazz) {
    stopService();
    LOGI("------------ StopService End------------");
}

extern "C"
JNIEXPORT void JNICALL
Java_com_tencent_iot_video_link_XP2P_setXp2pInfoAttributes(JNIEnv *env, jclass clazz, jstring attributes) {
    const char *_attributes = env->GetStringUTFChars(attributes, 0);
    setXp2pInfoAttributes(_attributes);
    LOGI("------------ SetXp2pInfoAttributes End------------");
}

extern "C"
JNIEXPORT void JNICALL
Java_com_tencent_iot_video_link_XP2P_stopSendService(JNIEnv *env, jclass clazz, jbyteArray data) {
    stopSendService(nullptr);
    LOGI("------------ StopSendService End------------");
}