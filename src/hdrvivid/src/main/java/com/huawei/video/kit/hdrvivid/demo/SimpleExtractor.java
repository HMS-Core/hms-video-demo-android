/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2022-2022. All rights reserved.
 */

package com.huawei.video.kit.hdrvivid.demo;

import com.huawei.video.kit.hdrvivid.demo.base.SimplePacket;

/**
 * Description: extractor hdr vivid packet from video mp4 files
 */
public class SimpleExtractor {
    private static final long INVALID_HANDLE = -1;

    private SimpleJni simpleJni = null;

    private long handleExtractor = INVALID_HANDLE;

    public SimpleExtractor() {
        simpleJni = SimpleJni.getInstance();
    }

    public boolean openSimpleExtractor(String filePath) {
        if (filePath == null || filePath.isEmpty()) {
            return false;
        }

        handleExtractor = simpleJni.nativeOpenExtractor(filePath);
        if (handleExtractor == INVALID_HANDLE) {
            return false;
        }

        return true;
    }

    public void closeSimpleExtractor() {
        if (handleExtractor == INVALID_HANDLE) {
            return;
        }

        simpleJni.nativeCloseExtractor(handleExtractor);
        handleExtractor = INVALID_HANDLE;
    }

    public boolean getSimpleNextPacket(SimplePacket simplePacket) {
        if (handleExtractor == INVALID_HANDLE) {
            return false;
        }

        return simpleJni.nativeGetNextPacket(handleExtractor, simplePacket);
    }

    public long getDurationUs() {
        if (handleExtractor == INVALID_HANDLE) {
            return -1;
        }

        return simpleJni.nativeGetDurationUs(handleExtractor);
    }

}
