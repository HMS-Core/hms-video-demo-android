/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2022-2022. All rights reserved.
 * Description: encapsulation log function
 * Create: 2022-05-18
 */

#include <stdio.h>
#include "SimpleLog.h"
#include "HdrVividKits.h"

static constexpr int32_t LOG_TYPE_MAX = 64;
static const char *const TAG_SDK = "HVDemoFromSDK";

int HdrVividLog(int level, const char *avcl, const char *fmt, const va_list vl)
{
    if (level < 0 || level >= LOG_TYPE_MAX) {
        return 0;
    }
    switch (level) {
        case HDRVIVID_LOG_INFO:
            __android_log_vprint(ANDROID_LOG_INFO, TAG_SDK, fmt, vl);
            break;
        case HDRVIVID_LOG_DEBUG:
            __android_log_vprint(ANDROID_LOG_DEBUG, TAG_SDK, fmt, vl);
            break;
        case HDRVIVID_LOG_WARN:
            __android_log_vprint(ANDROID_LOG_WARN, TAG_SDK, fmt, vl);
            break;
        case HDRVIVID_LOG_ERROR:
            __android_log_vprint(ANDROID_LOG_ERROR, TAG_SDK, fmt, vl);
            break;
        default:
            __android_log_vprint(ANDROID_LOG_INFO, TAG_SDK, fmt, vl);
            break;
    }
    return 1;
}

void EasyWriteFile(const char *path, const uint8_t *data, int32_t size)
{
    if (path == nullptr || data == nullptr || size <= 0) {
        return;
    }

    FILE *fp = fopen(path, "ab+");
    if (fp == nullptr) {
        return;
    }
    fwrite(data, 1, size, fp);
    fclose(fp);
}
