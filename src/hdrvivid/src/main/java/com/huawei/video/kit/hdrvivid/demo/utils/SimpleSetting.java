/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2022-2022. All rights reserved.
 */

package com.huawei.video.kit.hdrvivid.demo.utils;

/**
 * demo setting
 * 
 * @since 2022/5/5
 */
public class SimpleSetting {
    private static SimpleSetting simpleSetting = null;

    private int apiType = Constants.API_TYPE_NATIVE;

    private int inputMode;

    private int outputMode;

    private int outputColorSpace;

    private int outputColorFormat;

    private String filePath;

    private int brightness;

    private String bufferOutFilePath;

    private SimpleSetting() {
    }

    public static SimpleSetting getInstance() {
        if (simpleSetting == null) {
            synchronized (SimpleSetting.class) {
                if (simpleSetting == null) {
                    simpleSetting = new SimpleSetting();
                }
            }
        }
        return simpleSetting;
    }

    public int getApiType() {
        return apiType;
    }

    public void setApiType(int apiType) {
        this.apiType = apiType;
    }

    public int getBrightness() {
        return brightness;
    }

    public void setBrightness(int brightness) {
        this.brightness = brightness;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public int getInputMode() {
        return inputMode;
    }

    public void setInputMode(int inputMode) {
        this.inputMode = inputMode;
    }

    public int getOutputMode() {
        return outputMode;
    }

    public void setOutputMode(int outputMode) {
        this.outputMode = outputMode;
    }

    public int getOutputColorSpace() {
        return outputColorSpace;
    }

    public void setOutputColorSpace(int outputColorSpace) {
        this.outputColorSpace = outputColorSpace;
    }

    public int getOutputColorFormat() {
        return outputColorFormat;
    }

    public void setOutputColorFormat(int outputColorFormat) {
        this.outputColorFormat = outputColorFormat;
    }

    public String getBufferOutFilePath() {
        return bufferOutFilePath;
    }

    public void setBufferOutFilePath(String bufferOutFilePath) {
        this.bufferOutFilePath = bufferOutFilePath;
    }
}
