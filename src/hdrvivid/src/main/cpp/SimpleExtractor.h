/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2022-2022. All rights reserved.
 * Description: extractor hdr vivid packet from video mp4 files
 * Create: 2022-05-15
 */

#ifndef SIMPLE_EXTRACTOR_H
#define SIMPLE_EXTRACTOR_H

#include <stdint.h>
#include <stdio.h>
#include <media/NdkMediaExtractor.h>
#include "SimplePacket.h"
#include "MediaFormatBuffer.h"

class SimpleExtractor {
public:
    SimpleExtractor();

    ~SimpleExtractor();

    bool Open(const char *filePath);

    bool Close();

    SimplePacket *GetNextSample();

    bool IsEof() const;

    int64_t GetDurationUs() const;

    AMediaFormat *GetMediaFormat() const;

    MediaFormatBuffer *GetMediaFormatBuffer(const char *name);

private:
    bool SetTrc();

    bool SetColorSpace();

    void ParseHdrMetaData(HdrMetaData *hmd, const uint8_t *data, int32_t size);

    void ParseNalu(HdrMetaData *hmd, const uint8_t *data, int32_t size);

private:
    int32_t m_seq{0};
    bool m_eof{false};

    SimplePacket m_simplePacket;

    static constexpr const char *mimeHevc{"video/hevc"};
    static constexpr int32_t sampleCapacity{5 * 1024 * 1024};

    FILE *m_fp{nullptr};
    AMediaExtractor *m_me{nullptr};
    AMediaFormat *m_mf{nullptr};
    int64_t m_durationUs{-1};
    int32_t m_colorStandard{-1};
    int32_t m_colorTransfer{-1};
    int32_t m_width{-1};
    int32_t m_height{-1};

    uint8_t m_sample[sampleCapacity];
    uint8_t m_ebsp[sampleCapacity];
    MediaFormatBuffer m_mediaFormatBuffer;
};

#endif // SIMPLE_EXTRACTOR_H
