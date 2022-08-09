/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2022-2022. All rights reserved.
 * Description: packet for audio/video stream
 * Create: 2022-05-16
 */

#include "SimplePacket.h"

SimplePacket::SimplePacket() = default;

SimplePacket::~SimplePacket() = default;

void SimplePacket::Clear()
{
    ptsUs = -1;
    endOfStream = false;
    data = nullptr;
    size = -1;
    width = -1;
    height = -1;

    tf = TRANSFUNC_PQ;
    colorSpace = COLORSPACE_BT2020;
    colorFormat = COLORFORMAT_R8G8B8A8;

    hdrMetaData.Clear();
}

int64_t SimplePacket::GetPtsUs() const
{
    return ptsUs;
}

void SimplePacket::SetPtsUs(int64_t inPtsUs)
{
    SimplePacket::ptsUs = inPtsUs;
}

bool SimplePacket::IsEndOfStream() const
{
    return endOfStream;
}

void SimplePacket::SetEndOfStream(bool inEndOfStream)
{
    SimplePacket::endOfStream = inEndOfStream;
}

uint8_t *SimplePacket::GetData() const
{
    return data;
}

void SimplePacket::SetData(uint8_t *inData)
{
    SimplePacket::data = inData;
}

int32_t SimplePacket::GetSize() const
{
    return size;
}

void SimplePacket::SetSize(int32_t inSize)
{
    SimplePacket::size = inSize;
}

int32_t SimplePacket::GetWidth() const
{
    return width;
}

void SimplePacket::SetWidth(int32_t inWidth)
{
    SimplePacket::width = inWidth;
}

int32_t SimplePacket::GetHeight() const
{
    return height;
}

void SimplePacket::SetHeight(int32_t inHeight)
{
    SimplePacket::height = inHeight;
}

HdrVividTransFunc SimplePacket::GetTf() const
{
    return tf;
}

void SimplePacket::SetTf(HdrVividTransFunc inTf)
{
    SimplePacket::tf = inTf;
}

HdrVividColorSpace SimplePacket::GetColorSpace() const
{
    return colorSpace;
}

void SimplePacket::SetColorSpace(HdrVividColorSpace inColorSpace)
{
    SimplePacket::colorSpace = inColorSpace;
}

HdrVividColorFormat SimplePacket::GetColorFormat() const
{
    return colorFormat;
}

void SimplePacket::SetColorFormat(HdrVividColorFormat inColorFormat)
{
    SimplePacket::colorFormat = inColorFormat;
}

HdrMetaData *SimplePacket::GetHdrMetaData()
{
    return &hdrMetaData;
}
