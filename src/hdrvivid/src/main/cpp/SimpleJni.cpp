/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2022-2022. All rights reserved.
 * Description: jni interface
 * Create: 2022-05-05
 */

#include <jni.h>
#include <string>
#include <android/native_window_jni.h>
#include "HdrVividKits.h"
#include "SimpleCommon.h"
#include "SimpleProcessor.h"
#include "SimpleExtractor.h"
#include "SimpleLog.h"

JavaVM *g_jvm = nullptr;
static jmethodID g_onUpdateTexImageCallback4NativeMethodId = nullptr;
static jmethodID g_onReceiveFrameCallback = nullptr;

static jobject g_simpleJniObj = nullptr;
static SimpleProcessor *g_simpleProcessor = nullptr;
static SimplePlayer *g_simplePlayer = nullptr;

void OnUpdateTexImageCallback(JNIEnv *env, void *context, HdrVividRender *render, HdrVividTexImageInfo *texImageInfo)
{
    int64_t ptsUs = env->CallLongMethod(g_simpleJniObj, g_onUpdateTexImageCallback4NativeMethodId) / MS_2_US;

    if (context != nullptr) {
        auto *simpleProcessor = static_cast<SimpleProcessor *>(context);
        simpleProcessor->SetStaticMetaData();
        simpleProcessor->SetDynamicMetaData(ptsUs);
        texImageInfo->ptsUs = ptsUs;
        texImageInfo->flags = 0;
    } else {
        LOGW("%s, context is nullptr, ptsUs=%" PRId64, __FUNCTION__, ptsUs);
    }
}

void OnReceiveFrameCallback(JNIEnv *jniEnv, int64_t ptsUs)
{
    if (jniEnv != nullptr) {
        jniEnv->CallVoidMethod(g_simpleJniObj, g_onReceiveFrameCallback, ptsUs);
    }
}


int JNI_OnLoad(JavaVM *vm, void *r)
{
    LOGD("%s begin", __FUNCTION__);

    g_jvm = vm;

    LOGD("%s end ", __FUNCTION__);
    return JNI_VERSION_1_4;
}

extern "C" JNIEXPORT void JNICALL
Java_com_huawei_video_kit_hdrvivid_demo_SimpleJni_nativeSetInputSurface(
    JNIEnv *env, jobject thiz, jobject surface)
{
    if (g_simpleProcessor == nullptr) {
        return;
    }
    g_simpleProcessor->SetInputSurface(ANativeWindow_fromSurface(env, surface));
}

extern "C" JNIEXPORT void JNICALL
Java_com_huawei_video_kit_hdrvivid_demo_SimpleJni_nativeSetOutSurface(
    JNIEnv *env, jobject thiz, jobject surface)
{
    if (g_simpleProcessor == nullptr) {
        return;
    }
    g_simpleProcessor->SetOutputSurface(ANativeWindow_fromSurface(env, surface));
}


extern "C" JNIEXPORT void JNICALL
Java_com_huawei_video_kit_hdrvivid_demo_SimpleJni_nativeSetBufferOutFilePath(
    JNIEnv *env, jobject thiz, jstring bufferOutFilePath)
{
    if (g_simpleProcessor == nullptr) {
        return;
    }

    if (bufferOutFilePath == nullptr) {
        return;
    }

    const char *cBufferOutFilePath = env->GetStringUTFChars(bufferOutFilePath, nullptr);
    if (cBufferOutFilePath == nullptr) {
        return;
    }

    g_simpleProcessor->SetBufferOutFilePath(cBufferOutFilePath);
    env->ReleaseStringUTFChars(bufferOutFilePath, cBufferOutFilePath);
}

