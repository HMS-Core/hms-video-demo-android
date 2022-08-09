/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2022-2022. All rights reserved.
 * Description: common defines
 * Create: 2022-05-16
 */

#ifndef SIMPLE_COMMON_H
#define SIMPLE_COMMON_H

#include <cstdint>
#include <string>
#include "SimpleLog.h"

constexpr int32_t INVALID_HANDLE = -1;

constexpr int64_t D_2_H = 24;
constexpr int64_t H_2_M = 60;
constexpr int64_t M_2_S = 60;
constexpr int64_t S_2_MS = 1000;
constexpr int64_t MS_2_US = 1000;
constexpr int64_t US_2_NS = 1000;

constexpr int64_t M_2_MS = M_2_S * S_2_MS;
constexpr int64_t H_2_MS = H_2_M * M_2_MS;
constexpr int64_t D_2_MS = D_2_H * H_2_MS;

constexpr int64_t S_2_US = S_2_MS * MS_2_US;
constexpr int64_t S_2_NS = S_2_MS * MS_2_US * US_2_NS;
constexpr int64_t MS_2_NS = MS_2_US * US_2_NS;

constexpr int32_t INPUT_MODE_SURFACE = 1;
constexpr int32_t OUT_MODE_SURFACE = 1;
constexpr int32_t OUT_MODE_BUFFER = 2;

constexpr int32_t HALF = 2;
constexpr int32_t LINE_0 = 0;
constexpr int32_t LINE_1 = 1;
constexpr int32_t LINE_2 = 2;

enum SimpleStatus {
    SIMPLE_SUCCESS = 0,
    SIMPLE_ERR_UNKNOWN = 10000,
    SIMPLE_ERR_SDK_START_FAILED = 10001,
    SIMPLE_ERR_SDK_STOP_FAILED = 10002,
    SIMPLE_ERR_SDK_SET_FAILED = 10003,
    SIMPLE_ERR_WRONG_INPUT_PARAM = 10004,
    SIMPLE_ERR_FORMAT_NOT_SUPPORT = 10005,
    SIMPLE_ERR_MEDIACODEC_INIT_FAILED = 10006,
};

int64_t SimpleTimeGetBootTimeWithDeepSleepMs();

#endif // SIMPLE_COMMON_H
