/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2022-2022. All rights reserved.
 * Description: MediaFormat Buffer
 * Create: 2022-10-09
 */

#ifndef MEDIAFORMAT_BUFFER_H
#define MEDIAFORMAT_BUFFER_H

#include <cstdint>

class MediaFormatBuffer {
public:
    MediaFormatBuffer();

    ~MediaFormatBuffer();

    void Clear();

public:
    uint8_t *GetData() const;

    void SetData(uint8_t *inData);

    int32_t GetSize() const;

    void SetSize(int32_t inSize);

private:
    uint8_t *data{nullptr};

    int32_t size{-1};
};

#endif // MEDIAFORMAT_BUFFER_H
