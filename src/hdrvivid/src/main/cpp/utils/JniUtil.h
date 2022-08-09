/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2022-2022. All rights reserved.
 * Description: JNI utils
 * Create: 2022-06-05
 */

#ifndef JNI_UTILS_H
#define JNI_UTILS_H

#include <jni.h>

class JniUtil {
public:
    explicit JniUtil(JavaVM *jvm);

    ~JniUtil();

    JNIEnv *GetJniEnv() const;

    void JniAttach() const;

    void JniDetach() const;

    static bool HasException(JNIEnv *jniEnv);

    static jobject NewBundle(JNIEnv *jniEnv);

    static jobject NewPersistableBundle(JNIEnv *jniEnv);

private:
    static jobject NewBundle(JNIEnv *jniEnv, const char *type);

    static void ThrowException(JNIEnv *jniEnv);

private:
    JavaVM *m_jvm;
};

#endif // JNI_UTILS_H