extern "C" JNIEXPORT void JNICALL
Java_com_huawei_video_kit_hdrvivid_demo_SimpleJni_nativeInit(
    JNIEnv *env, jobject thiz, jobject setting)
{
    jclass settingCls = env->GetObjectClass(setting);
    if (settingCls == nullptr) {
        LOGW("setting is null");
        return;
    }
    jfieldID inputModeField = env->GetFieldID(settingCls, "inputMode", "I");
    jfieldID outputModeField = env->GetFieldID(settingCls, "outputMode", "I");
    jfieldID outputColorSpaceField = env->GetFieldID(settingCls, "outputColorSpace", "I");
    jfieldID outputColorFormatField = env->GetFieldID(settingCls, "outputColorFormat", "I");
    jfieldID brightnessField = env->GetFieldID(settingCls, "brightness", "I");
    jfieldID filePathField = env->GetFieldID(settingCls, "filePath", "Ljava/lang/String;");

    jint inputMode = env->GetIntField(setting, inputModeField);
    jint outputMode = env->GetIntField(setting, outputModeField);
    jint outputColorSpace = env->GetIntField(setting, outputColorSpaceField);
    jint outputColorFormat = env->GetIntField(setting, outputColorFormatField);
    jint brightness = env->GetIntField(setting, brightnessField);
    jstring filePath = (jstring) env->GetObjectField(setting, filePathField);

    const char *cFilePath = env->GetStringUTFChars(filePath, nullptr);
    LOGD("filePath：%s inputMode：%d outputMode：%d outputColorSpace：%d outputColorFormat：%d brightness：%d",
         cFilePath, inputMode, outputMode, outputColorSpace, outputColorFormat, brightness);

    g_simpleProcessor = new SimpleProcessor(g_jvm);
    g_simpleProcessor->SetConfig(cFilePath, inputMode, outputMode, (HdrVividColorSpace) outputColorSpace, (HdrVividColorFormat) outputColorFormat, brightness);

    env->ReleaseStringUTFChars(filePath, cFilePath);
    env->DeleteLocalRef(filePath);
    jclass jClazz = env->GetObjectClass(thiz);
    g_onUpdateTexImageCallback4NativeMethodId = env->GetMethodID(jClazz, "onUpdateTexImageCallback4Native", "()J");
    g_onReceiveFrameCallback = env->GetMethodID(jClazz, "onReceiveFrameCallback", "(J)V");

    g_simpleJniObj = env->NewGlobalRef(thiz);

    env->DeleteLocalRef(jClazz);
    env->DeleteLocalRef(settingCls);
}

extern "C" JNIEXPORT void JNICALL
Java_com_huawei_video_kit_hdrvivid_demo_SimpleJni_nativeRelease(JNIEnv *env, jobject thiz)
{
    LOGI("%s begin", __FUNCTION__);

    if (g_simpleProcessor) {
        delete g_simpleProcessor;
        g_simpleProcessor = nullptr;
    }

    if (g_simpleJniObj) {
        env->DeleteGlobalRef(g_simpleJniObj);
        g_simpleJniObj = nullptr;
    }

    LOGI("%s end", __FUNCTION__);
}

extern "C" JNIEXPORT int JNICALL
Java_com_huawei_video_kit_hdrvivid_demo_SimpleJni_nativeStartPlay(JNIEnv *env, jobject thiz, jstring decoderName)
{
    if (g_simpleProcessor == nullptr) {
        return SIMPLE_ERR_UNKNOWN;
    }

    if (decoderName == nullptr) {
        return SIMPLE_ERR_UNKNOWN;
    }
    const char *cDecoderName = env->GetStringUTFChars(decoderName, nullptr);
    if (cDecoderName == nullptr) {
        return SIMPLE_ERR_UNKNOWN;
    }

    int ret = g_simpleProcessor->StartPlay(cDecoderName);
    env->ReleaseStringUTFChars(decoderName, cDecoderName);
    return ret;
}

extern "C" JNIEXPORT int JNICALL
Java_com_huawei_video_kit_hdrvivid_demo_SimpleJni_nativeStopPlay(JNIEnv *env, jobject thiz)
{
    if (g_simpleProcessor == nullptr) {
        return SIMPLE_ERR_UNKNOWN;
    }

    return g_simpleProcessor->StopPlay();
}

