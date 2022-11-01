/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2022-2022. All rights reserved.
 */

package com.huawei.video.kit.hdrvivid.demo.utils;

import java.text.DecimalFormat;

import android.util.Log;

import com.huawei.video.kit.hdrvivid.demo.SimpleExtractor;
import com.huawei.video.kit.hdrvivid.demo.SimpleProcessor;
import com.huawei.video.kit.hdrvivid.demo.base.PlaybackInfo;
import com.huawei.video.kit.hdrvivid.demo.base.SimplePacket;
import com.huawei.video.kit.hdrvivid.demo.base.VideoInfo;

public class VideoInfoUtils {
    private static final String TAG = "VideoInfoUtils";

    private static final String FORMAT_PATTERN = "0.00";

    private static volatile VideoInfoUtils videoInfoUtils = null;

    private SimpleExtractor simpleExtractor = null;

    private VideoInfo videoInfo = new VideoInfo();

    private PlaybackInfo playbackInfo = new PlaybackInfo();

    private VideoInfoUtils() {
    }

    public static VideoInfoUtils getInstance() {
        if (videoInfoUtils == null) {
            synchronized (SimpleProcessor.class) {
                if (videoInfoUtils == null) {
                    videoInfoUtils = new VideoInfoUtils();
                }
            }
        }
        return videoInfoUtils;
    }

    public VideoInfo getVideoInfo() {
        return videoInfo;
    }

    public PlaybackInfo getPlaybackInfo() {
        return playbackInfo;
    }

    public boolean getVideoInfoFromFile(String filePath) {
        simpleExtractor = new SimpleExtractor();
        boolean result = simpleExtractor.openSimpleExtractor(filePath);
        if (!result) {
            return false;
        }

        SimplePacket simplePacket = new SimplePacket();
        result = simpleExtractor.getSimpleNextPacket(simplePacket);
        if (!result) {
            return false;
        }

        videoInfo.setWidth(simplePacket.width);
        videoInfo.setHeight(simplePacket.height);
        videoInfo.setTf(simplePacket.tf);
        videoInfo.setColorSpace(simplePacket.colorSpace);
        videoInfo.setColorFormat(simplePacket.colorFormat);
        videoInfo.setDurationUs(simpleExtractor.getDurationUs());
        simpleExtractor.closeSimpleExtractor();

        Log.i(TAG, "getVideoInfo: " + simplePacket.width + " " + simplePacket.height + " " + simplePacket.tf + " "
            + simplePacket.colorSpace + " " + simplePacket.colorFormat + " " + videoInfo.getDurationUs());
        return true;
    }

    public void increaseFrame(long pts) {
        int frameNum = playbackInfo.getTotalNum();
        if (frameNum == 0) {
            long currentTimeMillis = System.currentTimeMillis();
            playbackInfo.setStartTime(currentTimeMillis);
            playbackInfo.setStatsTime(currentTimeMillis);
        }
        playbackInfo.setPtsUs(pts);
        playbackInfo.setStatsNum(playbackInfo.getStatsNum() + 1);
        playbackInfo.setTotalNum(frameNum + 1);
    }

    public void onFinishPlay() {
        playbackInfo.setPtsUs(0);
        playbackInfo.setStartTime(0);
        playbackInfo.setTotalNum(0);
    }

    public String getFrameRate() {
        int statsNum = playbackInfo.getStatsNum();
        long statsTime = playbackInfo.getStatsTime();
        long currentTimeMillis = System.currentTimeMillis();
        long timeSpanMs = currentTimeMillis - statsTime;
        DecimalFormat decimalFormat = new DecimalFormat(FORMAT_PATTERN);

        if (timeSpanMs < SimpleTimeUtils.SEC2MS) {
            return decimalFormat.format(playbackInfo.getFrameRate());
        }

        float frameRate = (float) statsNum / ((float) timeSpanMs / SimpleTimeUtils.SEC2MS);
        playbackInfo.setStatsTime(currentTimeMillis);
        playbackInfo.setStatsNum(0);
        playbackInfo.setFrameRate(frameRate);
        Log.d(TAG, "getFrameRate " + statsNum + " " + timeSpanMs + " " + frameRate);

        return decimalFormat.format(frameRate);
    }

}
