#include <jni.h>
#include <string>
#include <android/log.h>
#include <flv-writer.h>
#include <flv-muxer.h>

#define TAG "VIDEO-LINK-ANDROID"
#define LOGD(...)  __android_log_print(ANDROID_LOG_DEBUG, TAG, __VA_ARGS__)
#define LOGE(...)  __android_log_print(ANDROID_LOG_ERROR, TAG, __VA_ARGS__)


JavaVM* javaVm = nullptr;
static jobject listener = nullptr;
static flv_muxer_t* muxer = nullptr;
static int64_t sg_pts = 0;

static int flv_onmuxer(void* flv, int type, const void* data, size_t bytes, uint32_t timestamp) {
    return flv_writer_input(flv, type, data, bytes, timestamp);
}

static int flv_onwrite(void *param, const struct flv_vec_t* vec, int n) {
    int total_size = 0;
    for(int i = 0; i < n; i++) {
        total_size += vec[i].len;
    }
    char* bytes = new char[total_size];
    for(int i = 0, offset = 0; i < n; i++) {
        memcpy(bytes + offset, vec[i].ptr, vec[i].len);
        offset += vec[i].len;
    }

    JNIEnv* env = nullptr;
    int attachResult = ::javaVm->AttachCurrentThread(&env, nullptr);
    if (attachResult != JNI_OK) {
        LOGE("attach current thread error!");
        return 0;
    }

    jbyteArray jbytes = env->NewByteArray(total_size);
    env->SetByteArrayRegion(jbytes, 0, total_size, reinterpret_cast<const jbyte*>(bytes));
    jclass listenerCls = env->GetObjectClass(listener);
    jmethodID mid = env->GetMethodID(listenerCls, "onFLV", "([B)V");
    env->CallVoidMethod(listener, mid, jbytes);
    delete[] bytes;

    return 0;
}

extern "C"
JNIEXPORT jint JNICALL
Java_com_tencent_iot_video_link_util_audio_FLVPacker_init(JNIEnv *env, jobject thiz) {
    void* w = flv_writer_create2(1, 0, flv_onwrite, nullptr);
    muxer = flv_muxer_create(flv_onmuxer, w);
    return JNI_OK;
}

extern "C"
JNIEXPORT jint JNICALL
Java_com_tencent_iot_video_link_util_audio_FLVPacker_aac2Flv(JNIEnv *env, jobject thiz,
                                                              jbyteArray aac, jlong timestamp) {
    unsigned char* c_data;
    jsize len = env->GetArrayLength(aac);
    jbyte* data = env->GetByteArrayElements(aac, JNI_FALSE);
    if (len > 0)
    {
        c_data = new unsigned char[len];
        for (jint i = 0; i < len; i++)
        {
            c_data[i] = (unsigned char)data[i];
        }
    }
    if (muxer == nullptr) {
        LOGE("Please init flv muxer first.");
        return -1;
    }

    int pts;
    if (sg_pts == 0) {
        pts = 0;
    } else {
        pts = timestamp - sg_pts;
    }
    sg_pts = timestamp;

    int ret = flv_muxer_aac(muxer, c_data, len, pts, pts);
    env->ReleaseByteArrayElements(aac, data, 0);
    return ret;
}

JNIEXPORT jint JNI_OnLoad(JavaVM* vm, void* reserved) {
    ::javaVm = vm;
    JNIEnv* jniEnv = nullptr;
    int result = javaVm->GetEnv(reinterpret_cast<void **>(&jniEnv), JNI_VERSION_1_6);
    if (result != JNI_OK) {
        return -1;
    }
    LOGD("JNI OnLoad");
    return JNI_VERSION_1_6;
}

JNIEXPORT void JNI_OnUnload(JavaVM* vm, void* reserved){
    if (listener != nullptr) {

    }
    LOGD("JNI OnUnload");
}

extern "C"
JNIEXPORT void JNICALL
Java_com_tencent_iot_video_link_util_audio_FLVPacker_setOnMuxerListener(JNIEnv *env, jobject thiz,
                                                                        jobject _listener) {
    if (_listener != nullptr) {
        LOGE("_listener != nullptr");
        listener = env->NewGlobalRef(_listener);
    }
}