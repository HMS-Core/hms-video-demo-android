/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2022-2022. All rights reserved.
 */

package com.huawei.video.kit.hdrvivid.demo.base;

public class VideoInfo {

    private int width;

    private int height;

    private int tf;

    private int colorSpace;

    private int colorFormat;

    private long durationUs;

    public long getDurationUs() {
        return durationUs;
    }

    public void setDurationUs(long durationUs) {
        this.durationUs = durationUs;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getTf() {
        return tf;
    }

    public void setTf(int tf) {
        this.tf = tf;
    }

    public int getColorSpace() {
        return colorSpace;
    }

    public void setColorSpace(int colorSpace) {
        this.colorSpace = colorSpace;
    }

    public int getColorFormat() {
        return colorFormat;
    }

    public void setColorFormat(int colorFormat) {
        this.colorFormat = colorFormat;
    }

}
