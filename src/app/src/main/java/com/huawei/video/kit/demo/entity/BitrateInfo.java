package com.huawei.video.kit.demo.entity;

/**
 * Bitrate info
 */
public class BitrateInfo {
    private int minBitrate;

    private int maxBitrate;

    private int currentBitrate;

    private int videoHeight;

    public int getMinBitrate() {
        return minBitrate;
    }

    public void setMinBitrate(int minBitrate) {
        this.minBitrate = minBitrate;
    }

    public int getMaxBitrate() {
        return maxBitrate;
    }

    public void setMaxBitrate(int maxBitrate) {
        this.maxBitrate = maxBitrate;
    }

    public int getCurrentBitrate() {
        return currentBitrate;
    }

    public void setCurrentBitrate(int currentBitrate) {
        this.currentBitrate = currentBitrate;
    }

    public int getVideoHeight() {
        return videoHeight;
    }

    public void setVideoHeight(int videoHeight) {
        this.videoHeight = videoHeight;
    }

    @Override
    public String toString() {
        return "BitrateInfo{" +
                "minBitrate=" + minBitrate +
                ", maxBitrate=" + maxBitrate +
                ", currentBitrate=" + currentBitrate +
                ", videoHeight=" + videoHeight +
                '}';
    }
}
