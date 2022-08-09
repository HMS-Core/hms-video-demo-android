/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2022-2022. All rights reserved.
 * Description: encapsulation log function
 * Create: 2022-05-10
 */

#ifndef SIMPLE_LOG_H
#define SIMPLE_LOG_H

#include <android/log.h>
#include <stdint.h>

static const char *const TAG = "HVDemo";

#define LOGV(...)   __android_log_print(ANDROID_LOG_VERBOSE, TAG, __VA_ARGS__)
#define LOGD(...)   __android_log_print(ANDROID_LOG_DEBUG, TAG, __VA_ARGS__)
#define LOGI(...)   __android_log_print(ANDROID_LOG_INFO, TAG, __VA_ARGS__)
#define LOGW(...)   __android_log_print(ANDROID_LOG_WARN, TAG, __VA_ARGS__)
#define LOGE(...)   __android_log_print(ANDROID_LOG_ERROR, TAG, __VA_ARGS__)
#define LOGF(...)   __android_log_print(ANDROID_LOG_FATAL, TAG, __VA_ARGS__)

void EasyWriteFile(const char *filePath, const uint8_t *data, int32_t size);

#endif // SIMPLE_LOG_H