extern "C" JNIEXPORT void JNICALL
Java_com_huawei_video_kit_hdrvivid_demo_SimpleJni_nativePausePlay(JNIEnv *env, jobject thiz)
{
    if (g_simpleProcessor == nullptr) {
        return;
    }

    g_simpleProcessor->PausePlay();
}

extern "C" JNIEXPORT void JNICALL
Java_com_huawei_video_kit_hdrvivid_demo_SimpleJni_nativeResumePlay(JNIEnv *env, jobject thiz)
{
    if (g_simpleProcessor == nullptr) {
        return;
    }

    g_simpleProcessor->ResumePlay();
}

extern "C" JNIEXPORT jint JNICALL
Java_com_huawei_video_kit_hdrvivid_demo_SimpleJni_nativeGetTextureId(JNIEnv *env, jobject thiz)
{
    if (g_simpleProcessor == nullptr) {
        return -1;
    }

    return g_simpleProcessor->GenTextureId();
}

extern "C" JNIEXPORT int JNICALL
Java_com_huawei_video_kit_hdrvivid_demo_SimpleJni_nativeUpdateTexture(JNIEnv *env, jobject thiz)
{
    if (g_simpleProcessor == nullptr) {
        return SIMPLE_ERR_UNKNOWN;
    }

    return g_simpleProcessor->UpdateTexture();
}

extern "C" JNIEXPORT jlong JNICALL
Java_com_huawei_video_kit_hdrvivid_demo_SimpleJni_nativeOpenExtractor(JNIEnv *env, jobject clazz, jstring filePath)
{
    LOGD("%s begin", __FUNCTION__);

    if (filePath == nullptr) {
        return INVALID_HANDLE;
    }
    const char *cPath = env->GetStringUTFChars(filePath, nullptr);
    if (cPath == nullptr) {
        return INVALID_HANDLE;
    }

    jlong retValue = INVALID_HANDLE;

    auto *se = new SimpleExtractor();
    if (!se->Open(cPath)) {
        se->Close();
        delete se;
    } else {
        retValue = reinterpret_cast<jlong>(se);
    }

    env->ReleaseStringUTFChars(filePath, cPath);
    LOGD("%s end, handleExtractor=%" PRId64, __FUNCTION__, retValue);
    return retValue;
}

extern "C" JNIEXPORT void JNICALL
Java_com_huawei_video_kit_hdrvivid_demo_SimpleJni_nativeCloseExtractor(JNIEnv *env, jobject clazz,
                                                                       jlong handleExtractor)
{
    LOGD("%s begin, handleExtractor=%" PRId64, __FUNCTION__, handleExtractor);

    if (handleExtractor == INVALID_HANDLE) {
        return;
    }
    auto *se = reinterpret_cast<SimpleExtractor *>(handleExtractor);
    if (se == nullptr) {
        return;
    }

    se->Close();
    delete se;

    LOGD("%s end", __FUNCTION__);
}

static void SetSimplePacket(JNIEnv *env, jobject simplePacket, SimplePacket *sPacket);

extern "C" JNIEXPORT jboolean JNICALL
Java_com_huawei_video_kit_hdrvivid_demo_SimpleJni_nativeGetNextPacket(JNIEnv *env, jobject clazz, jlong handleExtractor,
                                                                      jobject simplePacket)
{
    LOGD("%s begin, handleExtractor=%" PRId64, __FUNCTION__, handleExtractor);

    if (handleExtractor == INVALID_HANDLE) {
        return false;
    }
    auto *se = reinterpret_cast<SimpleExtractor *>(handleExtractor);
    if (se == nullptr) {
        return false;
    }

    SimplePacket *sPacket = se->GetNextSample();
    if (sPacket == nullptr) {
        return false;
    }

    SetSimplePacket(env, simplePacket, sPacket);

    LOGD("%s end", __FUNCTION__);
    return true;
}

static void SetMediaFormatBuffer(JNIEnv *env, jobject mediaFormatBuffer, MediaFormatBuffer *info);

