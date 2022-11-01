/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2022-2022. All rights reserved.
 * Description: hdr vivid static and dynamic meta data
 * Create: 2022-05-16
 */

#include <cstring>
#include "TeStream.h"
#include "SimpleLog.h"
#include "HdrMetaData.h"

HdrMetaData::HdrMetaData() = default;

HdrMetaData::~HdrMetaData() = default;

void HdrMetaData::Clear()
{
    gX = -1;
    gY = -1;
    bX = -1;
    bY = -1;
    rX = -1;
    rY = -1;
    whitePointX = -1;
    whitePointY = -1;
    maxDisplayMasteringLum = -1;
    minDisplayMasteringLum = -1;

    maxContentLightLevel = -1;
    maxPicAverageLightLevel = -1;

    dmSize = -1;
}

bool HdrMetaData::ParseMdcv(const uint8_t *payload, int32_t size)
{
    if (size < minDmSize || size > maxDmSize) {
        return false;
    }

    TeStream teStream;
    TeStream *teS = &teStream;
    TE_InitStream(teS, payload, GET_BITS(size));

    gX = TE_ReadBits(teS, 16);
    gY = TE_ReadBits(teS, 16);
    bX = TE_ReadBits(teS, 16);
    bY = TE_ReadBits(teS, 16);
    rX = TE_ReadBits(teS, 16);
    rY = TE_ReadBits(teS, 16);
    whitePointX = TE_ReadBits(teS, 16);
    whitePointY = TE_ReadBits(teS, 16);
    maxDisplayMasteringLum = TE_ReadBits32(teS);
    minDisplayMasteringLum = TE_ReadBits32(teS);

    return true;
}

bool HdrMetaData::ParseClli(const uint8_t *payload, int32_t size)
{
    if (size < minSmSize || size > maxDmSize) {
        return false;
    }

    TeStream teStream;
    TeStream *teS = &teStream;
    TE_InitStream(teS, payload, GET_BITS(size));

    maxContentLightLevel = TE_ReadBits(teS, 16);
    maxPicAverageLightLevel = TE_ReadBits(teS, 16);

    return true;
}

bool HdrMetaData::ParseDm(const uint8_t *payload, int32_t size)
{
    if (size <= 0 || size > maxDmSize) {
        return false;
    }

    TeStream teStream;
    TeStream *teS = &teStream;
    TE_InitStream(teS, payload, GET_BITS(size));

    int32_t readBytes = 0;
    int32_t countryCode = TE_ReadBits(teS, 8);
    readBytes++;
    if (countryCode == 0xFF) {
        readBytes++;
    }

    // terminalProvideCode
    TE_ReadBits(teS, 16);
    // terminalProvideOrientedCode
    TE_ReadBits(teS, 16);

    SetDm(GetRemainingStart(teS), size - GetConsumedBytes(teS));
    return true;
}

int32_t HdrMetaData::GetGx() const
{
    return gX;
}

void HdrMetaData::SetGx(int inGx)
{
    HdrMetaData::gX = inGx;
}

int32_t HdrMetaData::GetGy() const
{
    return gY;
}

void HdrMetaData::SetGy(int inGy)
{
    HdrMetaData::gY = inGy;
}

int32_t HdrMetaData::GetBx() const
{
    return bX;
}

void HdrMetaData::SetBx(int inBx)
{
    HdrMetaData::bX = inBx;
}

int32_t HdrMetaData::GetBy() const
{
    return bY;
}

void HdrMetaData::SetBy(int inBy)
{
    HdrMetaData::bY = inBy;
}

int32_t HdrMetaData::GetRx() const
{
    return rX;
}

void HdrMetaData::SetRx(int inRx)
{
    HdrMetaData::rX = inRx;
}

int32_t HdrMetaData::GetRy() const
{
    return rY;
}

void HdrMetaData::SetRy(int inRy)
{
    HdrMetaData::rY = inRy;
}

int32_t HdrMetaData::GetWhitePointX() const
{
    return whitePointX;
}

void HdrMetaData::SetWhitePointX(int inWhitePointX)
{
    HdrMetaData::whitePointX = inWhitePointX;
}

int32_t HdrMetaData::GetWhitePointY() const
{
    return whitePointY;
}

void HdrMetaData::SetWhitePointY(int inWhitePointY)
{
    HdrMetaData::whitePointY = inWhitePointY;
}

int32_t HdrMetaData::GetMaxDisplayMasteringLum() const
{
    return maxDisplayMasteringLum;
}

void HdrMetaData::SetMaxDisplayMasteringLum(int inMaxDisplayMasteringLum)
{
    HdrMetaData::maxDisplayMasteringLum = inMaxDisplayMasteringLum;
}

int32_t HdrMetaData::GetMinDisplayMasteringLum() const
{
    return minDisplayMasteringLum;
}

void HdrMetaData::SetMinDisplayMasteringLum(int inMinDisplayMasteringLum)
{
    HdrMetaData::minDisplayMasteringLum = inMinDisplayMasteringLum;
}

int32_t HdrMetaData::GetMaxContentLightLevel() const
{
    return maxContentLightLevel;
}

void HdrMetaData::SetMaxContentLightLevel(int inMaxContentLightLevel)
{
    HdrMetaData::maxContentLightLevel = inMaxContentLightLevel;
}

int32_t HdrMetaData::GetMaxPicAverageLightLevel() const
{
    return maxPicAverageLightLevel;
}

void HdrMetaData::SetMaxPicAverageLightLevel(int inMaxPicAverageLightLevel)
{
    HdrMetaData::maxPicAverageLightLevel = inMaxPicAverageLightLevel;
}

void HdrMetaData::SetDm(const uint8_t *inDmData, int32_t inDmSize)
{
    if (inDmData == nullptr || inDmSize > maxDmSize || inDmSize <= 0) {
        return;
    }

    for (int32_t i = 0; i < inDmSize; ++i) {
        dmData[i] = inDmData[i];
    }
    dmSize = inDmSize;
}

const uint8_t *HdrMetaData::GetDmData() const
{
    return dmData;
}

int32_t HdrMetaData::GetDmSize() const
{
    return dmSize;
}

