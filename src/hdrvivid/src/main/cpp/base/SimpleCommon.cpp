/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2022-2022. All rights reserved.
 * Description: common defines
 * Create: 2022-05-20
 */

#include <time.h>
#include "SimpleCommon.h"

int64_t SimpleTimeGetBootTimeWithDeepSleepMs()
{
    timespec tp{};
    clock_gettime(CLOCK_BOOTTIME, &tp);
    return tp.tv_sec * S_2_MS + tp.tv_nsec / MS_2_NS;
}