extern "C" JNIEXPORT jboolean JNICALL
Java_com_huawei_video_kit_hdrvivid_demo_SimpleJni_nativeGetMediaFormatBuffer(JNIEnv *env, jobject clazz,
                                                                             jlong handleExtractor,
                                                                             jobject mediaFormatBuffer, jstring name)
{
    LOGD("%s begin, handleExtractor=%" PRId64, __FUNCTION__, handleExtractor);

    if (handleExtractor == INVALID_HANDLE) {
        return false;
    }
    auto *se = reinterpret_cast<SimpleExtractor *>(handleExtractor);
    if (se == nullptr) {
        return false;
    }
    if (name == nullptr) {
        return false;
    }
    const char *cName = env->GetStringUTFChars(name, nullptr);
    if (cName == nullptr) {
        return false;
    }

    MediaFormatBuffer *info = se->GetMediaFormatBuffer(cName);
    if (info == nullptr) {
        return false;
    }

    SetMediaFormatBuffer(env, mediaFormatBuffer, info);

    LOGD("%s end", __FUNCTION__);
    return true;
}

extern "C" JNIEXPORT jboolean JNICALL
Java_com_huawei_video_kit_hdrvivid_demo_SimpleJni_nativeIsEof(JNIEnv *env, jobject clazz, jlong handleExtractor)
{
    LOGD("%s begin, handleExtractor=%" PRId64, __FUNCTION__, handleExtractor);

    if (handleExtractor == INVALID_HANDLE) {
        return false;
    }
    auto *se = reinterpret_cast<SimpleExtractor *>(handleExtractor);
    if (se == nullptr) {
        return false;
    }

    bool eof = se->IsEof();

    LOGD("%s end, eof=%d", __FUNCTION__, eof);
    return eof;
}

extern "C" JNIEXPORT void JNICALL
Java_com_huawei_video_kit_hdrvivid_demo_SimpleJni_nativeSetHwCodec(JNIEnv *env, jobject clazz, jlong handleExtractor,
                                                                   jboolean hwCodec)
{
    LOGD("%s begin, handleExtractor=%" PRId64, __FUNCTION__, handleExtractor);

    if (handleExtractor == INVALID_HANDLE) {
        return;
    }
    auto *se = reinterpret_cast<SimpleExtractor *>(handleExtractor);
    if (se == nullptr) {
        return;
    }
}

extern "C" JNIEXPORT jlong JNICALL
Java_com_huawei_video_kit_hdrvivid_demo_SimpleJni_nativeGetDurationUs(JNIEnv *env, jobject clazz, jlong handleExtractor)
{
    LOGD("%s begin, handleExtractor=%" PRId64, __FUNCTION__, handleExtractor);

    if (handleExtractor == INVALID_HANDLE) {
        return -1;
    }
    auto *se = reinterpret_cast<SimpleExtractor *>(handleExtractor);
    if (se == nullptr) {
        return -1;
    }

    return se->GetDurationUs();
}

extern "C" JNIEXPORT void JNICALL
Java_com_huawei_video_kit_hdrvivid_demo_SimpleJni_nativeStartRender(JNIEnv *env, jobject thiz, jint width, jint height,
                                                                    jobject surface)
{
    LOGD("%s begin", __FUNCTION__);
    if (g_simplePlayer == nullptr) {
        g_simplePlayer = new SimplePlayer();
    }
    g_simplePlayer->Init(width, height, ANativeWindow_fromSurface(env, surface));
}

extern "C" JNIEXPORT void JNICALL
Java_com_huawei_video_kit_hdrvivid_demo_SimpleJni_nativeRender(JNIEnv *env, jobject clazz, jobject byteBuffer,
                                                               jint dataSize)
{
    if (g_simplePlayer == nullptr) {
        return;
    }
    uint8_t *buffer = reinterpret_cast<uint8_t *>(env->GetDirectBufferAddress(byteBuffer));
    g_simplePlayer->OnBufferAvailable(buffer, dataSize);
}

