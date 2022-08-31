/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2022-2022. All rights reserved.
 */

package com.huawei.video.kit.hdrvivid.demo.base;

public class PlaybackInfo {
    private long ptsUs = 0L;

    private int totalNum = 0;

    private long startTime = 0L;

    private long statsTime = 0L;

    private int statsNum = 0;

    private float frameRate = 0.0f;

    public float getFrameRate() {
        return frameRate;
    }

    public void setFrameRate(float frameRate) {
        this.frameRate = frameRate;
    }

    public int getTotalNum() {
        return totalNum;
    }

    public void setTotalNum(int totalNum) {
        this.totalNum = totalNum;
    }

    public long getStatsTime() {
        return statsTime;
    }

    public void setStatsTime(long statsTime) {
        this.statsTime = statsTime;
    }

    public int getStatsNum() {
        return statsNum;
    }

    public void setStatsNum(int statsNum) {
        this.statsNum = statsNum;
    }

    public long getPtsUs() {
        return ptsUs;
    }

    public void setPtsUs(long ptsUs) {
        this.ptsUs = ptsUs;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }
}
