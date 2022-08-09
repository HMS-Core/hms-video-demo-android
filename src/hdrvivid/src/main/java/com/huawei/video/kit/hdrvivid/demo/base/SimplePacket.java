/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2022-2022. All rights reserved.
 */

package com.huawei.video.kit.hdrvivid.demo.base;

import com.huawei.video.kit.hdrvivid.demo.hdr.HdrMetaData;

/**
 * packet for audio/video stream
 */
public class SimplePacket {
    public static final int COLORSPACE_P3 = 1;
    public static final int COLORSPACE_BT709 = 2;
    public static final int COLORSPACE_BT2020 = 3;

    public static final int FORMAT_SRGB = 1;
    public static final int FORMAT_NV12 = 2;
    public static final int FORMAT_YUV420_888 = 3;

    public static final int TRANSFUNC_PQ = 1;
    public static final int TRANSFUNC_HLG = 2;
    public static final int TRANSFUNC_VIVID = 3;

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
