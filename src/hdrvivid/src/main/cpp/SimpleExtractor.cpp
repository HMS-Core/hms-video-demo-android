/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2022-2022. All rights reserved.
 * Description: extractor hdr vivid packet from video mp4 files
 * Create: 2022-05-15
 */

#include <cerrno>
#include <string.h>
#include "SimpleExtractor.h"
#include "SimpleLog.h"
#include "TeStream.h"
#include "nalu.h"
#include "Sei.h"

#define COLOR_STANDARD_BT709 1
#define COLOR_STANDARD_BT601_PAL 2
#define COLOR_STANDARD_BT601_NTSC 4
#define COLOR_STANDARD_BT2020 6

#define COLOR_TRANSFER_ST2084 6
#define COLOR_TRANSFER_HLG 7

SimpleExtractor::SimpleExtractor()
{
}

SimpleExtractor::~SimpleExtractor()
{
    if (m_me != nullptr) {
        AMediaExtractor_delete(m_me);
        m_me = nullptr;
    }

    if (m_mf != nullptr) {
        AMediaFormat_delete(m_mf);
        m_mf = nullptr;
    }

    if (m_fp != nullptr) {
        fclose(m_fp);
        m_fp = nullptr;
    }
}

bool SimpleExtractor::Open(const char *filePath)
{
    LOGI("%s, filePath=%s", __FUNCTION__, filePath);

    if (m_fp != nullptr) {
        fclose(m_fp);
        m_fp = nullptr;
    }
    m_fp = fopen(filePath, "r");
    if (m_fp == nullptr) {
        LOGW("%s failed to open, err=%d", __FUNCTION__, errno);
        return false;
    }
    fseek(m_fp, 0, SEEK_END);
    int32_t fileSize = ftell(m_fp);
    fseek(m_fp, 0, SEEK_SET);

    if (m_me != nullptr) {
        AMediaExtractor_delete(m_me);
    }
    m_me = AMediaExtractor_new();

    media_status_t status = AMediaExtractor_setDataSourceFd(m_me, fileno(m_fp), 0, fileSize);
    if (status != AMEDIA_OK) {
        LOGW("%s failed to open, status=%d", __FUNCTION__, status);
        return false;
    }

    size_t trackCount = AMediaExtractor_getTrackCount(m_me);
    bool found = false;
    for (size_t i = 0; i < trackCount; ++i) {
        m_mf = AMediaExtractor_getTrackFormat(m_me, i);
        if (m_mf == nullptr) {
            LOGW("%s failed to get mediaformat, idx=%zu", __FUNCTION__, i);
            continue;
        }

        const char *mime = nullptr;
        AMediaFormat_getString(m_mf, AMEDIAFORMAT_KEY_MIME, &mime);
        if (strcmp(mime, mimeHevc) == 0) {
            AMediaFormat_getInt32(m_mf, AMEDIAFORMAT_KEY_COLOR_STANDARD, &m_colorStandard);
            AMediaFormat_getInt32(m_mf, AMEDIAFORMAT_KEY_COLOR_TRANSFER, &m_colorTransfer);

            if (m_colorStandard == COLOR_STANDARD_BT2020 &&
                (m_colorTransfer == COLOR_TRANSFER_ST2084 || m_colorTransfer == COLOR_TRANSFER_HLG)) {
                AMediaFormat_getInt32(m_mf, AMEDIAFORMAT_KEY_WIDTH, &m_width);
                AMediaFormat_getInt32(m_mf, AMEDIAFORMAT_KEY_HEIGHT, &m_height);
                AMediaFormat_getInt64(m_mf, AMEDIAFORMAT_KEY_DURATION, &m_durationUs);

                found = true;
                AMediaExtractor_selectTrack(m_me, i);
                break;
            }
        }

        AMediaFormat_delete(m_mf);
        m_mf = nullptr;
    }

    if (!found) {
        LOGW("%s failed to find HDR Video", __FUNCTION__);
        return false;
    }

    LOGI("%s end, durationUs=%" PRId64, __FUNCTION__, m_durationUs);
    return true;
}

bool SimpleExtractor::Close()
{
    LOGI("%s begin", __FUNCTION__);

    if (m_me != nullptr) {
        AMediaExtractor_delete(m_me);
        m_me = nullptr;
    }

    if (m_mf != nullptr) {
        AMediaFormat_delete(m_mf);
        m_mf = nullptr;
    }

    if (m_fp != nullptr) {
        fclose(m_fp);
        m_fp = nullptr;
    }

    m_seq = 0;
    m_eof = false;

    LOGI("%s end", __FUNCTION__);
    return true;
}

