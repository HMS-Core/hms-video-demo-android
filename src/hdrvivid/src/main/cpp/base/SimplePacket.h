/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2022-2022. All rights reserved.
 * Description: packet for audio/video stream
 * Create: 2022-05-16
 */

#ifndef SIMPLE_PACKET_H
#define SIMPLE_PACKET_H

#include "HdrVividKits.h"
#include "HdrMetaData.h"

class SimplePacket {
public:
    SimplePacket();

    ~SimplePacket();

    void Clear();

public:
    int64_t GetPtsUs() const;

    void SetPtsUs(int64_t inPtsUs);

    bool IsEndOfStream() const;

    void SetEndOfStream(bool inEndOfStream);

    uint8_t *GetData() const;

    void SetData(uint8_t *inData);

    int32_t GetSize() const;

    void SetSize(int32_t inSize);

    int32_t GetWidth() const;

    void SetWidth(int32_t inWidth);

    int32_t GetHeight() const;

    void SetHeight(int32_t inHeight);

    HdrVividTransFunc GetTf() const;

    void SetTf(HdrVividTransFunc inTf);

    HdrVividColorSpace GetColorSpace() const;

    void SetColorSpace(HdrVividColorSpace inColorSpace);

    HdrVividColorFormat GetColorFormat() const;

    void SetColorFormat(HdrVividColorFormat inColorFormat);

    HdrMetaData *GetHdrMetaData();

private:
    int64_t ptsUs{-1};

    bool endOfStream{false};

    uint8_t *data{nullptr};

    int32_t size{-1};

    int32_t width{-1};

    int32_t height{-1};

    HdrVividTransFunc tf{TRANSFUNC_PQ};

    HdrVividColorSpace colorSpace{COLORSPACE_BT2020};

    HdrVividColorFormat colorFormat{COLORFORMAT_R8G8B8A8};

    HdrMetaData hdrMetaData;
};

#endif // SIMPLE_PACKET_H