extern "C" JNIEXPORT void JNICALL
Java_com_huawei_video_kit_hdrvivid_demo_SimpleJni_nativeStopRender(JNIEnv *env, jobject clazz)
{
    LOGD("%s begin", __FUNCTION__);
    if (g_simplePlayer == nullptr) {
        return;
    }
    g_simplePlayer->Release();
    delete g_simplePlayer;
    g_simplePlayer = nullptr;
}

static void SetHdrMetaData(JNIEnv *env, jobject simplePacket, SimplePacket *sPacket, jclass jClassSimplePacket);

static void SetSimplePacket(JNIEnv *env, jobject simplePacket, SimplePacket *sPacket)
{
    jclass jClassSimplePacket = env->FindClass("com/huawei/video/kit/hdrvivid/demo/base/SimplePacket");

    jfieldID ptsUs = env->GetFieldID(jClassSimplePacket, "ptsUs", "J");
    env->SetLongField(simplePacket, ptsUs, sPacket->GetPtsUs());

    if (sPacket->GetSize() > 0) {
        jbyteArray byteArray = env->NewByteArray(sPacket->GetSize());
        env->SetByteArrayRegion(byteArray, 0, sPacket->GetSize(), reinterpret_cast<const jbyte *>(sPacket->GetData()));
        jfieldID data = env->GetFieldID(jClassSimplePacket, "data", "[B");
        env->SetObjectField(simplePacket, data, byteArray);
    }

    jfieldID size = env->GetFieldID(jClassSimplePacket, "size", "I");
    env->SetIntField(simplePacket, size, sPacket->GetSize());

    jfieldID width = env->GetFieldID(jClassSimplePacket, "width", "I");
    env->SetIntField(simplePacket, width, sPacket->GetWidth());

    jfieldID height = env->GetFieldID(jClassSimplePacket, "height", "I");
    env->SetIntField(simplePacket, height, sPacket->GetHeight());

    jfieldID tf = env->GetFieldID(jClassSimplePacket, "tf", "I");
    env->SetIntField(simplePacket, tf, static_cast<int>(sPacket->GetTf()));

    jfieldID colorSpace = env->GetFieldID(jClassSimplePacket, "colorSpace", "I");
    env->SetIntField(simplePacket, colorSpace, static_cast<int>(sPacket->GetColorSpace()));

    jfieldID colorFormat = env->GetFieldID(jClassSimplePacket, "colorFormat", "I");
    env->SetIntField(simplePacket, colorFormat, static_cast<int>(sPacket->GetColorFormat()));

    // set HDR Meta Data
    SetHdrMetaData(env, simplePacket, sPacket, jClassSimplePacket);
    env->DeleteLocalRef(jClassSimplePacket);
}