SimplePacket *SimpleExtractor::GetNextSample()
{
    ssize_t sampleSize = AMediaExtractor_readSampleData(m_me, m_sample, sampleCapacity);
    int64_t ptsUs = AMediaExtractor_getSampleTime(m_me);
    if (sampleSize <= 0) {
        m_simplePacket.SetEndOfStream(true);
        m_eof = true;
        return nullptr;
    }
    AMediaExtractor_advance(m_me);

    m_simplePacket.SetPtsUs(ptsUs);
    m_simplePacket.SetEndOfStream(false);
    m_eof = false;
    m_simplePacket.SetData(m_sample);
    m_simplePacket.SetSize(sampleSize);
    m_simplePacket.SetWidth(m_width);
    m_simplePacket.SetHeight(m_height);

    SetTrc();
    SetColorSpace();

    HdrMetaData *hmd = m_simplePacket.GetHdrMetaData();
    ParseHdrMetaData(hmd, m_sample, sampleSize);

    LOGD("%s, meta: seq=%d, ptsUs=%" PRId64 ", size=%d, WxH=%dx%d, GBR=%d/%d, %d/%d, %d/%d, whitePoint=%d/%d, display=%d/%d, clli=%d/%d, size=%d",
         __FUNCTION__, m_seq, m_simplePacket.GetPtsUs(), m_simplePacket.GetSize(), m_simplePacket.GetWidth(), m_simplePacket.GetHeight(),
         hmd->GetGx(), hmd->GetGy(), hmd->GetBx(), hmd->GetBy(), hmd->GetRx(), hmd->GetRy(),
         hmd->GetWhitePointX(), hmd->GetWhitePointY(),
         hmd->GetMaxDisplayMasteringLum(), hmd->GetMinDisplayMasteringLum(),
         hmd->GetMaxContentLightLevel(), hmd->GetMaxPicAverageLightLevel(),
         hmd->GetDmSize());

    m_seq++;
    return &m_simplePacket;
}

bool SimpleExtractor::IsEof() const
{
    return m_eof;
}

int64_t SimpleExtractor::GetDurationUs() const
{
    return m_durationUs;
}

bool SimpleExtractor::SetTrc()
{
    if (m_colorTransfer == COLOR_TRANSFER_ST2084) {
        m_simplePacket.SetTf(TRANSFUNC_PQ);
    } else if (m_colorTransfer == COLOR_TRANSFER_HLG) {
        m_simplePacket.SetTf(TRANSFUNC_HLG);
    } else {
        return false;
    }

    return true;
}

bool SimpleExtractor::SetColorSpace()
{
    if (m_colorStandard == COLOR_STANDARD_BT2020) {
        m_simplePacket.SetColorSpace(COLORSPACE_BT2020);
    } else {
        return false;
    }

    return true;
}

int CheckStartCode(const unsigned char *p)
{
    if (p[NALU_STARTCODE_FIRST] == 0 && p[NALU_STARTCODE_SECOND] == 0 && p[NALU_STARTCODE_THIRD] == 1) {
        return NALU_STARTCODE_SIZE_3;
    } else if (p[NALU_STARTCODE_FIRST] == 0 && p[NALU_STARTCODE_SECOND] == 0 && p[NALU_STARTCODE_THIRD] == 0 &&
               p[NALU_STARTCODE_FOURTH] == 1) {
        return NALU_STARTCODE_SIZE_4;
    }
    return 0;
}

// Start code and Emulation Prevention need this to be defined in identical manner at encoder and decoder
static const int ZEROBYTES_SHORTSTARTCODE = 2; // indicates the number of zero bytes in the short start-code prefix
static int EBSPtoRBSP(unsigned char *tempBuffer, const unsigned char *streamBuff, int endBytePos, int beginBytePos)
{
    int i, j, cnt;

    if (endBytePos < beginBytePos) {
        return endBytePos;
    }

    cnt = 0;
    j = beginBytePos;

    for (i = beginBytePos; i < endBytePos; i++) {
        // starting from beginBytePos to avoid header information
        // in NAL unit, 0x000000, 0x000001 or 0x000002 shall not occur at any byte-aligned position
        if (cnt == ZEROBYTES_SHORTSTARTCODE && streamBuff[i] < 0x03) {
            return -1;
        }

        if (cnt == ZEROBYTES_SHORTSTARTCODE && streamBuff[i] == 0x03) {
            // check the 4th byte after 0x000003, except when cabac_zero_word is used, in which case the last three bytes of this NAL unit must be 0x000003
            if ((i < endBytePos - 1) && (streamBuff[i + 1] > 0x03)) {
                return -1;
            }

            // if cabac_zero_word is used, the final byte of this NAL unit(0x03) is discarded, and the last two bytes of RBSP must be 0x0000
            if (i == endBytePos - 1) {
                return j;
            }

            i++;
            cnt = 0;
        }

        tempBuffer[j] = streamBuff[i];

        if (streamBuff[i] == 0x00) {
            cnt++;
        } else {
            cnt = 0;
        }

        j++;
    }

    return j;
}

