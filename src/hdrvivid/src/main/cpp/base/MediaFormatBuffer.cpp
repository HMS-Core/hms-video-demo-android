/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2022-2022. All rights reserved.
 * Description: MediaFormat Buffer
 * Create: 2022-10-09
 */

#include "MediaFormatBuffer.h"

MediaFormatBuffer::MediaFormatBuffer() = default;

MediaFormatBuffer::~MediaFormatBuffer() = default;

void MediaFormatBuffer::Clear()
{
    data = nullptr;
    size = -1;
}

uint8_t *MediaFormatBuffer::GetData() const
{
    return data;
}

void MediaFormatBuffer::SetData(uint8_t *inData)
{
    MediaFormatBuffer::data = inData;
}

int32_t MediaFormatBuffer::GetSize() const
{
    return size;
}

void MediaFormatBuffer::SetSize(int32_t inSize)
{
    MediaFormatBuffer::size = inSize;
}
