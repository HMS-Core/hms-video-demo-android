/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2022-2022. All rights reserved.
 * Description: encapsulation log function
 * Create: 2022-05-18
 */

#include <stdio.h>
#include "SimpleLog.h"

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