static void SetHdrMetaData(JNIEnv *env, jobject simplePacket, SimplePacket *sPacket, jclass jClassSimplePacket)
{
    jfieldID hdm = env->GetFieldID(jClassSimplePacket, "hmd", "Lcom/huawei/video/kit/hdrvivid/demo/hdr/HdrMetaData;");
    jobject objHdm = env->GetObjectField(simplePacket, hdm);

    jclass jClassHdrMetaData = env->FindClass("com/huawei/video/kit/hdrvivid/demo/hdr/HdrMetaData");

    jfieldID gX = env->GetFieldID(jClassHdrMetaData, "gX", "I");
    env->SetIntField(objHdm, gX, sPacket->GetHdrMetaData()->GetGx());

    jfieldID gY = env->GetFieldID(jClassHdrMetaData, "gY", "I");
    env->SetIntField(objHdm, gY, sPacket->GetHdrMetaData()->GetGy());

    jfieldID bX = env->GetFieldID(jClassHdrMetaData, "bX", "I");
    env->SetIntField(objHdm, bX, sPacket->GetHdrMetaData()->GetBx());

    jfieldID bY = env->GetFieldID(jClassHdrMetaData, "bY", "I");
    env->SetIntField(objHdm, bY, sPacket->GetHdrMetaData()->GetBy());

    jfieldID rX = env->GetFieldID(jClassHdrMetaData, "rX", "I");
    env->SetIntField(objHdm, rX, sPacket->GetHdrMetaData()->GetRx());

    jfieldID rY = env->GetFieldID(jClassHdrMetaData, "rY", "I");
    env->SetIntField(objHdm, rY, sPacket->GetHdrMetaData()->GetRy());

    jfieldID whitePointX = env->GetFieldID(jClassHdrMetaData, "whitePointX", "I");
    env->SetIntField(objHdm, whitePointX, sPacket->GetHdrMetaData()->GetWhitePointX());

    jfieldID whitePointY = env->GetFieldID(jClassHdrMetaData, "whitePointY", "I");
    env->SetIntField(objHdm, whitePointY, sPacket->GetHdrMetaData()->GetWhitePointY());

    jfieldID maxDisplayMasteringLum = env->GetFieldID(jClassHdrMetaData, "maxDisplayMasteringLum", "I");
    env->SetIntField(objHdm, maxDisplayMasteringLum, sPacket->GetHdrMetaData()->GetMaxDisplayMasteringLum());

    jfieldID minDisplayMasteringLum = env->GetFieldID(jClassHdrMetaData, "minDisplayMasteringLum", "I");
    env->SetIntField(objHdm, minDisplayMasteringLum, sPacket->GetHdrMetaData()->GetMinDisplayMasteringLum());

    jfieldID maxContentLightLevel = env->GetFieldID(jClassHdrMetaData, "maxContentLightLevel", "I");
    env->SetIntField(objHdm, maxContentLightLevel, sPacket->GetHdrMetaData()->GetMaxContentLightLevel());

    jfieldID maxPicAverageLightLevel = env->GetFieldID(jClassHdrMetaData, "maxPicAverageLightLevel", "I");
    env->SetIntField(objHdm, maxPicAverageLightLevel, sPacket->GetHdrMetaData()->GetMaxPicAverageLightLevel());

    if (sPacket->GetHdrMetaData()->GetDmSize() > 0) {
        jbyteArray byteArray2 = env->NewByteArray(sPacket->GetHdrMetaData()->GetDmSize());
        env->SetByteArrayRegion(
            byteArray2, 0, sPacket->GetHdrMetaData()->GetDmSize(), reinterpret_cast<const jbyte *>(sPacket->GetHdrMetaData()->GetDmData()));
        jfieldID data2 = env->GetFieldID(jClassHdrMetaData, "dmData", "[B");
        env->SetObjectField(objHdm, data2, byteArray2);
    }

    jfieldID dmSize = env->GetFieldID(jClassHdrMetaData, "dmSize", "I");
    env->SetIntField(objHdm, dmSize, sPacket->GetHdrMetaData()->GetDmSize());

    env->DeleteLocalRef(jClassHdrMetaData);
    env->DeleteLocalRef(objHdm);
}


static void SetMediaFormatBuffer(JNIEnv *env, jobject mediaFormatBuffer, MediaFormatBuffer *info)
{
    jclass jClassMediaFormatBuffer = env->FindClass("com/huawei/video/kit/hdrvivid/demo/base/MediaFormatBuffer");
    if (info->GetSize() > 0) {
        jbyteArray byteArray = env->NewByteArray(info->GetSize());
        env->SetByteArrayRegion(byteArray, 0, info->GetSize(), reinterpret_cast<const jbyte *>(info->GetData()));
        jfieldID data = env->GetFieldID(jClassMediaFormatBuffer, "data", "[B");
        env->SetObjectField(mediaFormatBuffer, data, byteArray);
    }

    jfieldID size = env->GetFieldID(jClassMediaFormatBuffer, "size", "I");
    env->SetIntField(mediaFormatBuffer, size, info->GetSize());
    env->DeleteLocalRef(jClassMediaFormatBuffer);
}
