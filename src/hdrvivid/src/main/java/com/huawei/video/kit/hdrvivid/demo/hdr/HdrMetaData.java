/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2022-2022. All rights reserved.
 */

package com.huawei.video.kit.hdrvivid.demo.hdr;

/**
 * hdr vivid static and dynamic meta data
 */
public class HdrMetaData {
    /*
     * MDCV  G display_primaries_x
     */
    public int gX = -1;

    /*
     * MDCV  G display_primaries_y
     */
    public int gY = -1;

    /*
     * MDCV  B display_primaries_x
     */
    public int bX = -1;

    /*
     * MDCV  B display_primaries_y
     */
    public int bY = -1;

    /*
     * MDCV  R display_primaries_x
     */
    public int rX = -1;

    /*
     * MDCV  R display_primaries_y
     */
    public int rY = -1;

    /*
     * MDCV white_point_x
     */
    public int whitePointX = -1;

    /*
     * MDCV white_point_y
     */
    public int whitePointY = -1;

    /*
     * MDCV max_display_mastering_luminance
     */
    public int maxDisplayMasteringLum = -1;

    /*
     * MDCV min_display_mastering_luminance
     */
    public int minDisplayMasteringLum = -1;

    /*
     * Clli max_content_light_level
     */
    public int maxContentLightLevel = -1;

    /*
     * Clli max_pic_average_light_level
     */
    public int maxPicAverageLightLevel = -1;

    /*
     * dynamic meta data
     */
    public byte[] dmData;

    public int dmSize = -1;
}