static int NALUtoRBSP(unsigned char *leftBuffer, const unsigned char *nalu, unsigned int len)
{
    return EBSPtoRBSP(leftBuffer, nalu, len, 0);
}

static bool ParseSei(HdrMetaData *hmd, uint8_t *data, int32_t size)
{
    TeStream teStream;
    TeStream *teS = &teStream;
    TE_InitStream(teS, data, GET_BITS(size));

    int32_t readBits = 0;

    // get payload type
    if (GetBitCnt(teS) <= GET_BITS(1)) {
        return false;
    }
    int32_t payloadType = TE_ReadBits(teS, GET_BITS(1));
    readBits += GET_BITS(1);
    if (GetBitCnt(teS) <= GET_BITS(1)) {
        return false;
    }
    int32_t nextByte = TE_ReadBits(teS, GET_BITS(1));
    readBits += GET_BITS(1);
    while (nextByte == SEI_PAYLOAD_HEADER_STOP) {
        payloadType += nextByte;

        if (GetBitCnt(teS) <= GET_BITS(1)) {
            return false;
        }

        // need read next byte to check boundary
        nextByte = TE_ReadBits(teS, GET_BITS(1));
        readBits += GET_BITS(1);
    }

    // get payload size
    if (GetBitCnt(teS) <= GET_BITS(1)) {
        return false;
    }
    int32_t payloadSize = nextByte;
    nextByte = TE_ReadBits(teS, GET_BITS(1));
    readBits += GET_BITS(1);
    while (nextByte == SEI_PAYLOAD_HEADER_STOP) {
        payloadSize += nextByte;

        if (GetBitCnt(teS) <= GET_BITS(1)) {
            return false;
        }

        // need read next byte to check boundary
        nextByte = TE_ReadBits(teS, GET_BITS(1));
        readBits += GET_BITS(1);
    }

    if (GET_BITS(size) - readBits < GET_BITS(payloadSize)) {
        return false;
    }

    bool found = false;
    if (payloadType == SEI_MASTERING_DISPLAY_COLOUR_VOLUME) {
        found = hmd->ParseMdcv(GetRemainingStart(teS) - 1, payloadSize);
    } else if (payloadType == SEI_CONTENT_LIGHT_LEVEL_INFO) {
        found = hmd->ParseClli(GetRemainingStart(teS) - 1, payloadSize);
    } else if (payloadType == SEI_USER_DATA_REGISTERED_ITU_T_T35) {
        found = hmd->ParseDm(GetRemainingStart(teS) - 1, payloadSize);
    }

    return found;
}

void SimpleExtractor::ParseNalu(HdrMetaData *hmd, const uint8_t *data, int32_t size)
{
    int32_t startCodeSize = 0;
    int32_t userDataSize = 0;
    const uint8_t *userData = nullptr;

    const uint8_t *p = data;
    while (p < data + size) {
        startCodeSize = CheckStartCode(p);
        if (startCodeSize > 0) {
            p += startCodeSize;
        } else {
            p++;
            continue;
        }

        if (userData != nullptr) {
            userDataSize = (p - userData) - startCodeSize;
            int leftSize = NALUtoRBSP(m_ebsp, userData, userDataSize);
            if (leftSize > 0) {
                ParseSei(hmd, m_ebsp, leftSize);
            }

            userData = nullptr;
        }

        int32_t nalType = (*p & NALU_UNIT_TYPE_MASK) >> 1;  // | F(1) | Type(6) | LayerId(6) | TID(3) |
        if (nalType == NALU_TYPE_HEVC_PRE_SEI || nalType == NALU_TYPE_HEVC_SUF_SEI) {
            userData = p + NALU_UNIT_HEADER_SIZE;
        }
        p++;
    }

    if (userData != nullptr) {
        userDataSize = (p - userData) - (startCodeSize + 1);
        int leftSize = NALUtoRBSP(m_ebsp, userData, userDataSize);
        if (leftSize > 0) {
            ParseSei(hmd, m_ebsp, leftSize);
        }
    }
}

void SimpleExtractor::ParseHdrMetaData(HdrMetaData *hmd, const uint8_t *data, int32_t size)
{
    if (data == nullptr || size <= 0) {
        return;
    }

    ParseNalu(hmd, data, size);
}
