#include <jni.h>
#include <string>
#include <android/log.h>
#include <flv-writer.h>
#include <flv-muxer.h>

#define TAG "VIDEO-LINK-ANDROID"
#define LOGD(...)  __android_log_print(ANDROID_LOG_DEBUG, TAG, __VA_ARGS__)
#define LOGE(...)  __android_log_print(ANDROID_LOG_ERROR, TAG, __VA_ARGS__)


static flv_muxer_t* muxer = nullptr;

static int64_t sg_pts = 0;

static int flv_onmuxer(void* flv, int type, const void* data, size_t bytes, uint32_t timestamp)
{
    LOGD("========= flv_onmuxer bytes: %d", bytes);
    return flv_writer_input(flv, type, data, bytes, timestamp);
}

static int flv_onwrite(void *param, const struct flv_vec_t* vec, int n)
{
//    int i;
//    for(i = 0; i < n; i++)
//    {
//        if (vec[i].len != (int)fwrite(vec[i].ptr, 1, vec[i].len, (FILE*)param))
//            return ferror((FILE*)param);
//    }
//    return 0;
    LOGD("========= bytes: %d, vec num:%d", vec->len, n);
    return 0;
}

extern "C"
JNIEXPORT jint JNICALL
Java_com_tencent_iot_video_link_util_audio_FLVPacker_init(JNIEnv *env, jobject thiz) {
    LOGD("============ FLVPacker_init");
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

    LOGD("===========================------------ %d, pts: %d", len, pts);

    int ret = flv_muxer_aac(muxer, c_data, len, pts, pts);
    env->ReleaseByteArrayElements(aac, data, 0);
    return ret;
}