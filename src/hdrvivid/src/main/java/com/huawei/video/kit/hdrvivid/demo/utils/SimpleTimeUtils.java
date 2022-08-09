/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2022-2022. All rights reserved.
 */

package com.huawei.video.kit.hdrvivid.demo.utils;

public class SimpleTimeUtils {
    public static final int SEC2US = 1000000;

    public static final int SEC2MS = 1000;

    public static final int HOUR2SEC = 3600;

    public static final int HOUR2MINUTE = 60;

    public static String getTimeString(long timeUs) {
        StringBuffer sb = new StringBuffer();
        long sec = timeUs / SEC2US;
        sb.append(sec / HOUR2SEC)
            .append(Constants.COLON)
            .append((sec % HOUR2SEC) / HOUR2MINUTE)
            .append(Constants.COLON)
            .append((sec % HOUR2SEC) % HOUR2MINUTE);

        return sb.toString();
    }
}
