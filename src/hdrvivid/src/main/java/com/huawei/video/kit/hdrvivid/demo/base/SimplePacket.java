/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2022-2022. All rights reserved.
 */

package com.huawei.video.kit.hdrvivid.demo.base;

import com.huawei.video.kit.hdrvivid.demo.hdr.HdrMetaData;

/**
 * packet for audio/video stream
 */
public class SimplePacket {
    public long ptsUs = -1;

    public byte[] data;

    public int size;

    public int width;

    public int height;

    public int tf;

    public int colorSpace;

    public int colorFormat;

    public HdrMetaData hmd = new HdrMetaData();
}
