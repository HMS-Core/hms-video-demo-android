/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2022-2022. All rights reserved.
 */

package com.huawei.video.kit.hdrvivid.demo.utils;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Build;

public class DeviceUtils {
    public static boolean isScreenHdr(Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return false;
        }

        Configuration config = context.getResources().getConfiguration();
        return config.isScreenHdr();
    }
}
