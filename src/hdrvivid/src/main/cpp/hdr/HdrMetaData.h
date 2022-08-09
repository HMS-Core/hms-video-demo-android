/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2022-2022. All rights reserved.
 * Description: hdr vivid static and dynamic meta data
 * Create: 2022-05-16
 */

#ifndef HDR_META_DATA_H
#define HDR_META_DATA_H

#include <cstdint>

class HdrMetaData {
public:
    HdrMetaData();

    ~HdrMetaData();

    void Clear();

    bool ParseMdcv(const uint8_t *payload, int32_t size);

    bool ParseClli(const uint8_t *payload, int32_t size);

    bool ParseDm(const uint8_t *payload, int32_t size);

public:
    int32_t GetGx() const;

    void SetGx(int32_t inGx);

    int32_t GetGy() const;

    void SetGy(int32_t inGy);

    int32_t GetBx() const;

    void SetBx(int32_t inBx);

    int32_t GetBy() const;

    void SetBy(int32_t inBy);

    int32_t GetRx() const;

    void SetRx(int32_t inRx);

    int32_t GetRy() const;

    void SetRy(int32_t inRy);

    int32_t GetWhitePointX() const;

    void SetWhitePointX(int32_t inWhitePointX);

    int32_t GetWhitePointY() const;

    void SetWhitePointY(int32_t inWhitePointY);

    int32_t GetMaxDisplayMasteringLum() const;

    void SetMaxDisplayMasteringLum(int32_t inMaxDisplayMasteringLum);

    int32_t GetMinDisplayMasteringLum() const;

    void SetMinDisplayMasteringLum(int32_t inMinDisplayMasteringLum);

    int32_t GetMaxContentLightLevel() const;

    void SetMaxContentLightLevel(int32_t inMaxContentLightLevel);

    int32_t GetMaxPicAverageLightLevel() const;

    void SetMaxPicAverageLightLevel(int32_t inMaxPicAverageLightLevel);

    void SetDm(const uint8_t *inDmData, int32_t inDmSize);

    const uint8_t *GetDmData() const;

    int32_t GetDmSize() const;

private:
    static const int32_t minSmSize = 4;

    static const int32_t minDmSize = 24;

    static const int32_t maxDmSize = 128;

    /*
     * MDCV
     */
    int32_t gX{-1};

    int32_t gY{-1};

    int32_t bX{-1};

    int32_t bY{-1};

    int32_t rX{-1};

    int32_t rY{-1};

    int32_t whitePointX{-1};

    int32_t whitePointY{-1};

    int32_t maxDisplayMasteringLum{-1};

    int32_t minDisplayMasteringLum{-1};

    /*
     * Clli
     */
    int32_t maxContentLightLevel{-1};

    int32_t maxPicAverageLightLevel{-1};

    /*
     * dynamic meta data
     */
    uint8_t dmData[maxDmSize]{};

    int32_t dmSize{-1};
};

#endif // HDR_META_DATA_H
