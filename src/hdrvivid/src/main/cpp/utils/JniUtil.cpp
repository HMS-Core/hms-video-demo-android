/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2022-2022. All rights reserved.
 * Description: JNI utils
 * Create: 2022-06-05
 */

#include "SimpleLog.h"
#include "JniUtil.h"

JniUtil::JniUtil(JavaVM *jvm) : m_jvm(jvm)
{

}

JniUtil::~JniUtil()
{

}

JNIEnv *JniUtil::GetJniEnv() const
{
    if (m_jvm == nullptr) {
        LOGW("%s, m_jvm is nullptr", __FUNCTION__);
        return nullptr;
    }

    JNIEnv *jniEnv = nullptr;
    int status = m_jvm->GetEnv((void **) &jniEnv, JNI_VERSION_1_4);
    if (status != JNI_OK || jniEnv == nullptr) {
        LOGW("%s, GetEnv failed, %d, %p", __FUNCTION__, status, jniEnv);
        return nullptr;
    }

    return jniEnv;
}

void JniUtil::JniAttach() const
{
    JNIEnv *jniEnv = nullptr;
    int status = m_jvm->GetEnv((void **) &jniEnv, JNI_VERSION_1_4);
    if (status == JNI_EDETACHED || jniEnv == nullptr) {
        status = m_jvm->AttachCurrentThread(&jniEnv, nullptr);
        if (status < 0) {
            LOGW("%s failed, status=%d", __FUNCTION__, status);
        }
    }
}

void JniUtil::JniDetach() const
{
    if (m_jvm != nullptr) {
        m_jvm->DetachCurrentThread();
    }
}

bool JniUtil::HasException(JNIEnv *jniEnv)
{
    jthrowable exception = jniEnv->ExceptionOccurred();
    if (exception == nullptr) {
        return false;
    }

    ThrowException(jniEnv);
    return true;
}

jobject JniUtil::NewBundle(JNIEnv *jniEnv)
{
    return NewBundle(jniEnv, "android/os/Bundle");
}

jobject JniUtil::NewPersistableBundle(JNIEnv *jniEnv)
{
    return NewBundle(jniEnv, "android/os/PersistableBundle");
}

jobject JniUtil::NewBundle(JNIEnv *jniEnv, const char *type)
{
    jclass jBundleClass = jniEnv->FindClass(type);
    if (jBundleClass == nullptr) {
        LOGW("%s, find class failed", __FUNCTION__);
        return nullptr;
    }

    jmethodID jBundleCtorMethod = jniEnv->GetMethodID(jBundleClass, "<init>", "()V");
    if (jBundleCtorMethod == nullptr) {
        LOGW("%s, find ctor method failed", __FUNCTION__);
        return nullptr;
    }

    jobject bundleObj = jniEnv->NewObject(jBundleClass, jBundleCtorMethod);
    jniEnv->DeleteLocalRef(jBundleClass);
    return bundleObj;
}

void JniUtil::ThrowException(JNIEnv *jniEnv)
{
    jniEnv->ExceptionDescribe();
    jniEnv->ExceptionClear();

    jclass newExcCls = jniEnv->FindClass("java/lang/IllegalArgumentException");
    if (newExcCls == nullptr) {
        return;
    }
    jniEnv->ExceptionOccurred();
    jniEnv->ExceptionDescribe();
    jniEnv->ExceptionClear();
    LOGW("%s, do not throw exception", __FUNCTION__);
}
